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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xeustechnologies.googleapi.spelling.SpellChecker;
import org.xeustechnologies.googleapi.spelling.SpellResponse;
import org.xml.sax.SAXException;

import edu.toronto.cs.xcurator.generator.BasicSchemaExtraction;
import edu.toronto.cs.xcurator.generator.MappingGenerator;
import edu.toronto.cs.xcurator.generator.BasicSimilarityMetric;
import edu.toronto.cs.xcurator.parser.PatentParser;
import edu.toronto.cs.xml2rdf.string.NoWSCaseInsensitiveStringMetric;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

/**
 * The main test case for xCurator v2.0.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class MappingGeneratorTest extends TestCase {
  public void testBasicStep() throws ParserConfigurationException, SAXException,
      IOException {
    LogUtils.shutup();

    int[] max = new int[] { 100 }; // 10, 25, 50, 100, 250, 500, 1000 };

    for (int m: max) {

      System.out.println("\n\n  >> Running experiments for sample size: " + m +
          " << \n\n");

      PrintStream nout = new PrintStream("out.tmp");
      
      // Parse Patent XML File
      // String rawDir = "patents\\raw";
      // String parsedDir = "patents\\parsed";
      // String fileName = "ipg130101.xml";
      // Create the Patent Parser and parse raw patent XML file
      // PatentParser pp = new PatentParser();
      // pp.parse(rawDir, parsedDir, fileName);
      // Attributize patent file
      // Document rootDoc = XMLUtils.attributize(XMLUtils.parse(
      //		MappingGeneratorTest.class.getResourceAsStream(
      //				"/patents/parsed/" + fileName), m));
      
      // Attributize CT file
      Document rootDoc = XMLUtils.attributize(XMLUtils.parse(
          MappingGeneratorTest.class.getResourceAsStream(
              "/clinicaltrials/data/content.xml"), m));
      OutputFormat format = new OutputFormat(rootDoc);
      format.setLineWidth(65);
      format.setIndenting(true);
      format.setIndent(2);
      XMLSerializer serializer = new XMLSerializer (nout, format);
      serializer.asDOMSerializer();
      serializer.serialize(rootDoc);
      
      // Create a mapping generator
      MappingGenerator mg = new MappingGenerator();
      
      // Adding mapping steps
      
      // 1. Schema Extraction
      mg.addStep(new BasicSchemaExtraction(m));
      
      // 2. OntologyLink Addition
      int maxOnotlogyLookup = 1000;
    	int leafPromotionThreshold = 5;
    	double matchThreshold = 0.75;
    	double digitThreshold = 0.25;
    	double ontologyMatchingThreshold = 1;
    	StringMetric stringMetric = new NoWSCaseInsensitiveStringMetric();
    	mg.addStep(new BasicOntologyLinkAddition(maxOnotlogyLookup,
    			leafPromotionThreshold, digitThreshold, matchThreshold,
    			ontologyMatchingThreshold, stringMetric));
    	
      // 3. Schema Flattening
      mg.addStep(new BasicSchemaFlattening());
      
      // 4. Duplicate Removal
      BasicSimilarityMetric dsm = new BasicSimilarityMetric();
      double schemaSimThreshold = 0.95;
      int minimumNumberOfAttributeToMerges = 2;
      mg.addStep(new BasicDuplicateRemoval(schemaSimThreshold, minimumNumberOfAttributeToMerges, dsm));
      
      // 5. Key Identification
      double uniqunessThreshold = 1.0d; // meaning must be exactly unique
      mg.addStep(new BasicKeyIdentification(uniqunessThreshold));
      
      // 6. Intra-linking
      double intralinkingThreshold = 0.8d;
      mg.addStep(new BasicSchemaIntralinking(intralinkingThreshold));
      
      // Generate a document
      Document doc = mg.generateMapping(rootDoc.getDocumentElement(),
      		"http://www.linkedct.org/0.1#");

      serializer = new XMLSerializer(
          new FileOutputStream("output/output.ct.1." + m + ".xml"), format);
      serializer.asDOMSerializer();
      serializer.serialize(doc);
    }
  }
}
