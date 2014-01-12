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
package edu.toronto.cs.xcurator.generator;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.assertFalse;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.mapping.Mapping;
import edu.toronto.cs.xml2rdf.string.NoWSCaseInsensitiveStringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;
import java.io.File;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Test case for XBRL document Require JUnit 4+
 */
public class MappingGeneratorTestXBRL {

  @Rule
  public TemporaryFolder testTdbFolder = new TemporaryFolder();

  @Test
  public void generateRDFFromXBRL() throws ParserConfigurationException, SAXException,
          IOException, TransformerConfigurationException, TransformerException {
    LogUtils.shutup();

    // Use -1 to process all elements
    int[] max = new int[]{-1}; // 10, 25, 50, 100, 250, 500, 1000 };

    for (int m : max) {

      System.out.println("\n\n  >> Running experiments for sample size: " + m
              + " << \n\n");

      File testTdb = testTdbFolder.newFolder("testTdb");
      String tdbPath = testTdb.getAbsolutePath();

      Document dataDoc = XMLUtils.parse(
              MappingGeneratorTest.class.getResourceAsStream(
                      "/secxbrls/data/fb-20121231.xml"), m);
      Document rootDoc = XMLUtils.attributize(dataDoc);

      // Output attributized document
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(rootDoc), 
              new StreamResult(new PrintStream("out.tmp")));

      // Create a mapping generator
      MappingGenerator mg = new MappingGenerator();

      // Adding mapping steps
      // Schema Extraction
      mg.addStep(new BasicSchemaExtraction(m));

      // Generate a document
      Document mappingDoc = mg.generateMapping(rootDoc.getDocumentElement(),
              "http://www.sec.gov#");

      // Output the mapping file
      transformer.transform(new DOMSource(mappingDoc),
              new StreamResult(new File("output/output.ct.1." + m + ".xml")));

      // Generate RDF
      dataDoc = XMLUtils.addRoot(dataDoc, "testroot");
      String typePrefix = "http://facebook.com#";
      Mapping mapping = new Mapping(mappingDoc, new HashSet<String>());
      try {
        mapping.generateRDFs(tdbPath, dataDoc, typePrefix, null, "RDF/XML-ABBREV",
                new NoWSCaseInsensitiveStringMetric(), 1);
      } catch (XPathExpressionException ex) {
        Logger.getLogger(MappingGeneratorTest.class.getName()).log(Level.SEVERE, null, ex);
      }

      // Verify
      Model model = JenaUtils.getTDBModel(tdbPath);
      assertFalse("No RDF was generated. TDB directory: " + tdbPath, model.isEmpty());
    }
  }
}
