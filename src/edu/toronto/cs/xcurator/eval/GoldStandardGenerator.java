/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.eval;

import edu.toronto.cs.xcurator.utils.StrUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.semanticweb.yars.nx.parser.NxParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Amir
 */
public class GoldStandardGenerator {

    private static DocumentBuilder builder;
    Map<String, String> xmlTags;

    public GoldStandardGenerator() {
        xmlTags = new HashMap<>();
        setupDocumentBuilder();
    }
    static String xmlfile = "D:\\workspace\\bio2rdf-data\\data\\download\\drugbank\\drugbank.org.xml";
    static String xmlout = "D:\\workspace\\bio2rdf-data\\data\\download\\drugbank\\drugbank.xml";
//    static String xmlout = "D:\\workspace\\bio2rdf-data\\data\\download\\drugbank\\old\\drugbank.xml";
    static String rdf = "D:\\workspace\\bio2rdf-data\\data\\rdf\\drugbank\\drugbank.nq";
//    static String rdf = "D:\\workspace\\bio2rdf-data\\data\\rdf\\drugbank\\original\\drugbank.nq";

    public static void generateXML() {
        GoldStandardGenerator gs = new GoldStandardGenerator();
        gs.generateXMLUniqueValueFile(xmlfile, xmlout);
    }

    public static void main(String[] args) {
        GoldStandardGenerator gs = new GoldStandardGenerator();
        //        List<RDFEnt> result = gs.readRDFFile(rdf);
        //        System.out.println(result.size());
        //        System.out.println(result);
        gs.generateMappingFromXMLAndRDF(xmlout, rdf);
    }

    public void generateMappingFromXMLAndRDF(String xmlfile, String rdffile) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(xmlfile));
            String xml = IOUtils.toString(inputStream);
            final InputStream xmlInputStream = IOUtils.toInputStream(xml);
            Document dataDocument = createDocument(xmlInputStream);
            removeWhiteSpaceTextNodes(dataDocument);
            List<RDFEnt> rdfents = readRDFFile(rdffile);
            travelesXML(dataDocument.getDocumentElement());
            System.out.println(xmlTags);
            final Map<String, Boolean> results = matchTags(xmlTags, rdfents);
            for (String key : results.keySet()) {
                System.out.println(key + "\t" + results.get(key));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void travelesXML(Node node) {

        // do something with the current node instead of System.out
//        System.out.println(node);
//        System.out.println("NodeName:" + node.getNodeName());
//        System.out.println("NodeValue:" + node.getNodeValue());
//        System.out.println("NodeType:" + node.getNodeType());
        if (node.getNodeType() == Node.TEXT_NODE) {
//            System.out.println("TextContent:" + node.getTextContent());
//            node.setTextContent(StrUtils.nextRandString());
//            System.out.println(node.getNodeName() + " " + node.getNodeValue() + " " + node.getPrefix());
//            System.out.println(node.getParentNode().getNodeName());
            xmlTags.put(node.getTextContent(), node.getParentNode().getNodeName());
        }
        NamedNodeMap attrs = node.getAttributes();

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                final Node attr = attrs.item(i);
//                attr.setTextContent(StrUtils.nextRandString());
//                System.out.println(attr.getFirstChild());
//                System.out.println(node.getNodeName());
                xmlTags.put(attr.getTextContent(), attr.getNodeName());
            }
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
//            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            //calls this method for all the children which is Element
            travelesXML(currentNode);
//            }
        }
    }

    public void generateXMLUniqueValueFile(String xmlfile, String xmlout) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(xmlfile));
            String xml = IOUtils.toString(inputStream);
            final InputStream xmlInputStream = IOUtils.toInputStream(xml);
            Document dataDocument = createDocument(xmlInputStream);
            removeWhiteSpaceTextNodes(dataDocument);
            modifyValues(dataDocument.getDocumentElement());

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//            tf.setOutputProperty(OutputKeys.INDENT, "yes");
//            tf.setOutputProperty(OutputKeys.METHOD, "xml");
//            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource domSource = new DOMSource(dataDocument);
            StreamResult sr = new StreamResult(new File(xmlout));
            tf.transform(domSource, sr);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | TransformerException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void modifyValues(Node node) {
        // do something with the current node instead of System.out
//        System.out.println(node);
//        System.out.println("NodeName:" + node.getNodeName());
//        System.out.println("NodeValue:" + node.getNodeValue());
//        System.out.println("NodeType:" + node.getNodeType());
        if (node.getNodeType() == Node.TEXT_NODE) {
//            System.out.println("TextContent:" + node.getTextContent());
            node.setTextContent(StrUtils.nextRandString());
        }
        NamedNodeMap attrs = node.getAttributes();

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                final Node attr = attrs.item(i);
                attr.setTextContent(StrUtils.nextRandString());
            }
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
//            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            //calls this method for all the children which is Element
            modifyValues(currentNode);
//            }
        }
    }

    private Document createDocument(InputStream inputStream) {
        try {
            return builder.parse(inputStream);
        } catch (SAXException | IOException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void setupDocumentBuilder() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeWhiteSpaceTextNodes(Document doc) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            // XPath to find empty text nodes.
            XPathExpression xpathExp = xpathFactory.newXPath().compile(
                    "//text()[normalize-space(.) = '']");
            NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);

            // Remove each empty text node from document.
            for (int i = 0; i < emptyTextNodes.getLength(); i++) {
                Node emptyTextNode = emptyTextNodes.item(i);
                emptyTextNode.getParentNode().removeChild(emptyTextNode);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<RDFEnt> readRDFFile(String rdffile) {
        List<RDFEnt> results = new ArrayList<>();
        try {
            NxParser nxp = new NxParser(new FileInputStream(rdffile));

            while (nxp.hasNext()) {
                final org.semanticweb.yars.nx.Node[] ns = nxp.next();
                final RDFEnt rdfent = pruneRDFEnt(ns[0], ns[1], ns[2]);
                if (rdfent != null) {
                    results.add(rdfent);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

//    List<String> exclude = Arrays.asList("http://bio2rdf.org/drugbank_resource:", "http://bio2rdf.org/bio2rdf_vocabulary:", "http://bio2rdf.org/drugbank_vocabulary:", "http://bio2rdf.org/drugbank:", "http://bio2rdf.org/cas:", "http://bio2rdf.org/cas_vocabulary:");
    private String pruneSubject(String subject) {
//        for (String str : exclude) {
//            if (subject.startsWith(str)) {
//                subject = subject.replace(str, "");
//                break;
//            }
//        }
        if (subject.startsWith("http://")) {
            subject = subject.replaceFirst("http://", "");
        }
        if (subject.contains(":")) {
            final int pos = subject.indexOf(":");
            return subject.substring(pos + 1);
        } else {
            return subject;
        }
    }

//    private boolean isValidGStr(String str) {
//        if (str.length() == 32) {
//            return true;
//        }
//        return false;
//    }
    private Map<String, Boolean> matchTags(Map<String, String> xmlTags, List<RDFEnt> rdfents) {
        Map<String, Boolean> results = new HashMap<String, Boolean>();
        for (RDFEnt ent : rdfents) {
            String subjectType = findMatch(ent.subject, xmlTags);
            String objectType = findMatch(ent.object, xmlTags);
            if (subjectType != null && objectType != null && !subjectType.equals(objectType)) {
//                System.out.println(ent.subject + "\t" + ent.object);
//                System.out.println(subjectType + "\t" + objectType + "\t" + ent.isAttr);
                results.put(subjectType + "\t" + objectType, ent.isAttr);
//                System.out.println();
            }
        }
        return results;
    }

    private String findMatch(String subject, Map<String, String> xmlTags) {
        for (String key : xmlTags.keySet()) {
            if (subject.contains(key)) {
                return xmlTags.get(key);
            }
        }
        return null;
    }

    class RDFEnt {

//        String key;
        String subject;
        String object;
        boolean isAttr;
    }

    private RDFEnt pruneRDFEnt(org.semanticweb.yars.nx.Node subject, org.semanticweb.yars.nx.Node predicate, org.semanticweb.yars.nx.Node object) {
        RDFEnt ent = new RDFEnt();
        String subjectLabel = subject.getLabel();
        subjectLabel = pruneSubject(subjectLabel);
        String objectStr = object + "";

        String objectLabel = object.getLabel();
        ent.subject = subjectLabel;
        ent.object = objectLabel;
        ent.isAttr = false;
        if (objectStr.endsWith("@en") || objectStr.endsWith("#string>")) {
            ent.isAttr = true;
        }
//        System.out.println(subject + "\t" + predicate + "\t" + object + "\t");
//
//        System.out.println(subjectLabel + "\t" + predicate + "\t" + objectLabel + "\t");
//        System.out.println();
//        if (isValidGStr(subjectLabel) && isValidGStr(objectLabel)) {
//            ent.key = subjectLabel + "_" + objectLabel;
//        }
        return ent;

    }

}
