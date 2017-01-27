/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.eval;

import edu.toronto.cs.xcurator.utils.IOUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author amir
 */
public class EvalUtil {

    private static DecimalFormat df = new DecimalFormat("###.####");

    public static Set<String> getEntities(String inputfile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
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

    public static Set<String> getAttributes(String inputfile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
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

    public static Accuracy evaluate(Set<String> result, Set<String> ground, boolean verbose) {

        Set<String> intersection = new HashSet<>(result);
        intersection.retainAll(ground);
        if (verbose) {
            System.out.println("result:\n " + result);
            System.out.println("size: " + result.size());
            System.out.println();
            System.out.println("ground:\n " + ground);
            System.out.println("size: " + ground.size());
            System.out.println();
            System.out.println("intersection:\n " + intersection);
            System.out.println("size: " + intersection.size());
            System.out.println();
        }

        double pr = (double) intersection.size() / result.size();
        double re = (double) intersection.size() / ground.size();

        Accuracy ac = new Accuracy(pr, re);

        return ac;
    }

    private static void printAccuracyStats(Set<String> attributeSet, Set<String> grAttributesSet, boolean verbose) {
        Accuracy acAttr = evaluate(attributeSet, grAttributesSet, verbose);
        final String P = df.format(acAttr.precision());
        System.out.println("Prec:" + "\t" + P);
        final String R = df.format(acAttr.recall());
        System.out.println("Recall:" + "\t" + R);
        final String F1 = df.format(acAttr.fscore(1.0));
        System.out.println("F1:" + "\t" + F1);
        System.out.println(P + "\t" + R + "\t" + F1);
    }

    public static Set<String> readAttrEntFile(String filename) {
        List<String> lines = IOUtils.readFileLineByLine(filename);
        Set<String> set = new HashSet<>();
        for (String l : lines) {
            String[] split = l.split("\\t");
            if (split.length == 2) {
                set.add(split[1]);
            } else if (split.length == 1) {
                set.add(split[0]);
            } else {
                throw new RuntimeException(filename + " malformat.");
            }
        }
        return set;
    }

    public static void genAccuracyforEntitiesAndAtrributes(String mappingFile, String entityFile, String attributeFile, boolean verbose) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        //        int[] max = new int[]{10, 25, 50, 100, 250, 500, 1000}; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};
        // 10, 25, 50, 100, 250, 500, 1000
//        int[] phase = new int[]{1, 2, 3, 4, 5};

        Set<String> entitySet = getEntities(mappingFile);
        Set<String> attributeSet = getAttributes(mappingFile);

//        System.out.println("Entities found: " + grEntityList.size());
//        for (String entity : grEntityList) {
//            System.out.println(entity);
//        }
        Set<String> grEntitySet = readAttrEntFile(entityFile);
        Set<String> grAttributesSet = readAttrEntFile(attributeFile);

        System.out.println("mapping file:" + mappingFile);
        System.out.println("ENTITIES:");
        printAccuracyStats(entitySet, grEntitySet, verbose);

        System.out.println("ATTRIBUTES:");
        printAccuracyStats(attributeSet, grAttributesSet, verbose);

        System.out.println();
        if (verbose) {
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
    }
}
