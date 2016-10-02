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

import edu.toronto.cs.xcurator.utils.IOUtils;
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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

public class MappingGeneratorEval extends TestCase {

    private static DecimalFormat df = new DecimalFormat("###.####");

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

    public Set<String> getAttributes(String inputfile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Set<String> entityList = new HashSet<String>();
        Document doc = XMLUtils.parse(inputfile, -1);

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("/*[local-name()='mapping']/*[local-name()='entity']/*[local-name()='attribute']", doc, XPathConstants.NODESET); // we use this xpath to get rid of namespace
        for (int i = 0; i < nodeList.getLength(); i++) {
            final String nameWithClassPrefix = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            String className = nameWithClassPrefix.replace("property:", ""); // remove class: from beginning of the string
            entityList.add(className);
        }
        return entityList;
    }

    public Accuracy evaluate(Set<String> result, Set<String> ground) {

        Set<String> intersection = new HashSet<>(result);
        intersection.retainAll(ground);
        System.out.println("result:\n " + result);
        System.out.println("size: " + result.size());
        System.out.println();
        System.out.println("ground:\n " + ground);
        System.out.println("size: " + ground.size());
        System.out.println();
        System.out.println("intersection:\n " + intersection);
        System.out.println("size: " + intersection.size());
        System.out.println();

        double pr = (double) intersection.size() / result.size();
        double re = (double) intersection.size() / ground.size();

        Accuracy ac = new Accuracy(pr, re);

        return ac;
    }

    @Ignore("we will use entity with attribute accuracy instead.")
    @Test
    public void testAccuracyforEntities() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

//        int[] max = new int[]{10, 25, 50, 100, 250, 500, 1000}; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};
        // 10, 25, 50, 100, 250, 500, 1000
//        int[] phase = new int[]{1, 2, 3, 4, 5};
        String inputfile = "xcurator-data\\drugbank\\data\\mapping-sm.xml";

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
        printAccuracyStats(entitySet, grEntitySet);
    }

    @Test
    public void testAccuracyforEntitiesAndAtrributes() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

//        int[] max = new int[]{10, 25, 50, 100, 250, 500, 1000}; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};
        // 10, 25, 50, 100, 250, 500, 1000
//        int[] phase = new int[]{1, 2, 3, 4, 5};
        String root = "xcurator-data\\interpro\\";
        String inputfile = root + "mapping-KIG.xml";

        Set<String> entitySet = getEntities(inputfile);
        Set<String> attributeSet = getAttributes(inputfile);

//        System.out.println("Entities found: " + grEntityList.size());
//        for (String entity : grEntityList) {
//            System.out.println(entity);
//        }
        String gtEntityInputfile = root + "ents.txt";
        String gtAttributeInputfile = root + "attrs.txt";
        Set<String> grEntitySet = new HashSet<>(IOUtils.readFileLineByLine(gtEntityInputfile));
        Set<String> grAttributesSet = new HashSet<>(IOUtils.readFileLineByLine(gtAttributeInputfile));

        System.out.println("ENTITIES:");
        printAccuracyStats(entitySet, grEntitySet);

        System.out.println("ATTRIBUTES:");
        printAccuracyStats(attributeSet, grAttributesSet);

        System.out.println();
        System.out.println("MISSED:");
        Set<String> grUnion = new HashSet<>(grEntitySet);
        grUnion.addAll(grAttributesSet);

        Set<String> union = new HashSet<>(entitySet);
        union.addAll(attributeSet);

        Set onlyInResult = new HashSet<>(union);
        onlyInResult.removeAll(grUnion);
        System.out.println("Only In Result (both entities & attributes):\n" + onlyInResult);
        System.out.println("size: " + onlyInResult.size());

        Set onlyInGr = new HashSet<>(grUnion);
        onlyInGr.removeAll(union);
        System.out.println();
        System.out.println("Only In GroundTruth (both entities & attributes):\n" + onlyInGr);
        System.out.println("size: " + onlyInGr.size());

        System.out.println();

        Set wrongAttributes = new HashSet<>(grAttributesSet);
        wrongAttributes.retainAll(entitySet);
        System.out.println();
        System.out.println("Attributes that recognized as entity:\n" + wrongAttributes);
        System.out.println("size: " + wrongAttributes.size());

        System.out.println();

        Set wrongEntities = new HashSet<>(grEntitySet);
        wrongEntities.retainAll(attributeSet);
        System.out.println();
        System.out.println("Entities that recognized as attribute:\n" + wrongEntities);
        System.out.println("size: " + wrongEntities.size());

        System.out.println();
    }

    private void printAccuracyStats(Set<String> attributeSet, Set<String> grAttributesSet) {
        Accuracy acAttr = evaluate(attributeSet, grAttributesSet);
        final String P = df.format(acAttr.precision());
        System.out.println("Prec:" + "\t" + P);
        final String R = df.format(acAttr.recall());
        System.out.println("Recall:" + "\t" + R);
        final String F1 = df.format(acAttr.fscore(1.0));
        System.out.println("F1:" + "\t" + F1);
        System.out.println(P + "\t" + R + "\t" + F1);
    }

}
