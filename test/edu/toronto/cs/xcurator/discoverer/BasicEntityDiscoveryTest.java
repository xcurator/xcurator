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
package edu.toronto.cs.xcurator.discoverer;

import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class BasicEntityDiscoveryTest {
  
  private BasicEntitiesDiscovery basicEntitiesDiscovery;
  
  private final String defaultUri = "http://cs.toronto.edu/xcurator/items";
  private final String idPattern = "http://edgar.sec.gov/Archives/edgar/data/1326801/000132680113000003/fb-20121231.xml#${UUID}";
  private final String exampleEntityTypeUri = "http://fasb.org/us-gaap/2012-01-31#NonoperatingIncomeExpense";
  private Document dataDoc;
  private XmlParser parser;
  private Mapping mapping;
  
  @Before
  public void setup() {
    try {
      parser = new XmlParser();
      basicEntitiesDiscovery = new BasicEntitiesDiscovery(parser,
              defaultUri, idPattern);
      dataDoc = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
              "/secxbrls/data/fb-20121231.xml"), -1);
      mapping = new XmlBasedMapping("http://www.cs.toronto.edu/xcurator");
    } catch (SAXException | IOException | ParserConfigurationException ex) {
      Logger.getLogger(BasicEntityDiscoveryTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  @Test
  public void test_process() {
    basicEntitiesDiscovery.process(dataDoc, mapping);
    
    Assert.assertTrue(mapping.isInitialized());
    Assert.assertNotNull(mapping.getEntity(exampleEntityTypeUri));
  }
}
