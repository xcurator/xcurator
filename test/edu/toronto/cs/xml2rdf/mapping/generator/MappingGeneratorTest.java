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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.string.NoWSCaseInsensitiveStringMetric;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class MappingGeneratorTest extends TestCase{
  public void testLoadMapping() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

    int[] max = new int[] {/*5, 10, 20, 40, 50, 100, 125, 250, 500,*/ 1000, 2000};

    for (int m: max) {
      Document rootDoc = XMLUtils.attributize(XMLUtils.parse(new FileInputStream("/home/soheil/Archive/LinkedData/smallpruneddblp.xml"), m));
      OutputFormat format = new OutputFormat(rootDoc);
      format.setLineWidth(65);
      format.setIndenting(true);
      format.setIndent(2);
      XMLSerializer serializer = new XMLSerializer (
          System.out, format);
      serializer.asDOMSerializer();
      serializer.serialize(rootDoc);
      
      
      Document doc = new DummyMappingGenerator(1, new NoWSCaseInsensitiveStringMetric(), 0.95, new DummySimilarityMetric(), 4, 0.75, m, 1000, .25, 2, .8).generateMapping(rootDoc.getDocumentElement(), "http://www.linkedct.org/0.1#");

      serializer = new XMLSerializer (
          new FileOutputStream("output.dblp." + m + ".xml"), format);
      serializer.asDOMSerializer();
      serializer.serialize(doc);
    }

  }
}
