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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is replacing the edu.toronto.cs.xml2rdf.xml.XMLUtils class.
 * All added functions should be tested.
 * @author ekzhu
 */
public class XMLUtils {
  
  /**
   * Get schema uri of an element (for leaf element, use getAttributeUri instead).
   * @param element
   * @param defaultBaseUri
   * @return 
   */
  public static String getSchemaUri(Element element, String defaultBaseUri) {
    if (defaultBaseUri == null) {
      throw new IllegalArgumentException("Default base URI cannot be null.");
    }
    String baseUri = element.getNamespaceURI();
    baseUri = baseUri != null ? baseUri : defaultBaseUri;
    return baseUri + "#" + element.getLocalName();
  }
  
  /**
   * Get uri of a leaf element which is an attribute of its parent.
   * @param leaf
   * @param parent
   * @param defaultBaseUri
   * @return 
   */
  public static String getAttributeUri(Element leaf, Element parent, String defaultBaseUri) {
    return hasNamespace(leaf) && parent != null ? 
            getSchemaUri(leaf, defaultBaseUri) : 
            getSchemaUri(parent, defaultBaseUri) + "." + leaf.getNodeName();
  }
  
  public static String getValueAttributeUri(Element parent, String defaultBaseUri) {
    return getSchemaUri(parent, defaultBaseUri) + ".value";
  }
  
  public static String getRelationUri(Element subject, Element object, String defaultBaseUri) {
    return getSchemaUri(subject, defaultBaseUri) + "." + object.getNodeName();
  }
  
  /**
   * Check if the given element uses namespace for its name.
   * @param element
   * @return 
   */
  public static boolean hasNamespace(Element element) {
    return element.getNamespaceURI() != null;
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
  
  /**
   * Check if the node is a leaf node (with no child elements).
   * @param node
   * @return 
   */
  public static boolean isLeaf(Node node) {
    NodeList nodeList = node.getChildNodes();
    if (nodeList.getLength() == 0) {
      return true;
    }
    for (int i = 0; i < nodeList.getLength(); i++) {
      if (nodeList.item(i) instanceof Element) {
        // if the node contains child element it is not 
        // a leaf node
        return false;
      }
    }
    return true;
  }
  
  /**
   * Get a set of strings representing the relative path
   * (including the current element) to all the 
   * leaf elements under the current element.
   * @param element
   * @return 
   */
  public static Set<String> getPathsToLeaves(Element element) {
    if (element == null) {
      return null;
    }
    Set<String> ret = new HashSet<>();
    if (isLeaf(element)) {
      ret.add(element.getNodeName());
    } else {
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if (n instanceof Element) {
          Element childElement = (Element) n;
          for (String childNodeName: getPathsToLeaves(childElement)) {
            ret.add(element.getNodeName() + "/" + childNodeName);
          }
        }
      }
    }
    return ret;
  }
  
  /**
   * Get a set of all leaves (including the current element) under the 
   * current element.
   * @param element
   * @return 
   */
  public static Set<Element> getLeafElements(Element element) {
    if (element == null) {
      return null;
    }
    Set<Element> ret = new HashSet<>();
    if (isLeaf(element)) {
      ret.add(element);
    } else {
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if (n instanceof Element) {
          ret.addAll(getLeafElements((Element) n));
        }
      }
    }
    return ret;
  }
  
  /**
   * Get map uri-to-path of all leaves (including the current element)
   * under the current elements.
   * @param element
   * @param defaultBaseUri
   * @return 
   */
  public static Map<String, String> getLeavesUri2PathMap(Element element, 
          String defaultBaseUri) {
    if (element == null) {
      return null;
    }
    Map<String, String> leaves = new HashMap<>();
    if (isLeaf(element)) {
      String uri = getAttributeUri(element, (Element) element.getParentNode(), defaultBaseUri);
      leaves.put(uri, element.getNodeName());
    }
    else {
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if (n instanceof Element) {
          Element childElement = (Element) n;
          for (Entry<String, String> uri2path : 
                  getLeavesUri2PathMap(childElement, defaultBaseUri).entrySet()) {
            leaves.put(uri2path.getKey(), element.getNodeName() + "/" + uri2path.getValue());
          }
        }
      }
    }
    return leaves;
  }
  
  public static DocumentBuilder createNsAwareDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    return builderFactory.newDocumentBuilder();
  }
  
}
