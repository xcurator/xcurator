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
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.common.UriBuilder;
import edu.toronto.cs.xcurator.common.XmlDocumentBuilder;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class MappingDiscoveryTests {

  private BasicEntitiesDiscovery basicEntitiesDiscovery;
  private SerializeMapping serializeMapping;
  private MappingDiscoverer discoverer;
  private Document dataDoc;
  private XmlParser parser;
  private Mapping mapping;
  private Transformer transformer;
  private UriBuilder uriBuilder;

  @Before
  public void setup() {
    try {
      parser = new XmlParser();
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      uriBuilder = new UriBuilder(
              "http://example.org/resource/class", "http://example.org/resource/property",
              "class", "property");
    } catch (TransformerConfigurationException ex) {
      Logger.getLogger(MappingDiscoveryTests.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Test
  public void test_discoverMapping_clinical_trials() {
    // Setup
    try {
      // Set up the entity discovery step
      basicEntitiesDiscovery = new BasicEntitiesDiscovery(parser, uriBuilder);

      // Set up the mapping serialization step
      serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
              new FileOutputStream("output/clinicaltrials-mapping.xml"), transformer);

      dataDoc = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
              "/clinicaltrials/data/content.xml"), -1);
      mapping = new XmlBasedMapping("http://www.cs.toronto.edu/xcurator", "xcurator");

      discoverer = new MappingDiscoverer(dataDoc, mapping);
    } catch (SAXException | IOException | ParserConfigurationException ex) {
      Logger.getLogger(BasicEntityDiscoveryTest.class.getName()).log(Level.SEVERE, null, ex);
    }

    // Add discovery steps
    discoverer.addStep(basicEntitiesDiscovery).addStep(serializeMapping);

    // Test
    discoverer.discoverMapping();

    // Verify
    Assert.assertTrue(mapping.isInitialized());

    Iterator<Entity> iter = mapping.getEntityIterator();
    while (iter.hasNext()) {
      System.out.println(iter.next().getTypeUri());
    }

    Entity example = mapping.getEntity("http://example.org/resource/class/biospec_descr");
    Assert.assertNotNull(example);
  }

  @Test
  public void test_discoverMapping_fb_XBRL() {
    try {
      // Set up the entity discovery step
      basicEntitiesDiscovery = new BasicEntitiesDiscovery(parser,uriBuilder);

      // Set up the mapping serialization step
      serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
              new FileOutputStream("output/fb-20121231-mapping.xml"), transformer);

      dataDoc = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
              "/secxbrls/data/fb-20121231.xml"), -1);
      mapping = new XmlBasedMapping();

      discoverer = new MappingDiscoverer(dataDoc, mapping);
    } catch (SAXException | IOException | ParserConfigurationException ex) {
      Logger.getLogger(BasicEntityDiscoveryTest.class.getName()).log(Level.SEVERE, null, ex);
    }

    // Add discovery steps
    discoverer.addStep(basicEntitiesDiscovery).addStep(serializeMapping);

    // Test
    discoverer.discoverMapping();

    // Verify
    Assert.assertTrue(mapping.isInitialized());

    Assert.assertNotNull(mapping.getEntity("http://example.org/resource/class/us-gaap-NonoperatingIncomeExpense"));
    Assert.assertNotNull(mapping.getEntity("http://example.org/resource/class/xbrli-segment"));
    Assert.assertNotNull(mapping.getEntity("http://example.org/resource/class/xbrli-period"));
  }

  @Test
  public void test_discoverMapping_multiple_XBRLs() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
    
    // Set up the entity discovery step
    basicEntitiesDiscovery = new BasicEntitiesDiscovery(parser,uriBuilder);

    // Set up the mapping serialization step
    serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
            new FileOutputStream("output/xbrl-mapping.xml"), transformer);

    Document fb2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
            "/secxbrls/data/fb-20131231.xml"), -1);

    Document msft2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
            "/secxbrls/data/msft-20130630.xml"), -1);

    Document goog2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
            "/secxbrls/data/goog-20131231.xml"), -1);

    mapping = new XmlBasedMapping("http://www.cs.toronto.edu/xcurator", "xcurator");

    discoverer = new MappingDiscoverer(mapping);

    discoverer.addDataDocument(new DataDocument(fb2013))
            .addDataDocument(new DataDocument(msft2013))
            .addDataDocument(new DataDocument(goog2013));

    // Add discovery steps
    discoverer.addStep(basicEntitiesDiscovery).addStep(serializeMapping);

    // Test
    discoverer.discoverMapping();

    // Verify
    Assert.assertTrue(mapping.isInitialized());

    Entity example = mapping.getEntity("http://example.org/resource/class/us-gaap-CapitalLeaseObligationsCurrent");
    Assert.assertNotNull(example);
  }

}
