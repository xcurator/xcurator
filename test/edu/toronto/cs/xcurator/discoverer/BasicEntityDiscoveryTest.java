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

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.common.RdfUriBuilder;
import edu.toronto.cs.xcurator.TestConfigs;
import edu.toronto.cs.xcurator.common.XmlParser;
import edu.toronto.cs.xcurator.common.XmlUriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

  private final String exampleEntityTypeUri = "http://fasb.org/us-gaap/2012-01-31/NonoperatingIncomeExpense";
  private Document dataDoc;
  private XmlParser parser;
  private RdfUriBuilder rdfUriBuilder;
  private XmlUriBuilder xmlUriBuilder;
  private Mapping mapping;

  @Before
  public void setup() {
    try {
      parser = new XmlParser();
      rdfUriBuilder = new RdfUriBuilder(TestConfigs.testRdfUriConfig());
      xmlUriBuilder = new XmlUriBuilder();
      basicEntitiesDiscovery = new BasicEntitiesDiscovery(parser, rdfUriBuilder, xmlUriBuilder);
      dataDoc = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
              "/secxbrls/data/fb-20121231.xml"), -1);
      mapping = new XmlBasedMapping("http://www.cs.toronto.edu/xcurator", "xcurator");
    } catch (SAXException | IOException | ParserConfigurationException ex) {
      Logger.getLogger(BasicEntityDiscoveryTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Test
  public void test_process() {
    
    List<DataDocument> dataDocs = new ArrayList<>();
    dataDocs.add(new DataDocument(dataDoc));
    
    basicEntitiesDiscovery.process(dataDocs, mapping);

    Assert.assertTrue(mapping.isInitialized());

    Entity example = mapping.getEntity(exampleEntityTypeUri);
    Assert.assertNotNull(example);
    
    Iterator<Entity> entIterator = mapping.getEntityIterator();
    while (entIterator.hasNext()) {
      Entity e = entIterator.next();
      System.out.println("Entity: " + e.getId() + " path: " + e.getPath());
      Iterator<Attribute> attrIterator = e.getAttributeIterator();
      while (attrIterator.hasNext()) {
        Attribute attr = attrIterator.next();
        System.out.println(attr.getId() + " path: " + attr.getPath());
      }
    }

  }
}
