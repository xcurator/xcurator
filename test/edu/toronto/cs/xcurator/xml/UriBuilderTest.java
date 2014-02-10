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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class UriBuilderTest {
  
  private XmlParser parser;
  private UriBuilder uriBuilder;
  
  @Before
  public void setup() {
    parser = new XmlParser();
    uriBuilder = new UriBuilder("http://example.org", "example");
  }
  
  @Test
  public void getUriTest() throws SAXException, IOException, ParserConfigurationException {
    Document dataDoc = parser.parse(
            XmlParserTest.class.getResourceAsStream(
                    "/secxbrls/data/fb-20121231.xml"), -1);
    Element root = dataDoc.getDocumentElement();
    NsContext rootNsContext = new NsContext(root);
    Assert.assertTrue("Incorrect base URI", 
                uriBuilder.getElementUri(root, rootNsContext).equals(
                        root.getNamespaceURI() + "#" + root.getLocalName()));
    NodeList nl = root.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element) {
        Element child = (Element) n;
        NsContext nsContext = new NsContext(child);
        Assert.assertTrue("Incorrect base URI", 
                uriBuilder.getElementUri(child, nsContext).startsWith(child.getNamespaceURI()));
      }
    }
  }
  
  @Test
  public void getUriTestUseDefaultUriBase() throws SAXException, IOException, ParserConfigurationException {
    Document dataDoc;
    dataDoc = parser.parse(
            XmlParserTest.class.getResourceAsStream(
                    "/samplexmls/plant_catalog.xml"), -1);
    Element root = dataDoc.getDocumentElement();
    NsContext rootNsContext = new NsContext(root);
    Assert.assertTrue("Incorrect base URI", 
                uriBuilder.getElementUri(root, rootNsContext).equals(
                        "http://example.org" + "#" + root.getLocalName()));
    Assert.assertTrue(rootNsContext.getNamespaceURI("example").equals("http://example.org"));
    NodeList nl = root.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element) {
        Element child = (Element) n;
        NsContext nsContext = new NsContext(child);
        Assert.assertTrue("Incorrect base URI", 
                uriBuilder.getElementUri(child, nsContext).startsWith("http://example.org"));
        Assert.assertTrue(nsContext.getNamespaceURI("example").equals("http://example.org"));
      }
    }
  }
}
