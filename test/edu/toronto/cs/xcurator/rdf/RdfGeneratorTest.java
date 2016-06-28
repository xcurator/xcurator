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
package edu.toronto.cs.xcurator.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.TestConfigs;
import edu.toronto.cs.xcurator.discoverer.BasicEntityDiscoveryTest;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class RdfGeneratorTest {

    private RdfGenerator rdfGenerator;
    private RdfGeneration rdfGeneration;
    private XmlBasedMappingDeserialization mappingDeserialization;
    private String testTdbDir;
    private XmlParser parser;

    @Rule
    public TemporaryFolder testTdbFolder = new TemporaryFolder();

    @Before
    public void setup() {
        // Use temporary directory for setting up testing TDB
        File testTdb = null;
        try {
            testTdb = testTdbFolder.newFolder("testTdb");
        } catch (IOException ex) {
            Logger.getLogger(RdfGeneratorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        testTdbDir = testTdb.getAbsolutePath();
        rdfGeneration = new RdfGeneration(testTdbDir, TestConfigs.testRdfUriConfig());
        parser = new XmlParser();
    }

    /**
     * Run the RDF generator pipeline for clinical trial data Before running
     * this, run the Mapping Discovery Test first to generate the mapping file
     * for clinical trials.
     *
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    @Test
    public void test_generateRdfs_clinical_trials() throws SAXException, IOException, ParserConfigurationException {
        // Setup deserializer
        mappingDeserialization = new XmlBasedMappingDeserialization(
                new FileInputStream("output/clinicaltrials-mapping.xml"), parser);

        Document dataDocument = parser.parse(RdfGeneratorTest.class.getResourceAsStream(
                "/clinicaltrials/data/content.xml"), 10);
        rdfGenerator = new RdfGenerator(new DataDocument(dataDocument), new XmlBasedMapping());

        // Add steps
        rdfGenerator.addStep(mappingDeserialization);
        rdfGenerator.addStep(rdfGeneration);

        // Generate
        rdfGenerator.generateRdfs();

        // Verify
        Model model = TDBFactory.createModel(testTdbDir);
        Assert.assertFalse("No RDF was generated. TDB directory: " + testTdbDir, model.isEmpty());

        ResIterator iter = model.listResourcesWithProperty(RDF.type);
        while (iter.hasNext()) {
            Resource resource = iter.nextResource();
            System.out.println(resource.getLocalName());
            StmtIterator iterStm = resource.listProperties();
            while (iterStm.hasNext()) {
                System.out.println(iterStm.nextStatement().toString());
            }
        }
    }

    @Test
    // Run test_discoverMapping_fb_XBRL to generate the mapping file before running
    // this test.
    public void test_generateRdfs_fb_XBRL() throws SAXException, IOException, ParserConfigurationException {
        // Setup deserializer
        mappingDeserialization = new XmlBasedMappingDeserialization(
                new FileInputStream("output/fb-20121231-mapping.xml"), parser);

        Document dataDocument = parser.parse(RdfGeneratorTest.class.getResourceAsStream(
                "/secxbrls/data/fb-20121231.xml"), -1);
        rdfGenerator = new RdfGenerator(new DataDocument(dataDocument), new XmlBasedMapping());

        // Add steps
        rdfGenerator.addStep(mappingDeserialization);
        rdfGenerator.addStep(rdfGeneration);

        // Generate
        rdfGenerator.generateRdfs();

        // Verify
        Model model = TDBFactory.createModel(testTdbDir);
        Assert.assertFalse("No RDF was generated. TDB directory: " + testTdbDir, model.isEmpty());

        Resource r = model.getResource("http://example.org/resource/class/unitNumerator");
        // Failing, investigate
        Assert.assertTrue(r.hasProperty(model.getProperty("http://example.org/resource/property/measure")));

//    ResIterator iter = model.listResourcesWithProperty(RDF.type);
//    while (iter.hasNext()) {
//      Resource resource = iter.nextResource();
//      System.out.println(resource.getLocalName());
//      StmtIterator iterStm = resource.listProperties();
//      while (iterStm.hasNext()) {
//        System.out.println(iterStm.nextStatement().toString());
//      }
//    }
    }

    @Test
    // Run test_discoverMapping_XBRL_msft to generate the mapping file before running
    // this test.
    public void test_generateRdfs_msft_XBRL() throws SAXException, IOException, ParserConfigurationException {
        // Setup deserializer
        mappingDeserialization = new XmlBasedMappingDeserialization(
                new FileInputStream("output/msft-20130630-mapping.xml"), parser);

        Document dataDocument = parser.parse(RdfGeneratorTest.class.getResourceAsStream(
                "/secxbrls/data/msft-20130630.xml"), -1);
        rdfGenerator = new RdfGenerator(new DataDocument(dataDocument), new XmlBasedMapping());

        // Add steps
        rdfGenerator.addStep(mappingDeserialization);
        rdfGenerator.addStep(rdfGeneration);

        // Generate
        rdfGenerator.generateRdfs();

        // Verify
        Model model = TDBFactory.createModel(testTdbDir);
        Assert.assertFalse("No RDF was generated. TDB directory: " + testTdbDir, model.isEmpty());

        Resource r = model.getResource("http://example.org/resource/class/unitNumerator");
        // Failing, investigate
        Assert.assertTrue(r.hasProperty(model.getProperty("http://example.org/resource/property/measure")));

//    ResIterator iter = model.listResourcesWithProperty(RDF.type);
//    while (iter.hasNext()) {
//      Resource resource = iter.nextResource();
//      System.out.println(resource.getLocalName());
//      StmtIterator iterStm = resource.listProperties();
//      while (iterStm.hasNext()) {
//        System.out.println(iterStm.nextStatement().toString());
//      }
//    }
    }

    @Test
    // Run test_discoverMapping_multiple_XBRLs to generate the mapping file before running
    // this test.
    public void test_generateRdfs_multiple_XBRLs() throws SAXException, IOException, ParserConfigurationException {
        // Setup deserializer
        mappingDeserialization = new XmlBasedMappingDeserialization(
                new FileInputStream("output/xbrl-mapping.xml"), parser);

        Document fb2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
                "/secxbrls/data/fb-20131231.xml"), -1);

        Document msft2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
                "/secxbrls/data/msft-20130630.xml"), -1);

        Document goog2013 = parser.parse(BasicEntityDiscoveryTest.class.getResourceAsStream(
                "/secxbrls/data/goog-20131231.xml"), -1);

        rdfGenerator = new RdfGenerator(new XmlBasedMapping());

        // Add document and steps
        rdfGenerator.addDataDocument(new DataDocument(fb2013, "http://example.org/resource/fb-20131231"))
                .addDataDocument(new DataDocument(msft2013, "http://example.org/resource/msft-20130630"))
                .addDataDocument(new DataDocument(goog2013, "http://example.org/resource/goog-20131231"))
                .addStep(mappingDeserialization)
                .addStep(rdfGeneration);

        // Generate
        rdfGenerator.generateRdfs();

        // Verify
        Model model = TDBFactory.createModel(testTdbDir);
        Assert.assertFalse("No RDF was generated. TDB directory: " + testTdbDir, model.isEmpty());

        ResIterator iter = model.listResourcesWithProperty(RDF.type);
        while (iter.hasNext()) {
            Resource resource = iter.nextResource();
            System.out.println(resource.getLocalName());
            StmtIterator iterStm = resource.listProperties();
            while (iterStm.hasNext()) {
                System.out.println(iterStm.nextStatement().toString());
            }
        }

    }
}
