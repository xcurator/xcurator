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

import edu.toronto.cs.xcurator.common.XPathFinder;
import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ekzhu
 */
public class XPathFinderTest {

  private XPathFinder finder;
  private final String examplePath = "/xbrli:xbrl/us-gaap:NonoperatingIncomeExpense";
  private XmlParser parser;
  private Document mappingDoc;
  private Document dataDoc;
  private NsContext nsContext;

  @Before
  public void setup() {
    try {
      finder = new XPathFinder();
      parser = new XmlParser();
      mappingDoc = parser.parse(XPathFinderTest.class.getResourceAsStream(
              "/secxbrls/mapping/fb-20121231-mapping.xml"), -1);
      dataDoc = parser.parse(XPathFinderTest.class.getResourceAsStream(
              "/secxbrls/data/fb-20121231.xml"), -1);
      nsContext = new NsContext(mappingDoc.getDocumentElement());

    } catch (SAXException | IOException | ParserConfigurationException ex) {
      Logger.getLogger(XPathFinderTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Test
  public void test_getNodesByPath() throws XPathExpressionException {
    // Test
    NodeList nl = finder.getNodesByPath(examplePath, null, dataDoc, nsContext);

    Assert.assertTrue(nl.getLength() > 0);
  }
}
