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
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class Mapping {
  private Document doc; 
  private List<Entity> entities;
  private Set<String> blackList; 

  public Mapping(String path, Set<String> blackList) throws ParserConfigurationException, SAXException, IOException {
    entities = new ArrayList<Entity>();
    this.doc = XMLUtils.parse(path, -1);
    this.blackList = blackList;
    loadEntities(doc);
  }

  public Mapping(InputStream is,  Set<String> blackList) throws SAXException, IOException, ParserConfigurationException {
    entities = new ArrayList<Entity>();
    this.doc = XMLUtils.parse(is, -1);
    this.blackList = blackList;
    loadEntities(doc);
  }

  public Mapping(Reader reader, Set<String> blackList) throws SAXException, IOException, ParserConfigurationException {
    entities = new ArrayList<Entity>();
    this.doc = XMLUtils.parse(reader, -1);
    this.blackList = blackList;
    loadEntities(doc);
  }

  private void loadEntities(Document doc) {
    NodeList nl = doc.getElementsByTagName("entity");
    for (int i = 0; i < nl.getLength(); i++) {
      Element entityElement = (Element) nl.item(i);
      Entity entity = getEntityFromElement(entityElement);
      this.entities.add(entity);
    }
  }

  private Entity getEntityFromElement(Element entityElement) {
    String type = entityElement.getAttribute("type");
    String path = entityElement.getAttribute("path");
    Entity entity = new Entity(type, path, entityElement, doc, blackList);

    entity.reloadId();
    entity.reloadAttributes();
    entity.reloadRelations();
    entity.reloadLookupKeys();
    entity.reloadOntologyLinks();

    return entity;
  }

  public void generateRDFs(String tdbPath, Document dataDoc, String typePrefix, PrintStream out, 
      String format, StringMetric stringMetric, double threshold) throws XPathExpressionException {
    Model model = JenaUtils.getTDBModel(tdbPath);

    for (Entity entity: entities) {
      String entityPath = entity.getPath();
      LogUtils.debug(this.getClass(), "Creating instances of " + entity );
      NodeList nodeList = XMLUtils.getNodesByPath(entityPath, null, dataDoc);
      for (int i = 0; i < nodeList.getLength(); i++) {
        entity.generateRDF((Element) nodeList.item(i), dataDoc, model, typePrefix, stringMetric, threshold);
      }
      LogUtils.debug(this.getClass(), "Instances of " + entity + " are created.");
    }

    if (out != null) {
      model.write(out, format);
    }
    model.commit();
    model.close();
  }

  public void generateRDFSchema(String tdbPath, Document dataDoc, 
      String typePrefix, PrintStream out, 
      String format, StringMetric stringMetric, double threshold) throws XPathExpressionException {
    Model model = JenaUtils.getTDBModel(tdbPath);

    Map<String, Set<String>> ranges = new HashMap<String, Set<String>>();
    Map<String, Set<String>> domains = new HashMap<String, Set<String>>();

    Resource rootResource = model.createResource(typePrefix);

    com.hp.hpl.jena.rdf.model.Property classProperty = model.createProperty("http://dblab.cs.toronto.edu/project/xcurator/0.1#classes");
    Bag classBag = model.createBag(typePrefix + "classBag");
    model.add(rootResource, classProperty, classBag);

    com.hp.hpl.jena.rdf.model.Property propertyProperty = model.createProperty("http://dblab.cs.toronto.edu/project/xcurator/0.1#properties");
    Bag propertyBag = model.createBag(typePrefix + "propertyBag");
    model.add(rootResource, propertyProperty, propertyBag);

    for (Entity entity: entities) {
      Resource type = model.createResource(entity.getType());
      model.add(type, RDF.type, RDFS.Class);
      model.add(type, RDFS.subClassOf, RDFS.Resource);

      if (!classBag.contains(type)) {
        classBag.add(type);
      }

      for (Property prop: entity.getProperties()) {
        Set<String> propRange = ranges.get(prop.getName());
        if (propRange == null) {
          propRange = new HashSet<String>();
          ranges.put(prop.getName(), propRange);
        }
        propRange.add(RDFS.Literal.getURI());


        Set<String> propDomain = domains.get(prop.getName());
        if (propDomain == null) {
          propDomain = new HashSet<String>();
          domains.put(prop.getName(), propDomain);
        }
        propDomain.add(entity.getType());
      }

      for (Relation relation: entity.getRelations()) {
        Set<String> relRange = ranges.get(relation.getName());
        if (relRange == null) {
          relRange = new HashSet<String>();
          ranges.put(relation.getName(), relRange);
        }
        relRange.add(relation.getTargetEntity());

        Set<String> relDomain = domains.get(relation.getName());
        if (relDomain == null) {
          relDomain = new HashSet<String>();
          domains.put(relation.getName(), relDomain);
        }
        relDomain.add(entity.getType());
      }
    }

    for (String name: ranges.keySet()) {
      Resource nameResouce = model.createResource(name);
      model.add(nameResouce, RDF.type, RDF.Property);
      if (!propertyBag.contains(nameResouce)) {
        propertyBag.add(nameResouce);
      } 

      if (ranges.get(name).size() == 0) {
        model.add(nameResouce, RDFS.range, RDFS.Resource);
      } else {
        String range = ranges.get(name).iterator().next();
        Resource rangeResouce = model.createResource(range);
        model.add(nameResouce, RDFS.range, rangeResouce);
      }

      if (domains.get(name).size() == 0) {
        model.add(nameResouce, RDFS.domain, RDFS.Resource);
      } else {
        String domain = domains.get(name).iterator().next();
        Resource domainResouce = model.createResource(domain);
        model.add(nameResouce, RDFS.domain, domainResouce);
      }
    }

    //    RDF.Bag
    //    RDFS.member

    if (out != null) {
      model.write(out, format);
    }
    model.commit();
    model.close();
  }
  
  public static Set<String> getAllTypes(Model model, String typePrefix) {
    Resource rootResource = model.createResource(typePrefix);

    com.hp.hpl.jena.rdf.model.Property classProperty = model.createProperty("http://dblab.cs.toronto.edu/project/xcurator/0.1#classes");
    Bag classBag = model.createBag("http://dblab.cs.toronto.edu/project/xcurator/0.1#classBag");
    model.add(rootResource, classProperty, classBag);
    
    Set<String> ret = new HashSet<String>();
    
    NodeIterator iterator = classBag.iterator();
    while (iterator.hasNext()) {
      Resource resource = (Resource) iterator.next();
      ret.add(resource.getURI());
    }
    
    return ret;
  }
}
