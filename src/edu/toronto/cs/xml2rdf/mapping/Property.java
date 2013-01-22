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
package edu.toronto.cs.xml2rdf.mapping;

import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class Property {

  private String name;
  private String type;
  private Entity entity;
  private String path;
  private Element element;
  private Set<String> ontologyTypes;
  boolean key;

  public Property(String name, String type, Entity entity, String path,
      Element element, Set<String> ontologyTypes, boolean key) {
    this.name = name;
    this.type = type;
    this.entity = entity;
    this.path = path;
    this.element = element;
    this.ontologyTypes = ontologyTypes;
    this.key = key;
  }

  public String getValue(Element dataElement, Document dataDoc) {
    // TODO: Run xpath and then convert it based on the type
    return null;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Element getElement() {
    return element;
  }

  public void setElement(Element element) {
    this.element = element;
  }
  
  public Set<String> getOntologyTypes() {
    return ontologyTypes;
  }

  public void setOntologyTypes(Set<String> ontologyTypes) {
    this.ontologyTypes = ontologyTypes;
  }

  public boolean isKey() {
    return key;
  }

  public void setKey(boolean key) {
    this.key = key;
  }

  public com.hp.hpl.jena.rdf.model.Property getJenaProperty(Model model) {
    return model.createProperty(getName());
  }
  
  public Resource createRDFProperty(
      Model model, Resource parentResource, Element item, Document dataDoc) throws XPathExpressionException {
    
    com.hp.hpl.jena.rdf.model.Property jenaProperty = getJenaProperty(model);
    Resource res = null;
    NodeList nodeList = XMLUtils.getNodesByPath(path, item, dataDoc);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      String value = node.getTextContent();
      res = parentResource.addProperty(jenaProperty, value);
    }
    
    
    for (String uri: ontologyTypes) {
      jenaProperty.addProperty(OWL.equivalentProperty, model.createResource(uri));
    }

    return res;
  }
  
  public String getSPARQLEqualPhrase(String parentVarName, Element item, Document dataDoc) throws XPathExpressionException {
    String phrase = "";
    NodeList nodeList = XMLUtils.getNodesByPath(path, item, dataDoc);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      String value = JenaUtils.querify(node.getTextContent());
//      value = value.replace("\n", "\\n");
//      value = value.replaceAll("\\s+", "\\\\\\\\s+")
//            .replaceAll("\\[", ".")
//            .replaceAll("\\]", ".")
//            .replace("\"", "\\\"")
//            .replace("*", "\\\\*")
//            .replace("+", "\\\\+")
//            .replace("(", "\\\\(")
//            .replace(")", "\\\\)");
      String varName = JenaUtils.getNextSparqlVarName();
      phrase += parentVarName + " <" + name + "> " + varName + " . \n";
      phrase += "FILTER ( " + varName + " = \""+ value + "\") . \n";
//      phrase += "FILTER regex("+ varName + ", \"^" + value + "$\", \"i\" ) . \n";
    }
    return phrase;
  }
  
  public static void main(String[] args) {
    System.out.println("*".replace("*", "\\*"));
  }
  
}
