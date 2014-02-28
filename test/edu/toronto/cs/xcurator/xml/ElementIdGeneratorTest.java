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
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ekzhu
 */
public class ElementIdGeneratorTest {

  private ElementIdGenerator idGenerator;
  private final String uriBase = "http://test/entity";
  private XPathFinder xpath;
  private final String examplePath = "/xbrli:xbrl/us-gaap:NonoperatingIncomeExpense";
  private final String xbrli_identifier = "/xbrli:xbrl/xbrli:context/xbrli:entity/xbrli:identifier";
  private final String xbrli_explicitMember = "/xbrli:xbrl/xbrli:context/xbrli:entity/xbrli:segment/xbrldi:explicitMember";
  private XmlParser parser;
  private Document dataDoc;
  private NsContext nsContext;

  @Before
  public void setup() throws SAXException, IOException, ParserConfigurationException {
    idGenerator = new ElementIdGenerator(uriBase);
    xpath = new XPathFinder();
    parser = new XmlParser();
    dataDoc = parser.parse(XPathFinderTest.class.getResourceAsStream(
            "/secxbrls/data/fb-20131231.xml"), -1);
    nsContext = new NsContext(dataDoc.getDocumentElement());
  }

  @Test
  /**
   * The generator should generate the same ID for identical XML elements
   */
  public void test_generatedElementIdSameForIdenticalElements() throws XPathExpressionException, NoSuchAlgorithmException, IOException {
    NodeList nl = xpath.getNodesByPath(xbrli_identifier, null, dataDoc, nsContext);
    Element first = (Element) nl.item(0);
    String id_first = idGenerator.generateId( nsContext, first, dataDoc, xpath);
    for (int i = 0; i < nl.getLength(); i++) {
      String id = idGenerator.generateId(nsContext, (Element) nl.item(i), dataDoc, xpath);
      Assert.assertTrue(id_first.equals(id));
    }
  }
  
  @Test
  /**
   * The generator should generate different IDs for different XML elements
   */
  public void test_generatedElementIdDifferentForDifferElements() throws XPathExpressionException, NoSuchAlgorithmException, IOException {
    NodeList nl = xpath.getNodesByPath(xbrli_explicitMember, null, dataDoc, nsContext);
    Element first = (Element) nl.item(0);
    String id_first = idGenerator.generateId( nsContext, first, dataDoc, xpath);
    String dim_first = first.getAttribute("dimension");
    String text_first = first.getTextContent().trim();
    System.out.println(id_first);
    System.out.println("The first one: \n" + first.toString().trim());
    for (int i = 0; i < nl.getLength(); i++) {
      Element e = (Element) nl.item(i);
      String id = idGenerator.generateId(nsContext, e, dataDoc, xpath);
      if (e.getAttribute("dimension").equals(dim_first) &&
              e.getTextContent().trim().equals(text_first)) {
        System.out.println("This ID must be the same as the first: " + id);
        System.out.println("The first one must be the same as this: \n" + e.toString().trim());
        Assert.assertTrue(id_first.equals(id));
      }
      else {
        Assert.assertTrue(!id_first.equals(id));
      }
    }
  }

  @Test
  public void test_generateElementId() throws XPathExpressionException, NoSuchAlgorithmException, IOException {
    Element testElement = (Element) xpath.getNodesByPath(examplePath, null, dataDoc, nsContext)
            .item(0);
    String id = idGenerator.generateId(nsContext, testElement, dataDoc, xpath);
    System.out.println("The generated ID is: " + id);
    Assert.assertTrue(id.startsWith("http://test/entity/"));
  }
}
