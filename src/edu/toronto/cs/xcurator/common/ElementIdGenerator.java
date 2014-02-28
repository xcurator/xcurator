/*
 *    Copyright (c) 2013, University of Toronto.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package edu.toronto.cs.xcurator.common;

import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.XPathFinder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementIdGenerator {
  
  private final String resourceUriPattern;
  
  public ElementIdGenerator(String resourceUriBase) {
    this.resourceUriPattern = (resourceUriBase.endsWith("/") ? resourceUriBase :
            resourceUriBase + "/") + "${UUID}";
  }

  public String generateUri(NsContext entityNamespaceContext,
          Element element, Document dataDoc, XPathFinder xpath) 
          throws NoSuchAlgorithmException, IOException, XPathExpressionException {
    return generateUri(this.resourceUriPattern, entityNamespaceContext, element, dataDoc, xpath);
  }
  
  public String generateUri(String resourceUriPattern, NsContext entityNamespaceContext,
          Element element, Document dataDoc, XPathFinder xpath)
          throws NoSuchAlgorithmException, IOException, XPathExpressionException {
    int lastEndIndex = 0;
    String id = resourceUriPattern != null ? resourceUriPattern : this.resourceUriPattern;
    String uuidPattern = "UUID";
    String generatedId = "";
    do {
      int startIndex = id.indexOf("${", lastEndIndex);
      if (startIndex == -1) {
        break;
      }
      int endIndex = id.indexOf("}", startIndex);
      if (endIndex == -1) {
        break;
      }
      String literal = id.substring(lastEndIndex, startIndex);
      String path = id.substring(startIndex + 2, endIndex);
      String pathValue = null;
      if (uuidPattern.equals(path)) {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        pathValue = "";
        byte[] md5 = digest.digest(asByteArray(element));
        for (byte b : md5) {
          pathValue += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
        }
      } else {
        pathValue = xpath.getStringByPath(path, element, dataDoc, entityNamespaceContext);
      }
      generatedId += literal + pathValue;
      lastEndIndex = endIndex + 1;
    } while (true);

    if (generatedId.trim().length() == 0) {
      String messString = "Error in generating id: " + element.toString();
      Logger.getLogger(ElementIdGenerator.class.getName()).log(Level.SEVERE, messString);
    }
    return generatedId;
  }

  public byte[] asByteArray(Element element) throws IOException {
    ByteArrayOutputStream bis = new ByteArrayOutputStream();

    OutputFormat format = new OutputFormat(element.getOwnerDocument());
    XMLSerializer serializer = new XMLSerializer(
            bis, format);
    serializer.asDOMSerializer();
    serializer.serialize(element);

    return bis.toByteArray();
  }
}
