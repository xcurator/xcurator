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

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XmlXPathFinder {

  private XPathFactory factory = XPathFactory.newInstance();

  // ekzhu: These *ByPath functions would not work for path containing namespaces.
  public NodeList getNodesByPath(String path, Element localElement, Document doc,
          NsContext nsContext) throws XPathExpressionException {
    // Note: if using absolute path, then the root element must also be specified,
    // that is, it should be like "/clinical_studies/clinical_study/..."
    XPath xpath = factory.newXPath();
    xpath.setNamespaceContext(nsContext);
    Object element = getElementToBeSearched(path, localElement, doc);
    NodeList nodeList = (NodeList) xpath.evaluate(path, element, XPathConstants.NODESET);
    return nodeList;
  }

  public boolean getBooleanPath(String path, Element localElement, Document doc,
          NsContext nsContext) throws XPathExpressionException {
    // Note: if using absolute path, then the root element must also be specified,
    // that is, it should be like "/clinical_studies/clinical_study/..."
    XPath xpath = factory.newXPath();
    Object element = getElementToBeSearched(path, localElement, doc);
    boolean res = (Boolean) xpath.evaluate(path, element, XPathConstants.BOOLEAN);
    return res;
  }

  public String getStringByPath(String path, Element localElement, Document doc,
          NsContext nsContext) throws XPathExpressionException {
    // Note the difference between this function and function "getStringsByPath"
    // The path for this function should be like "/clinical_studies/clinical_study/brief_title",
    // which returns ONLY ONE string of the first matched element "brief_title"
    XPath xpath = factory.newXPath();
    Object element = getElementToBeSearched(path, localElement, doc);
    return (String) xpath.evaluate(path, element, XPathConstants.STRING);
  }

  public Set<String> getStringsByPath(String path, Element localElement, Document doc,
          NsContext nsContext) throws XPathExpressionException {
    // Note the difference between this function and function "getStringByPath"
    // The path for this function should be like "/clinical_studies/clinical_study/brief_title/text()",
    // with the extra "/text()" at the end, and it returns ALL strings of ALL matching element "brief_title"
    Set<String> ret = new HashSet<String>();

    NodeList nl = getNodesByPath(path, localElement, doc, nsContext);
    for (int i = 0; i < nl.getLength(); i++) {
      if (nl.item(i) instanceof Text) {
        ret.add(((Text) nl.item(i)).getTextContent().trim());
      }
    }

    return ret;
  }

  private Object getElementToBeSearched(String path, Element localElement, Document doc) {
    return path.startsWith("/") || localElement == null ? doc : localElement;
  }
}
