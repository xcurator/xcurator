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

import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author zhuerkan
 */
public class XmlDocumentBuilder {
  
  public Document createDocument() throws ParserConfigurationException {
    DocumentBuilder builder = createNsAwareDocumentBuilder();
    return builder.newDocument();
  }
  
  public Element addRootElement(Document doc, String rootNamespaceUri,
          String rootName) {
    Element root = doc.createElementNS(rootNamespaceUri, rootName);
    doc.appendChild(root);
    return root;
  }
  
  public DocumentBuilder createNsAwareDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    return builderFactory.newDocumentBuilder();
  }
  
  public void addNsContextToEntityElement(Element entity, NsContext nsCtx) {
    for (Map.Entry<String, String> ns : nsCtx.getNamespaces().entrySet()) {
      String attributeName = XMLConstants.XMLNS_ATTRIBUTE;
      if (!ns.getKey().equals("")) {
        attributeName += ":" + ns.getKey();
      }
      entity.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
              attributeName, ns.getValue());
    }
  }
  
  public void addUriBasedAttrToElement(String attrName, String typeUri, 
          NsContext nsContext, Element element) {
    
    String typeName = typeUri.substring(typeUri.lastIndexOf("/") + 1);
    String baseUri = typeUri.substring(0, typeUri.lastIndexOf("/") + 1);
    String prefix = nsContext.getPrefix(baseUri);
    element.setAttribute(attrName, prefix == null ? typeUri : prefix+":"+typeName);
  }
  
}
