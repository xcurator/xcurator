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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.toronto.cs.xml2rdf.freebase.FreeBaseLinker;
import edu.toronto.cs.xml2rdf.jena.SKOS;
import edu.toronto.cs.xml2rdf.opencyc.OpenCycOntology;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class Entity {

  static boolean debug = true;
  
  public static final String AUTO_GENERATED = "UUID";
  private Element element;
  private String path;
  private String type;
  private Document doc;

  private List<Property> properties;
  private List<Relation> relations;
  private List<LookupKey> lookupKeys;
  private Set<String> ontologyTypes;
  private String id;
  private Set<String> blackList;

  public Entity(String type, String path, Element entityElement, Document doc, Set<String> blackList) {
    this.type = type;
    this.path = path;
    this.element = entityElement;
    this.doc = doc;
    this.blackList = blackList;
  }

  public void addProperty(Property attr) {
    properties.add(attr);
  }

  public void addRelation(Relation relation) {
    relations.add(relation);
  }

  public void addLookupKey(LookupKey key) {
    lookupKeys.add(key);
  }

  public void addOntologyLink(String conceptURI) {
    ontologyTypes.add(conceptURI);
  }

  public void reloadId() {
    id = element.getElementsByTagName("id").item(0).getTextContent();
  }

  public void reloadAttributes() {
    properties = new ArrayList<Property>();

    NodeList nl = element.getElementsByTagName("property");
    for (int i = 0; i < nl.getLength(); i++) {
      Element attributeElement = (Element) nl.item(i);
      if (attributeElement.getParentNode() != element) {
        continue;
      }

      String name = attributeElement.getAttribute("name");
      String type = attributeElement.getAttribute("type");
      String path = attributeElement.getAttribute("path");

      Set<String> typeSet = new HashSet<String>();
      NodeList ontologyNodeList = attributeElement.getElementsByTagName("ontology-link");
      for (int j = 0; j < ontologyNodeList.getLength(); j++) {
        Node linkNode = ontologyNodeList.item(j);
        if (linkNode instanceof Element) {
          String typeURI = ((Element)linkNode).getAttribute("uri");
          if (!blackList.contains(typeURI)) {
            typeSet.add(typeURI);
          }
        }
      }

      addProperty(
          new Property(name, type, this, path, 
              attributeElement, typeSet, 
              "true".equals(attributeElement.getAttribute("key"))));
    }
  }

  public void reloadRelations() {
    relations = new ArrayList<Relation>();

    NodeList nl = element.getElementsByTagName("relation");
    for (int i = 0; i < nl.getLength(); i++) {
      Element relationElement = (Element) nl.item(i);
      if (relationElement.getParentNode() != element) {
        continue;
      }

      String name = relationElement.getAttribute("name");
      String targetEntity = relationElement.getAttribute("targetEntity");
      String path = relationElement.getAttribute("path");

      /*
       * Loading the foreign lookup key
       */
      Element foreignLookupKeyElement = (Element) relationElement
      .getElementsByTagName("lookupkey").item(0);
      List<Property> properties = new ArrayList<Property>();
      NodeList targetProperties = foreignLookupKeyElement
      .getElementsByTagName("target-property");
      for (int j = 0; j < targetProperties.getLength(); j++) {
        Element propertyElement = (Element) targetProperties.item(j);
        String propertyName = propertyElement.getAttribute("name");
        String propertyPath = propertyElement.getAttribute("path");
        properties.add(new Property(propertyName, propertyPath, this,
            propertyPath, propertyElement, new HashSet<String>(), true));
      }
      ForeignLookupKey foreignLookupKey = new ForeignLookupKey(name,
          this, targetEntity, properties, foreignLookupKeyElement);

      Relation relation = new Relation(name, this, targetEntity,
          foreignLookupKey, path, relationElement);
      addRelation(relation);
    }
  }

  public void reloadLookupKeys() {
    lookupKeys = new ArrayList<LookupKey>();

    NodeList nl = element.getElementsByTagName("lookupkey");
    for (int i = 0; i < nl.getLength(); i++) {
      Element lookupKeyElement = (Element) nl.item(i);
      if (lookupKeyElement.getParentNode() != element) {
        continue;
      }

      List<String> attributes = new ArrayList<String>();
      NodeList pkAttribute = lookupKeyElement
      .getElementsByTagName("attribute");
      for (int j = 0; j < pkAttribute.getLength(); j++) {
        Element attribute = (Element) pkAttribute.item(j);
        attributes.add(attribute.getAttribute("name"));
      }

      LookupKey lookupKey = new LookupKey(type, this, attributes,
          lookupKeyElement);
      addLookupKey(lookupKey);
    }
  }

  public void reloadOntologyLinks() {
    ontologyTypes = new HashSet<String>();

    NodeList nl = element.getElementsByTagName("ontology-link");
    for (int i = 0; i < nl.getLength(); i++) {
      Element ontologyLinkElement = (Element) nl.item(i);
      if (ontologyLinkElement.getParentNode() != element) {
        continue;
      }
      String uri = ontologyLinkElement.getAttribute("uri");
      if (!blackList.contains(uri)) {
        addOntologyLink(uri);
      }
    }
  }


  public Element getElement() {
    return element;
  }

  public void setElement(Element element) {
    this.element = element;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Document getDoc() {
    return doc;
  }

  public void setDoc(Document doc) {
    this.doc = doc;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public List<Relation> getRelations() {
    return relations;
  }

  public List<LookupKey> getLookupKeys() {
    return lookupKeys;
  }

  public String generateId(Element element, Document doc)
  throws XPathExpressionException {
    int lastEndIndex = 0;
    String generatedId = "";
    do {
      int startIndex = id.indexOf("${", lastEndIndex);
      if (startIndex == -1) {
        break;
      }

      int endIndex = id.indexOf("}", startIndex);
      if (endIndex == -1) {
        break;
      }

      String literal = id.substring(lastEndIndex, startIndex);

      String path = id.substring(startIndex + 2, endIndex);
      String pathValue = null;
      if (AUTO_GENERATED.equals(path)) {
        try {
          MessageDigest digest = MessageDigest.getInstance("MD5");
          pathValue = "";
          byte[] md5 = digest.digest(XMLUtils.asByteArray(element));
          for (byte b: md5) {
            pathValue += Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 );

          }
        } catch (NoSuchAlgorithmException e) {
          // TODO Auto-generated catch block
          if (debug)
            e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          if (debug)
            e.printStackTrace();
        }
      } else {
        pathValue = XMLUtils.getStringByPath(path, element, doc);
      }

      generatedId += literal + pathValue;

      lastEndIndex = endIndex + 1;
    } while (true);

    if (generatedId.trim().length() == 0) {
      LogUtils.error(this.getClass(), "Error in generating id: " + XMLUtils.getAllLeaves(element));
    }
    return generatedId;
  }

  public void generateRDF(Element item, Document dataDoc, Model model, String typePrefix,
      StringMetric stringMetric, double threshold) 
  throws XPathExpressionException {
    if (getSameResource(model, typePrefix, item, dataDoc) != null) {
      LogUtils.debug(this.getClass(), "Duplicate detected!");
      return;
    }
    
    Resource rootResource = model.createResource(typePrefix);

    com.hp.hpl.jena.rdf.model.Property classProperty = model.createProperty("http://dblab.cs.toronto.edu/project/xcurator/0.1#instances");
    Bag instanceBag = model.createBag(typePrefix + "instanceBag");
    model.add(rootResource, classProperty, instanceBag);


    Resource typeResource = model.createResource(getType());
    Resource instanceResource = model.createResource(generateId(item, dataDoc));

    instanceResource.addProperty(RDF.type, typeResource);

    if (!instanceBag.contains(instanceResource)) {
      instanceBag.add(instanceResource);
    }
    
    String name = null;
    for (Property property : getProperties()) {
      property.createRDFProperty(model, instanceResource, item, dataDoc);
    }

    for (Relation relation : getRelations()) {
      relation.createRDFRelation(model, instanceResource, item, dataDoc, typePrefix);
    }

    if (ontologyTypes != null && ontologyTypes.size() > 0) {
      name = item.getTextContent();
      Set<String> sameAs = OpenCycOntology.getInstance().findSameAsForResource(name, stringMetric, threshold, ontologyTypes);
      for (String uri: sameAs) {
        instanceResource.addProperty(sameAs.size() == 1? SKOS.exactMatch : SKOS.closeMatch, uri);
      }

      sameAs = new FreeBaseLinker().findSameAsForResource(name, stringMetric, threshold, ontologyTypes);
      for (String uri: sameAs) {
        instanceResource.addProperty(sameAs.size() == 1? SKOS.exactMatch : SKOS.closeMatch, uri);
      }

      for (String uri: ontologyTypes) {
        typeResource.addProperty(OWL.equivalentClass, model.createResource(uri));
      }
    }
  }

  private Object getSameResource(Model model, String typePrefix,
      Element item, Document dataDoc) throws XPathExpressionException {
    QueryExecution qExec = null;
    try{
      String query = getEqualsQuery(model, typePrefix, item, dataDoc);
      LogUtils.debug(this.getClass(), query);
      qExec = QueryExecutionFactory.create(query, model);
      ResultSet rs = qExec.execSelect();
      while (rs.hasNext()) {
        QuerySolution solution = rs.next();
        return solution.get("?x0");
      }
    } catch(Exception e){
      if (debug)
        e.printStackTrace();
    } finally {
      if (qExec != null) {
        qExec.close();
      }
    }
    return null;
  }

  public String getEqualsQuery(Model model, String typePrefix, Element item, Document dataDoc) throws XPathExpressionException {

    String whereClause = "WHERE {\n";

    whereClause += "?x0 rdf:type <" + type + "> . \n";
    boolean hasKey = false;
    for (Property property : getProperties()) {
      if (property.isKey()) {
        hasKey = true;
      }
    }
    for (Property property : getProperties()) {
      if (property.isKey() || !hasKey) {
        whereClause += property.getSPARQLEqualPhrase("?x0", item, dataDoc);
      }
    }

    if (!hasKey) {
      for (Relation rel: getRelations()) {
        whereClause += rel.getSPARQLEqualPhrase("?x0", typePrefix, model, item, dataDoc);
      }
    }

    whereClause += "}\n";

    String prefixes = "PREFIX t: <" + typePrefix + "> \n" +
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"; 
    String select = "select ?x0 ";

    return prefixes + select + whereClause;
  }

  public static Set<Resource> getAllEntitiesOfType(Model model, String type) {
    Set<Resource> ret = new HashSet<Resource>();
    
    ResIterator list = model.listSubjectsWithProperty(RDF.type, model.createResource(type));
    while (list.hasNext()) {
      Resource entity = list.next();
      ret.add(entity);
    }
    
    return ret;
  }
}
