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
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.xml.ElementIdGenerator;
import edu.toronto.cs.xcurator.xml.XPathFinder;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.File;
import java.io.IOException;
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
    File testTdb = testTdbFolder.newFolder("testTdb");
    testTdbDir = testTdb.getAbsolutePath();
    rdfGeneration = new RdfGeneration(testTdbDir, new XPathFinder(),
            new ElementIdGenerator());
    
    parser = new XmlParser();
  }
  
  @Test
  public void test_generateRdfs_clinical_trials() throws SAXException, IOException, ParserConfigurationException {
    // Setup deserializer
    mappingDeserialization = new XmlBasedMappingDeserialization(
            RdfGeneratorTest.class.getResourceAsStream(
                    "/clinicaltrials/mapping/clinicaltrials-mapping.xml"), parser);
    
    Document dataDocument = parser.parse(RdfGeneratorTest.class.getResourceAsStream(
            "/clinicaltrials/data/content.xml"), 10);
    rdfGenerator = new RdfGenerator(dataDocument, new XmlBasedMapping());
    
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
  public void test_generateRdfs_fb_XBRL() throws SAXException, IOException, ParserConfigurationException {
    // Setup deserializer
    mappingDeserialization = new XmlBasedMappingDeserialization(
            RdfGeneratorTest.class.getResourceAsStream(
                    "/secxbrls/mapping/fb-20121231-mapping.xml"), parser);
    
    Document dataDocument = parser.parse(RdfGeneratorTest.class.getResourceAsStream(
            "/secxbrls/data/fb-20121231.xml"), -1);
    rdfGenerator = new RdfGenerator(dataDocument, new XmlBasedMapping());
    
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
}
