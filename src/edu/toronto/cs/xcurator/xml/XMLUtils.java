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
package edu.toronto.cs.xcurator.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class XMLUtils {
  
  public static String getUri(Element element, String defaultBaseUri) {
    if (defaultBaseUri == null) {
      throw new IllegalArgumentException("Default base URI cannot be null.");
    }
    String baseUri = element.getNamespaceURI();
    baseUri = baseUri != null ? baseUri : defaultBaseUri;
    return baseUri + "#" + element.getLocalName();
  }
  
  /**
   * Extract attributes and transform them to child elements. Attributes that are
   * namespace definitions will be ignored.
   * @param doc
   * @return transformed document
   * @throws ParserConfigurationException 
   */
  public static Document extractAttributesToElements(Document doc) throws ParserConfigurationException {
    Element root = doc.getDocumentElement();
    extractAttributesToElements(root);
    return doc;
  }

  private static void extractAttributesToElements(Element root) {
    NamedNodeMap attributeMap = root.getAttributes();
    for (int i = 0; i < attributeMap.getLength(); i++) {
      Attr attr =  (Attr) attributeMap.item(i);
      if (isNamespaceDef(attr)) {
        continue;
      }

      Element attrElement = root.getOwnerDocument().createElement(attr.getName());
      attrElement.setTextContent(attr.getValue());
      root.appendChild(attrElement);
    }

    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        extractAttributesToElements((Element) children.item(i));
      }
    }
  }
  
  /**
   * Check if the attribute node is a namespace definition.
   * @param attr
   * @return 
   */
  public static boolean isNamespaceDef(Attr attr) {
    String prefix = attr.getPrefix();
    return (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) ||
              attr.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE);
  }
  
}
