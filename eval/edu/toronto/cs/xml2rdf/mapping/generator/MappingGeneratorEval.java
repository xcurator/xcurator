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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class MappingGeneratorEval extends TestCase {

    public class Accuracy {

        final private double pr;
        final private double re;

        public Accuracy(double precision, double recall) {
            pr = precision;
            re = recall;
        }

        public double precision() {
            return pr;
        }

        public double recall() {
            return re;
        }

        public double fscore(double beta) {
            return (1 + beta * beta) * ((pr * re) / ((beta * beta * pr) + re));
        }
    }

    public Set<String> getEntities(String inputfile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Set<String> entityList = new HashSet<String>();
        Document doc = XMLUtils.parse(inputfile, -1);

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("/*[local-name()='mapping']/*[local-name()='entity']", doc, XPathConstants.NODESET); // we use this xpath to get rid of namespace
        for (int i = 0; i < nodeList.getLength(); i++) {
            final String nameWithClassPrefix = nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue();
            String className = nameWithClassPrefix.replace("class:", ""); // remove class: from beginning of the string
            entityList.add(className);
        }

        return entityList;
    }

    public Accuracy evaluate(Set<String> result, Set<String> ground) {

        Set<String> intersection = new HashSet<>(result);
        intersection.retainAll(ground);
        System.out.println("result: " + result);
        System.out.println("result#: " + result.size());
        System.out.println("ground: " + ground);
        System.out.println("ground#: " + ground.size());
        System.out.println("intersection: " + intersection);
        System.out.println("intersection#: " + intersection.size());

        double pr = (double) intersection.size() / result.size();
        double re = (double) intersection.size() / ground.size();

        Accuracy ac = new Accuracy(pr, re);

        return ac;
    }

    public void testLoadMapping() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

//        int[] max = new int[]{10, 25, 50, 100, 250, 500, 1000}; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};
        // 10, 25, 50, 100, 250, 500, 1000
//        int[] phase = new int[]{1, 2, 3, 4, 5};
        String inputfile = "xcurator-data\\drugbank\\data\\mappingK.xml";

        Set<String> entitySet = getEntities(inputfile);

//        System.out.println("Entities found: " + grEntityList.size());
//        for (String entity : grEntityList) {
//            System.out.println(entity);
//        }
        String gtInputfile = "xcurator-data\\drugbank\\ground-truth\\classes.csv";
        final String content = FileUtils.readFileToString(new File(gtInputfile));
        List<String> grEntityList = Arrays.asList(content.split("\\r?\\n"));

        Set<String> grEntitySet = new HashSet<>(grEntityList);
//                inputfile = "output/output.ct." + Integer.toString(p) + "." + m + ".xml";

        Accuracy ac = evaluate(entitySet, grEntitySet);

        System.out.println("Precision" + "\t" + "Recall" + "\t" + "F1");
        System.out.println(ac.precision() + "\t" + ac.recall() + "\t" + ac.fscore(1.0));

    }

}
