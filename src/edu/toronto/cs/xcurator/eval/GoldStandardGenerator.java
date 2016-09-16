/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.eval;

import com.github.jsonldjava.core.RDFDataset;
import edu.toronto.cs.xcurator.utils.StrUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    public GoldStandardGenerator() {

        setupDocumentBuilder();
    }
    static String xmlfile = "D:\\workspace\\bio2rdf-data\\data\\download\\drugbank\\drugbank.sample2.xml";
    static String xmlout = "D:\\workspace\\bio2rdf-data\\data\\download\\drugbank\\drugbank.sample2.out.xml";
    static String rdf = "D:\\workspace\\bio2rdf-data\\data\\rdf\\drugbank\\drugbank.nq";

    public static void generateXML() {
        GoldStandardGenerator gs = new GoldStandardGenerator();
        gs.generateXMLUniqueValueFile(xmlfile, xmlout);
    }

    public static void main(String[] args) {
        GoldStandardGenerator gs = new GoldStandardGenerator();
        gs.readRDFFile(rdf);
    }

    public void generateMappingFromXMLAndRDF(String xmlfile, String rdffile) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(xmlfile));
            String xml = IOUtils.toString(inputStream);
            final InputStream xmlInputStream = IOUtils.toInputStream(xml);
            Document dataDocument = createDocument(xmlInputStream);
            removeWhiteSpaceTextNodes(dataDocument);
            readRDFFile(rdffile);

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

    private void readRDFFile(String rdffile) {
        try {
            NxParser nxp = new NxParser(new FileInputStream(rdffile));

            while (nxp.hasNext()) {
                final org.semanticweb.yars.nx.Node[] ns = nxp.next();
//                System.out.println(ns);
                if (ns.length == 4) {
                    //Only Process Triples
                    //Replace the print statements with whatever you want
                    for (org.semanticweb.yars.nx.Node n : ns) {
                        System.out.print(n);
                        System.out.print(" ");
                    }
                    System.out.println(".");
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoldStandardGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
