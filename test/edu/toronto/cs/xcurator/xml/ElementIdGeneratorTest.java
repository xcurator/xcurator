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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author ekzhu
 */
public class ElementIdGeneratorTest {
  
  private ElementIdGenerator idGenerator;
  private final String idPattern = "http://edgar.sec.gov/Archives/edgar/data/1326801/000132680113000003/fb-20121231.xml#${UUID}";
  private XPathFinder xpath;
  private final String examplePath = "/xbrli:xbrl/us-gaap:NonoperatingIncomeExpense";
  private XmlParser parser;
  private Document dataDoc;
  private NsContext nsContext;
  private Element testElement;
  
  @Before
  public void setup() {
    try {
      idGenerator = new ElementIdGenerator();
      xpath = new XPathFinder();
      parser = new XmlParser();
      dataDoc = parser.parse(XPathFinderTest.class.getResourceAsStream(
              "/secxbrls/data/fb-20121231.xml"), -1);
      nsContext = new NsContext(dataDoc.getDocumentElement());
      testElement = (Element) xpath.getNodesByPath(examplePath, null, dataDoc, nsContext)
              .item(0);
    } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException ex) {
      Logger.getLogger(ElementIdGeneratorTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  @Test
  public void test_generateElementId() {
    try {
      String id = idGenerator.generateId(idPattern, nsContext, testElement, dataDoc, xpath);
      System.out.println("The generated ID is: " + id);
      Assert.assertTrue(id.startsWith("http://edgar.sec.gov/Archives/edgar/data/1326801/000132680113000003/fb-20121231.xml#"));
    } catch (NoSuchAlgorithmException | IOException | XPathExpressionException ex) {
      Logger.getLogger(ElementIdGeneratorTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
