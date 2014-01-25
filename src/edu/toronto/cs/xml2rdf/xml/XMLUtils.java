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
package edu.toronto.cs.xml2rdf.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class XMLUtils {

  static boolean debug = true;

  static XPathFactory factory = XPathFactory.newInstance();

  // ekzhu: These *ByPath functions would not work for path containing namespaces.
  public static NodeList getNodesByPath(String path, Element localElement, Document doc) throws XPathExpressionException {
    // Note: if using absolute path, then the root element must also be specified,
    // that is, it should be like "/clinical_studies/clinical_study/..."
    XPath xpath = factory.newXPath();
    Object element = path.startsWith("/") || localElement == null ? doc : localElement;
    NodeList nodeList = (NodeList) xpath.evaluate(path, element, XPathConstants.NODESET);
    return nodeList;
  }

  public static boolean getBooleanPath(String path, Element localElement, Document doc) throws XPathExpressionException {
    // Note: if using absolute path, then the root element must also be specified,
    // that is, it should be like "/clinical_studies/clinical_study/..."
    XPath xpath = factory.newXPath();
    Object element = path.startsWith("/") || localElement == null ? doc : localElement;
    boolean res = (Boolean) xpath.evaluate(path, element, XPathConstants.BOOLEAN);
    return res;
  }
  public static String getStringByPath(String path, Element localElement, Document doc) throws XPathExpressionException {
    // Note the difference between this function and function "getStringsByPath"
    // The path for this function should be like "/clinical_studies/clinical_study/brief_title",
    // which returns ONLY ONE string of the first matched element "brief_title"
    XPath xpath = factory.newXPath();
    Object element = path.startsWith("/") || localElement == null ? doc : localElement;
    return (String) xpath.evaluate(path, element, XPathConstants.STRING);
  }

  public static Set<String> getStringsByPath(String path, Element localElement, Document doc) throws XPathExpressionException {
    // Note the difference between this function and function "getStringByPath"
    // The path for this function should be like "/clinical_studies/clinical_study/brief_title/text()",
    // with the extra "/text()" at the end, and it returns ALL strings of ALL matching element "brief_title"
    Set<String> ret = new HashSet<String>();

    NodeList nl = getNodesByPath(path, localElement, doc);
    for (int i = 0; i < nl.getLength(); i++) {
      if (nl.item(i) instanceof Text) {
        ret.add(((Text)nl.item(i)).getTextContent().trim());
      }
    }

    return ret;
  }


  public static Document parse(String path, int maxElement) throws SAXException, IOException, ParserConfigurationException {
    // File Parser #1
    DocumentBuilder builder = createNsAwareDocumentBuilder();
    Document doc = builder.parse(path);
    doc = pruneDocument(doc, maxElement);
    return doc;
  }

  private static Document pruneDocument(Document doc, int maxElement) throws ParserConfigurationException {
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

    if (debug)
      System.out.println("Creating document of " + newDoc.getDocumentElement().getChildNodes().getLength());
    return newDoc;
  }

  public static Document parse(InputStream is, int maxElement) throws SAXException, IOException, ParserConfigurationException {
    // File Parser #2
    DocumentBuilder builder = createNsAwareDocumentBuilder();
    Document doc = builder.parse(is);
    doc = pruneDocument(doc, maxElement);
    return doc;

  }

  public static Document parse(Reader reader, int maxElement) throws SAXException, IOException, ParserConfigurationException {
    // File Parser #3
    DocumentBuilder builder = createNsAwareDocumentBuilder();
    Document doc = builder.parse(new InputSource(reader));
    doc = pruneDocument(doc, maxElement);
    return doc;

  }
  
  public static DocumentBuilder createNsAwareDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    return builderFactory.newDocumentBuilder();
  }

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

  public static List<String> getAllLeaves(Element element) {
    // Get a list of strings representing the relative path
    // (including the current element) to all the leaf elements
    // under the current element

    // Eric: Why return a List? Returning a Set seems to make
    // more sense.

    if (element == null) {
      return null;
    }

    List<String> ret = new LinkedList<String>();
    if (isLeaf(element)) {
      ret.add(element.getNodeName());
    } else {
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if (n instanceof Element) {
          Element childElement = (Element) n;
          for (String childNodeName: getAllLeaves(childElement)) {
            ret.add(element.getNodeName() + "/" + childNodeName);
          }
        }
      }
    }

    return ret;
  }


  public static List<String> getAllLeaveValues(Element element) throws XPathExpressionException {
    if (element == null) {
      return null;
    }

    List<String> ret = new LinkedList<String>();
    if (isLeaf(element)) {
      ret.add(element.getTextContent());
    } else {
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if (n instanceof Element) {
          Element childElement = (Element) n;
          for (String childText: getAllLeaveValues(childElement)) {
            ret.add(childText);
          }
        }
      }
    }

    return ret;
  }

  public static byte[] asByteArray(Element element) throws IOException {
    ByteArrayOutputStream bis = new ByteArrayOutputStream();

    OutputFormat format = new OutputFormat(element.getOwnerDocument());
    XMLSerializer serializer = new XMLSerializer (
        bis, format);
    serializer.asDOMSerializer();
    serializer.serialize(element);

    return bis.toByteArray();
  }

  public static String asString(Element element) throws IOException {
    return new String(asByteArray(element));
  }

  public static Document attributize(Document doc) throws ParserConfigurationException {
    Element root = doc.getDocumentElement();
    attributize(root);
    return doc;
  }

  private static void attributize(Element root) {
    NamedNodeMap attributeMap = root.getAttributes();
    for (int i = 0; i < attributeMap.getLength(); i++) {
      org.w3c.dom.Attr attr =  (Attr) attributeMap.item(i);

      Element attrElement = root.getOwnerDocument().createElement(attr.getName());
      attrElement.setTextContent(attr.getValue());
      root.appendChild(attrElement);
    }

    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        attributize((Element) children.item(i));
      }
    }
  }

  public static Document addRoot(Document dataDoc, String elementName) {
    Element oldRoot = dataDoc.getDocumentElement();
    Element newRoot = dataDoc.createElement(elementName);
    dataDoc.removeChild(oldRoot);
    newRoot.appendChild(oldRoot);
    dataDoc.appendChild(newRoot);
    return dataDoc;
  }


}
