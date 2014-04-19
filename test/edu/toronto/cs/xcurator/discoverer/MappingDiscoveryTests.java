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

import edu.toronto.cs.xcurator.TestConfigs;
import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.common.RdfUriBuilder;
import edu.toronto.cs.xcurator.common.XPathFinder;
import edu.toronto.cs.xcurator.common.XmlDocumentBuilder;
import edu.toronto.cs.xcurator.common.XmlParser;
import edu.toronto.cs.xcurator.common.XmlUriBuilder;
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
import javax.xml.xpath.XPathExpressionException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class MappingDiscoveryTests {

  private BasicEntityDiscovery basicEntitiesDiscovery;
  private SerializeMapping serializeMapping;
  private MappingDiscoverer discoverer;
  private Document dataDoc;
  private XmlParser parser;
  private Mapping mapping;
  private Transformer transformer;
  private RdfUriBuilder rdfUriBuilder;
  private XmlUriBuilder xmlUriBuilder;

  @Before
  public void setup() {
    try {
      parser = new XmlParser();
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      rdfUriBuilder = new RdfUriBuilder(TestConfigs.testRdfUriConfig());
      xmlUriBuilder = new XmlUriBuilder();
    } catch (TransformerConfigurationException ex) {
      Logger.getLogger(MappingDiscoveryTests.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Test
  public void test_discoverMapping_clinical_trials() {
    // Setup
    try {
      // Set up the entity discovery step
      basicEntitiesDiscovery = new BasicEntityDiscovery(parser, rdfUriBuilder, xmlUriBuilder);

      // Set up the mapping serialization step
      serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
              new FileOutputStream("output/clinicaltrials-mapping.xml"), transformer,
              TestConfigs.testRdfUriConfig());

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
      System.out.println(iter.next().getId());
    }

    Entity example = mapping.getEntity("biospec_descr");
    Assert.assertNotNull(example);
  }

  @Test
  public void test_discoverMapping_fb_XBRL() {
    try {
      // Set up the entity discovery step
      basicEntitiesDiscovery = new BasicEntityDiscovery(parser, rdfUriBuilder, xmlUriBuilder);

      // Set up the mapping serialization step
      serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
              new FileOutputStream("output/fb-20121231-mapping.xml"), transformer,
              TestConfigs.testRdfUriConfig());

      dataDoc = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
              "/secxbrls/data/fb-20121231.xml"), -1);
      mapping = new XmlBasedMapping();

      discoverer = new MappingDiscoverer(dataDoc, mapping);
    } catch (SAXException | IOException | ParserConfigurationException ex) {
      Logger.getLogger(BasicEntityDiscoveryTest.class.getName()).log(Level.SEVERE, null, ex);
    }

    // Add discovery steps
    discoverer.addStep(basicEntitiesDiscovery)
            .addStep(new KeyAttributeDiscovery())
            .addStep(new HashBasedEntityInterlinking(rdfUriBuilder))
            .addStep(serializeMapping);

    // Test
    discoverer.discoverMapping();

    // Verify
    Assert.assertTrue(mapping.isInitialized());

    Assert.assertNotNull(mapping.getEntity("http://fasb.org/us-gaap/2012-01-31/NonoperatingIncomeExpense"));
    Assert.assertNotNull(mapping.getEntity("http://www.xbrl.org/2003/instance/segment"));
    Assert.assertNotNull(mapping.getEntity("http://www.xbrl.org/2003/instance/period"));
  }

  @Test
  public void test_discoverMapping_multiple_XBRLs() throws FileNotFoundException,
          SAXException, IOException, ParserConfigurationException, XPathExpressionException {

    // Set up the entity discovery step
    basicEntitiesDiscovery = new BasicEntityDiscovery(parser, rdfUriBuilder, xmlUriBuilder);

    // Set up the mapping serialization step
    serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
            new FileOutputStream("output/xbrl-mapping.xml"), transformer,
            TestConfigs.testRdfUriConfig());

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

    Entity example = mapping.getEntity("http://fasb.org/us-gaap/2013-01-31/NonoperatingIncomeExpense");
    Assert.assertNotNull(example);
    XPathFinder xpath = new XPathFinder();
    NodeList nl = xpath.getNodesByPath(example.getPath(), msft2013, example.getNamespaceContext());
    Assert.assertTrue(nl.getLength() > 0);
  }

  @Test
  public void test_discoverMapping_XBRL_msft() throws FileNotFoundException,
          SAXException, IOException, ParserConfigurationException, XPathExpressionException {

    // Set up the entity discovery step
    basicEntitiesDiscovery = new BasicEntityDiscovery(parser, rdfUriBuilder, xmlUriBuilder);

    // Set up the mapping serialization step
    serializeMapping = new SerializeMapping(new XmlDocumentBuilder(),
            new FileOutputStream("output/msft-20130630-mapping.xml"), transformer,
            TestConfigs.testRdfUriConfig());

    Document msft2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
            "/secxbrls/data/msft-20130630.xml"), -1);

    mapping = new XmlBasedMapping("http://www.cs.toronto.edu/xcurator", "xcurator");

    discoverer = new MappingDiscoverer(mapping);

    discoverer.addDataDocument(new DataDocument(msft2013));

    // Add discovery steps
    discoverer.addStep(basicEntitiesDiscovery)
            .addStep(new KeyAttributeDiscovery())
            .addStep(new HashBasedEntityInterlinking(rdfUriBuilder))
            .addStep(serializeMapping);

    // Test
    discoverer.discoverMapping();

    // Verify
    Assert.assertTrue(mapping.isInitialized());
    Assert.assertNotNull(mapping.getEntity("http://fasb.org/us-gaap/2013-01-31/NonoperatingIncomeExpense"));
    Assert.assertNotNull(mapping.getEntity("http://www.xbrl.org/2003/instance/segment"));
    Assert.assertNotNull(mapping.getEntity("http://www.xbrl.org/2003/instance/period"));

    Entity example = mapping.getEntity("http://fasb.org/us-gaap/2013-01-31/NonoperatingIncomeExpense");
    Assert.assertNotNull(example);
    XPathFinder xpath = new XPathFinder();
    NodeList nl = xpath.getNodesByPath(example.getPath(), msft2013, example.getNamespaceContext());
    Assert.assertTrue(nl.getLength() > 0);
  }

}
