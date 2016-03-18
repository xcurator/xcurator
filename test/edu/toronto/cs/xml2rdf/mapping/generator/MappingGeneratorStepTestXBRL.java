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
package edu.toronto.cs.xml2rdf.mapping.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.mapping.generator.MappingGenerator.MappingStep;
import edu.toronto.cs.xml2rdf.string.NoWSCaseInsensitiveStringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class MappingGeneratorStepTestXBRL extends TestCase {

    public void testLoadMapping() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        LogUtils.shutup();

        int[] max = new int[]{4000}; // 10, 25, 50, 100, 250, 500, 1000 }; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};

        final String typePrefix = "http://www.sec.gov#";

        final String inputDataPath = "/xbrl/data/goog-20131231.xml";
//         final String inputDataPath = "/xbrl/data/facebook-new/fb-20151231.xml";

        for (int m : max) {

            System.out.println("\n\n  >> Running experiments for sample size: " + m + " << \n\n");

            long start;
            long end;

            PrintStream nout = new PrintStream("out-xbrl-facebook.tmp");

            Document rootDoc = XMLUtils.attributize(XMLUtils.parse(MappingGeneratorTest.class.getResourceAsStream(inputDataPath), m));
            OutputFormat format = new OutputFormat(rootDoc);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(
                    nout, format);
            //System.out, format);
            serializer.asDOMSerializer();
            serializer.serialize(rootDoc);

            start = System.currentTimeMillis();
            Document doc = new DummyMappingGenerator(1,
                    new NoWSCaseInsensitiveStringMetric(),
                    0.95,
                    new DummySimilarityMetric(),
                    4,
                    0.75,
                    m,
                    1000,
                    .25,
                    2,
                    0.8, MappingStep.BASIC //, MappingStep.DUPLICATE_REMOVAL, MappingStep.INTERlINKING, MappingStep.INTRALINKING, MappingStep.SCHEMA_FLATTENING
            //, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL 
            ).generateMapping(rootDoc.getDocumentElement(), typePrefix);
            end = System.currentTimeMillis();
            System.out.println("Execution time of phase 1 was " + (end - start) + " ms.");

            serializer = new XMLSerializer(
                    new FileOutputStream("output/xbrl/output.xbrl.1." + m + ".xml"), format);
            serializer.asDOMSerializer();
            serializer.serialize(doc);

            rootDoc = XMLUtils.attributize(XMLUtils.parse(MappingGeneratorTest.class.getResourceAsStream(inputDataPath), m));
            serializer = new XMLSerializer(
                    nout, format);
            serializer.asDOMSerializer();
            serializer.serialize(rootDoc);

            start = System.currentTimeMillis();
            doc = new DummyMappingGenerator(1,
                    new NoWSCaseInsensitiveStringMetric(),
                    0.95,
                    new DummySimilarityMetric(),
                    4,
                    0.75,
                    m,
                    500, //1000,
                    .25,
                    2,
                    0.8, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL //, MappingStep.INTERlINKING, MappingStep.INTRALINKING, MappingStep.SCHEMA_FLATTENING
            //, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL 
            ).generateMapping(rootDoc.getDocumentElement(), typePrefix);
            end = System.currentTimeMillis();
            System.out.println("Execution time of phase 2 was " + (end - start) + " ms.");

            serializer = new XMLSerializer(
                    new FileOutputStream("output/xbrl/output.xbrl.2." + m + ".xml"), format);
            serializer.asDOMSerializer();
            serializer.serialize(doc);

            rootDoc = XMLUtils.attributize(XMLUtils.parse(MappingGeneratorTest.class.getResourceAsStream(inputDataPath), m));
            serializer = new XMLSerializer(
                    nout, format);
            serializer.asDOMSerializer();
            serializer.serialize(rootDoc);

            start = System.currentTimeMillis();
            doc = new DummyMappingGenerator(1,
                    new NoWSCaseInsensitiveStringMetric(),
                    0.95,
                    new DummySimilarityMetric(),
                    4,
                    0.75,
                    m,
                    500, //1000,
                    .25,
                    2,
                    0.8, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL, MappingStep.SCHEMA_FLATTENING
            //, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL 
            ).generateMapping(rootDoc.getDocumentElement(), typePrefix);
            end = System.currentTimeMillis();
            System.out.println("Execution time of phase 3 was " + (end - start) + " ms.");

            serializer = new XMLSerializer(
                    new FileOutputStream("output/xbrl/output.xbrl.3." + m + ".xml"), format);
            serializer.asDOMSerializer();
            serializer.serialize(doc);

            rootDoc = XMLUtils.attributize(XMLUtils.parse(MappingGeneratorTest.class.getResourceAsStream(inputDataPath), m));
            serializer = new XMLSerializer(
                    nout, format);
            serializer.asDOMSerializer();
            serializer.serialize(rootDoc);

            start = System.currentTimeMillis();
            doc = new DummyMappingGenerator(1,
                    new NoWSCaseInsensitiveStringMetric(),
                    0.95,
                    new DummySimilarityMetric(),
                    4,
                    0.75,
                    m,
                    500, //1000,
                    .25,
                    2,
                    0.8, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL, MappingStep.INTERLINKING, MappingStep.INTRALINKING
            //, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL 
            ).generateMapping(rootDoc.getDocumentElement(), typePrefix);
            end = System.currentTimeMillis();
            System.out.println("Execution time of phase 4 was " + (end - start) + " ms.");

            serializer = new XMLSerializer(
                    new FileOutputStream("output/xbrl/output.xbrl.4." + m + ".xml"), format);
            serializer.asDOMSerializer();
            serializer.serialize(doc);

            rootDoc = XMLUtils.attributize(XMLUtils.parse(MappingGeneratorTest.class.getResourceAsStream(inputDataPath), m));
            serializer = new XMLSerializer(
                    nout, format);
            serializer.asDOMSerializer();
            serializer.serialize(rootDoc);

            start = System.currentTimeMillis();
            doc = new DummyMappingGenerator(1,
                    new NoWSCaseInsensitiveStringMetric(),
                    0.95,
                    new DummySimilarityMetric(),
                    4,
                    0.75,
                    m,
                    500, //1000,
                    .25,
                    2,
                    0.8, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL, MappingStep.INTERLINKING, MappingStep.INTRALINKING, MappingStep.SCHEMA_FLATTENING
            //, MappingStep.BASIC, MappingStep.DUPLICATE_REMOVAL 
            ).generateMapping(rootDoc.getDocumentElement(), typePrefix);
            end = System.currentTimeMillis();
            System.out.println("Execution time of phase 5 was " + (end - start) + " ms.");

            serializer = new XMLSerializer(
                    new FileOutputStream("output/xbrl/output.xbrl.5." + m + ".xml"), format);
            serializer.asDOMSerializer();
            serializer.serialize(doc);

        }

    }
}
