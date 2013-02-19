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

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class Statistics {
	private Document doc;
	private String prefix; 
	private int entityCount = 0;

	private int propertyCount = 0;
	private int propertyMax = 0;
	private int propertyMin = Integer.MAX_VALUE;

	private int relCount = 0;
	private int relMax = 0;
	private int relMin = Integer.MAX_VALUE;

	private int linkCount = 0;
	private int linkMax = 0;
	private int linkMin = Integer.MAX_VALUE;

	private int promotedCount = 0;

	private int mergedCount = 0;
	private int linkedCount;


	public Statistics(String schemaPath, String prefix) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		this.doc = XMLUtils.parse(new FileInputStream(schemaPath), -1);
		this.prefix = prefix;
	}



	public void generateStatistics(PrintStream out) {
		Element mappingElement = doc.getDocumentElement();
		NodeList children = mappingElement.getChildNodes();

		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				entityCount++;

				Element entityElement = (Element) childNode;
				updateStatsForElement(entityElement);
			}
		}

		//    out.println(/*"prop:\t" +*/ propertyCount/(double)entityCount + "\t" + propertyMin + "\t" + propertyMax);
		out.println(/*"rel:\t" + */relCount/(double)entityCount + "\t" + relMin + "\t" + relMax);
		//    out.println(/*"link:\t" + */linkCount/(double)entityCount + "\t" + linkMin + "\t" + linkMax);
		//    out.println("merged:\t" + mergedCount);
		//    out.println("promoted:\t" + promotedCount);
		//    out.println("linked count:\t" + linkedCount);
		//    out.println("entity count:\t" + entityCount);

	}

	private void updateStatsForElement(Element entityElement) {

		mergedCount += entityElement.getAttribute("type").contains("_or_")? 1 : 0;

		NodeList children = entityElement.getChildNodes();

		int propertyCount = 0;
		int linkCount = 0;
		int relCount = 0;

		boolean linked = false;

		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (childNode instanceof Element) {
				Element entityChild = (Element) childNode;
				if ("property".equals(entityChild.getNodeName())) {
					this.propertyCount++;
					propertyCount++;

					boolean propertyLinked = false;
					NodeList propertyChildren = entityChild.getChildNodes();
					for (int j = 0; j < propertyChildren.getLength(); j++) {
						Node propertyChildNode = propertyChildren.item(j);
						if (propertyChildNode instanceof Element && "ontology-link".equals(propertyChildNode.getNodeName())) {
							linkCount++;
							this.linkCount++;
							propertyLinked = true;
						}
					}

					if (propertyLinked) {
						linkedCount++;
					}
				} else if ("ontology-link".equals(entityChild.getNodeName())) {
					linkCount++;
					this.linkCount++;
					linked = true;
				} else if ("relation".equals(entityChild.getNodeName())) {
					relCount++;
					this.relCount++;
				}

			}
		}

		this.relMin = Math.min(relCount, this.relMin);
		this.relMax = Math.max(relCount, this.relMax);

		this.propertyMax = Math.max(propertyCount, propertyMax);
		this.propertyMin = Math.min(propertyCount, propertyMin);

		this.linkMax = Math.max(linkCount, linkMax);
		this.linkMin = Math.min(linkCount, linkMin);

		if (relCount == 0 && linkCount > 0 && propertyCount == 1) {
			promotedCount++;
		}

		if (linked) {
			linkedCount++;
		}

	}

	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {

		for (int i: new int[]{5, 10, 20, 40, 50, 100, 125, 250, 500}) {
			new Statistics("/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/output."+ i + ".xml", "http://www.linkedct.org/0.1#")
			.generateStatistics(System.out);
		}
	}
}
