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
package edu.toronto.cs.xml2rdf.analysis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class SchemaNodeLinkTree {

    private Document doc;
    private String prefix;

    public SchemaNodeLinkTree(String schemaPath, String prefix) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        this.doc = XMLUtils.parse(new FileInputStream(schemaPath), -1);
        this.prefix = prefix;
    }

    Map<String, Element> schemaMap = new HashMap<String, Element>();

    public void generateDataTree(PrintStream out) {
        Element mappingElement = doc.getDocumentElement();
        NodeList children = mappingElement.getChildNodes();

        String rootEntity = null;

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityElement = (Element) childNode;
                String entityType = entityElement.getAttribute("type");
                schemaMap.put(entityType, entityElement);

                if (rootEntity == null) {
                    rootEntity = entityType;
                }
            }
        }

        out.println("var " + rootEntity + " = {");

        Element rootElement = schemaMap.get(rootEntity);
        children = rootElement.getChildNodes();

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                generateDataForElement((Element) childNode, out);
            }
        }
        out.println("};");
    }

    private void generateDataForElement(Element entityElement, PrintStream out) {
        NodeList children = entityElement.getChildNodes();

        String name = entityElement.getAttribute("type").replace(prefix, "");
        out.println(name + ": {");

        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityChild = (Element) childNode;
                if ("property".equals(entityChild.getNodeName())) {
                    String propertyName = getNameForProperty(entityChild, out);
                    out.print(propertyName + ": ");

                    NodeList propertyChildren = entityChild.getElementsByTagName("ontology-link");
                    if (propertyChildren.getLength() != 0) {
                        out.println("{");
                    }
                    for (int j = 0; j < propertyChildren.getLength(); j++) {
                        Node propertyChildNode = propertyChildren.item(j);
                        if (propertyChildNode instanceof Element && "ontology-link".equals(propertyChildNode.getNodeName())) {
                            String ontologyName = getNameForOntlink((Element) propertyChildNode, out);
                            out.println(ontologyName + ":1,");
                        }
                    }

                    if (propertyChildren.getLength() == 0) {
                        out.println(" 1,");
                    } else {
                        out.println("}, ");
                    }
                } else if ("ontology-link".equals(entityChild.getNodeName())) {
                    String linkName = getNameForOntlink(entityChild, out);
                    out.println(linkName + ": 1, ");
                } else if ("relation".equals(entityChild.getNodeName())) {
                    String relationName = getNameForRelation(entityChild, out);
                    out.println(relationName + ": {");
                    generateDataForElement(schemaMap.get(entityChild.getAttribute("targetEntity")), out);
                    out.println(relationName + "},");
                }

            }

        }
        out.println("},");
    }

    private String getNameForRelation(Element entityChild, PrintStream out) {
        String name = entityChild.getAttribute("targetEntity").replace(prefix, "");
        return name;
    }

    private String getNameForOntlink(Element entityChild, PrintStream out) {
        String name = entityChild.getAttribute("uri");
        name = name.substring(name.lastIndexOf("/") + 1).replace('.', '_').replace("-", "");
        return name;
    }

    private String getNameForProperty(Element entityChild, PrintStream out) {
        String name = entityChild.getAttribute("name").replace(prefix, "");
        return name;
    }

    public static void main(String[] args) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        new SchemaNodeLinkTree("/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/output.250.xml", "http://www.linkedct.org/0.1#")
                .generateDataTree(System.out);
    }
}
