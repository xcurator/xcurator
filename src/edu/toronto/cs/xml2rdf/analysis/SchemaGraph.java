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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class SchemaGraph {

    public static void main(String[] args) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
    }

    private Document doc;
    private String prefix;

    Map<String, Integer> graphNodeIds = new HashMap<String, Integer>();

    int globalId = 0;
    int groupId = 0;

    public SchemaGraph(String schemaPath, String prefix) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        this.doc = XMLUtils.parse(new FileInputStream(schemaPath), -1);
        this.prefix = prefix;
    }

    public SchemaGraph(InputStream schemaIS, String prefix) throws SAXException, IOException, ParserConfigurationException {
        this.doc = XMLUtils.parse(schemaIS, -1);
        this.prefix = prefix;
    }

    public SchemaGraph(Reader schemaReader, String prefix) throws SAXException, IOException, ParserConfigurationException {
        this.doc = XMLUtils.parse(schemaReader, -1);
        this.prefix = prefix;
    }

    public void generateDotGraph(PrintWriter out) {
        out.println("digraph G {");
        out.println("size=\"30,30\";");

        Element mappingElement = doc.getDocumentElement();
        NodeList children = mappingElement.getChildNodes();

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityElement = (Element) childNode;
                generateDotForElement(entityElement, out);
            }

            children.item(i);
        }

        out.println("}");
    }

    private void generateDotForElement(Element entityElement, PrintWriter out) {
        NodeList children = entityElement.getChildNodes();

        String name = entityElement.getAttribute("type").replace(prefix, "");
        boolean hasLinkToFreeBase = false;
        boolean hasLinkToOpenCyc = false;

        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityChild = (Element) childNode;
                if ("property".equals(entityChild.getNodeName())) {
                    out.print(name + "->");
                    String propertyName = getNameForProperty(entityChild, out);
                    out.println(propertyName + ";");
                    out.println(propertyName + " [style=rounded,shape=box,label=\"\"];");

                    NodeList propertyChildren = entityChild.getChildNodes();
                    for (int j = 0; j < propertyChildren.getLength(); j++) {
                        Node propertyChildNode = propertyChildren.item(j);
                        if (propertyChildNode instanceof Element && "ontology-link".equals(propertyChildNode.getNodeName())) {
                            out.print(propertyName + "->");
                            String ontologyName = getNameForOntlink((Element) propertyChildNode, out);
                            out.println(ontologyName + ";");
                            out.println(ontologyName + " [style=dotted,shape=box,label=\"\"];");
                        }
                    }
                } else if ("ontology-link".equals(entityChild.getNodeName())) {
                    out.print(name + "->");
                    String linkName = getNameForOntlink(entityChild, out);
                    out.println(linkName + ";");
                    out.println(linkName + " [style=dotted,shape=box,label=\"\"];");
                } else if ("relation".equals(entityChild.getNodeName())) {
                    out.print(name + "->");
                    String relationName = getNameForRelation(entityChild, out);
                    out.println(relationName + ";");
                }

            }

        }

        out.println(name + " [shape=box,label=\"\"];");
    }

    private String getNameForRelation(Element entityChild, PrintWriter out) {
        String name = entityChild.getAttribute("targetEntity").replace(prefix, "");
        return name;
    }

    private String getNameForOntlink(Element entityChild, PrintWriter out) {
        String name = entityChild.getAttribute("uri");
        name = name.substring(name.lastIndexOf("/") + 1).replace('.', '_').replace("-", "");
        return name;
    }

    private String getNameForProperty(Element entityChild, PrintWriter out) {
        String name = entityChild.getAttribute("name").replace(prefix, "");
        return name;
    }

    public void generateProtoVis(PrintWriter out) {
        //    var miserables = {
        //          nodes:[
        //            {nodeName:"Myriel", group:1},
        //            {nodeName:"Napoleon", group:1},

        out.println("var schemaGraph = {");
        out.println("nodes: [ ");

        Element mappingElement = doc.getDocumentElement();
        NodeList children = mappingElement.getChildNodes();

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityElement = (Element) childNode;
                generatePropVisNode(entityElement, out);
            }

        }
        out.println("],\nlinks: [");

        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityElement = (Element) childNode;
                generatePropVisLinks(entityElement, out);
            }

        }

        out.println("]}");
    }

    private void generatePropVisNode(Element entityElement, PrintWriter out) {
        NodeList children = entityElement.getChildNodes();

        groupId++;

        String name = entityElement.getAttribute("type");
        graphNodeIds.put(name, globalId++);
        out.println("{nodeName:\"" + name + "\",group:" + groupId + "},");

        boolean linked = false;
        boolean hasLinkToOpenCyc = false;

        //    {nodeName:"Myriel", group:1},
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityChild = (Element) childNode;
                if ("property".equals(entityChild.getNodeName())) {
                    String propertyName = name + getNameForProperty(entityChild, out);
                    graphNodeIds.put(propertyName, globalId++);
                    out.println("{nodeName:\"" + propertyName + "\",group:" + groupId + "},");

                    NodeList linksList = entityChild.getElementsByTagName("ontology-link");
                    if (linksList.getLength() != 0) {
                        graphNodeIds.put(propertyName + "links", globalId++);
                        out.println("{nodeName:\"" + propertyName + "links" + "\",group:" + groupId + "},");
                    }
                    //          
                    //          NodeList propertyChildren = entityChild.getChildNodes();
                    //          for (int j = 0; j < propertyChildren.getLength(); j++) {
                    //            Node propertyChildNode = propertyChildren.item(j);
                    //            if (propertyChildNode instanceof Element && "ontology-link".equals(propertyChildNode.getNodeName())) {
                    //              String ontologyName = name + getNameForOntlink((Element) propertyChildNode, out);
                    //              graphNodeIds.put(ontologyName, globalId++ );
                    //              out.println("{nodeName:\""+ ontologyName+ "\",group:" + groupId + "},");
                    //
                    //            }
                    //          }
                } else if ("ontology-link".equals(entityChild.getNodeName())) {
                    if (!linked) {
                        linked = true;
                        String ontologyName = name + "link";
                        graphNodeIds.put(ontologyName, globalId++);
                        out.println("{nodeName:\"" + ontologyName + "\",group:" + groupId + "},");
                    }

                } else if ("relation".equals(entityChild.getNodeName())) {
                }

            }

        }
    }

    private void generatePropVisLinks(Element entityElement, PrintWriter out) {
        NodeList children = entityElement.getChildNodes();

        String name = entityElement.getAttribute("type");
        Integer sourceId = graphNodeIds.get(name);

        boolean linked = false;
        //      {source:1, target:0, value:1},

        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element entityChild = (Element) childNode;
                if ("property".equals(entityChild.getNodeName())) {
                    String propertyName = name + getNameForProperty(entityChild, out);
                    Integer destId = graphNodeIds.get(propertyName);
                    out.println("{source:" + sourceId + ", target:" + destId + ", value:1}, ");

                    NodeList linksList = entityChild.getElementsByTagName("ontology-link");
                    if (linksList.getLength() != 0) {
                        out.println("{source:" + destId + ", target:" + graphNodeIds.get(propertyName + "links") + ", value:21}, ");
                    }

                    //          NodeList propertyChildren = entityChild.getChildNodes();
                    //          for (int j = 0; j < propertyChildren.getLength(); j++) {
                    //
                    //
                    //            
                    //            Node propertyChildNode = propertyChildren.item(j);
                    //
                    //
                    //            
                    //            if (propertyChildNode instanceof Element && "ontology-link".equals(propertyChildNode.getNodeName())) {
                    //              String ontologyName = name + getNameForOntlink((Element) propertyChildNode, out);
                    //              destId = graphNodeIds.get(ontologyName);
                    //              out.println("{source:" + sourceId + ", target:" + destId + ", value:1}, ");
                    //
                    //            }
                    //          }
                } else if ("ontology-link".equals(entityChild.getNodeName())) {
                    if (!linked) {
                        linked = true;
                        String ontologyName = name + "link";
                        Integer destId = graphNodeIds.get(ontologyName);
                        out.println("{source:" + sourceId + ", target:" + destId + ", value:21}, ");
                    }

                    //          
                    //          String ontologyName = name + getNameForOntlink(entityChild, out);
                    //          Integer destId = graphNodeIds.get(ontologyName);
                    //          out.println("{source:" + sourceId + ", target:" + destId + ", value:1}, ");
                } else if ("relation".equals(entityChild.getNodeName())) {
                    //      {source:1, target:0, value:1},
                    Integer destId = graphNodeIds.get(entityChild.getAttribute("targetEntity"));
                    out.println("{source:" + sourceId + ", target:" + destId + ", value:4}, ");
                }

            }

        }
    }

}
