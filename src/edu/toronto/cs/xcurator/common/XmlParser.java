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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlParser {

  private final boolean debug;

  public XmlParser() {
    this.debug = false;
  }

  public XmlParser(boolean debug) {
    this.debug = debug;
  }

  public Document parse(String path, int maxElement) throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
    Document doc = builder.parse(path);
    doc = pruneDocument(doc, maxElement);
    return doc;
  }

  public Document parse(InputStream is, int maxElement) throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
    Document doc = builder.parse(is);
    doc = pruneDocument(doc, maxElement);
    return doc;
  }

  public Document parse(Reader reader, int maxElement) throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
    Document doc = builder.parse(new InputSource(reader));
    doc = pruneDocument(doc, maxElement);
    return doc;
  }

  private Document pruneDocument(Document doc, int maxElement) throws ParserConfigurationException {
    if (maxElement == -1) {
      return doc;
    }

    Document newDoc = (Document) doc.cloneNode(false);
    Element newRoot = (Element) doc.getDocumentElement().cloneNode(false);
    newDoc.adoptNode(newRoot);
    newDoc.appendChild(newRoot);

    NodeList nl = doc.getDocumentElement().getChildNodes();
    for (int i = 0; i < maxElement && i < nl.getLength(); i++) {
      if (!(nl.item(i) instanceof Element)) {
        maxElement++;
        continue;
      }

      Node item = nl.item(i).cloneNode(true);
      newDoc.adoptNode(item);
      newDoc.getDocumentElement().appendChild(item);
    }

    if (debug) {
      System.out.println("Creating document of " + newDoc.getDocumentElement().getChildNodes().getLength());
    }
    return newDoc;
  }
  
  /**
   * Get immediate (1st level) children that are leaves.
   * @param root
   * @return 
   */
  public List<Element> getLeafChildElements(Element root) {
    List<Element> leaves = new ArrayList<>();
    if (isLeaf(root)) {
      return leaves;
    }
    NodeList nl = root.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element && isLeaf(n)) {
        leaves.add((Element)n);
      }
    }
    return leaves;
  }
  
  /**
   * Get all attributes as a list from the element, ignoring
   * namespace definitions.
   * @param element
   * @return 
   */
  public List<Attr> getAttributes(Element element) {
    List<Attr> attrList = new ArrayList<>();
    NamedNodeMap attributeMap = element.getAttributes();
    for (int i = 0; i < attributeMap.getLength(); i++) {
      Attr attr = (Attr) attributeMap.item(i);
      if (isNamespaceDef(attr)) {
        continue;
      }
      attrList.add(attr);
    }
    return attrList;
  }
  
  /**
   * Check if the attribute node is a namespace definition.
   * @param attr
   * @return 
   */
  public boolean isNamespaceDef(Attr attr) {
    String prefix = attr.getPrefix();
    return (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) ||
              attr.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE);
  }
  
  /**
   * Check if the node is a leaf node (with no child elements).
   * @param node
   * @return 
   */
  public boolean isLeaf(Node node) {
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
  
}
