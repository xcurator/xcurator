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
package edu.toronto.cs.xcurator.rdf;

import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.xml.NsContext;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class XmlBasedMappingDeserialization implements RdfGenerationStep {

  private final String mappingFilePath;
  private final XmlParser parser;

  public XmlBasedMappingDeserialization(String mappingFilePath, XmlParser parser) {
    this.mappingFilePath = mappingFilePath;
    this.parser = parser;
  }

  @Override
  public void process(Mapping mapping) {
    try {
      if (!(mapping instanceof XmlBasedMapping)) {
        throw new IllegalArgumentException("The mapping needs to be XML based.");
      }
      Document mapDoc = parser.parse(mappingFilePath, -1);
      Element root = mapDoc.getDocumentElement();
      
      // Get the namespace URI of the mapping element tags
      String namespaceUri = root.getNamespaceURI();
      ((XmlBasedMapping)mapping).setMappingNamespaceUri(namespaceUri);
      
      // Discover the root namespace context, which maybe overrided by entities
      NsContext rootNsContext = new NsContext(root);
      mapping.setBaseNamespaceContext(rootNsContext);
      
      // Looking at all entities in the mapping
      NodeList nl = mapDoc.getElementsByTagNameNS(namespaceUri, XmlBasedMapping.entityTagName);
      for (int i = 0; i < nl.getLength(); i++) {
        Element entityElement = (Element) nl.item(i);
        
        // Discover the (maybe) overrided namespace context of this entity
        NsContext nsContext = new NsContext(rootNsContext);
        nsContext.discover(entityElement);
        
        // Create entity and add all attributes and relations to it
        Entity entity = createEntity(entityElement, nsContext);
        discoverAttributes(entity, entityElement, namespaceUri, nsContext);
        discoverRelations(entity, entityElement, namespaceUri, nsContext);
        
        // Add created entities, attributes and relations to the mapping
        mapping.addEntity(entity);
        Iterator<Attribute> attrIter = entity.getAttributeIterator();
        while (attrIter.hasNext()) {
          mapping.addAttribute(attrIter.next());
        }
        Iterator<Relation> relIter = entity.getRelationIterator();
        while (relIter.hasNext()) {
          mapping.addRelation(relIter.next());
        }
        
        // Set this mapping is initialized to allow further use
        mapping.setInitialized();
      }
    } catch (SAXException ex) {
      Logger.getLogger(XmlBasedMappingDeserialization.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(XmlBasedMappingDeserialization.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParserConfigurationException ex) {
      Logger.getLogger(XmlBasedMappingDeserialization.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void discoverAttributes(Entity entity, Element entityElement, 
          String namespaceUri, NsContext nsContext) {
    NodeList nl = entityElement.getElementsByTagNameNS(namespaceUri, XmlBasedMapping.attributeTagName);
    for (int i = 0; i < nl.getLength(); i++) {
      Element attributeElement = (Element) nl.item(i);
      if (attributeElement.getParentNode() != entityElement) {
        continue;
      }
      Attribute attr = createAttribute(attributeElement, nsContext);
      entity.addAttribute(attr);
    }
  }

  private void discoverRelations(Entity entity, Element entityElement, 
          String namespaceUri, NsContext nsContext) {
    NodeList nl = entityElement.getElementsByTagNameNS(namespaceUri, XmlBasedMapping.relationTagName);
    for (int i = 0; i < nl.getLength(); i++) {
      Element relationElement = (Element) nl.item(i);
      if (relationElement.getParentNode() != entityElement) {
        continue;
      }
      Relation rel = createRelation(relationElement, nsContext);
      entity.addRelation(rel);
    }
  }

  private Entity createEntity(Element entityElement, NsContext nsContext) {
    String type = parser.getUriFromPrefixedName(
            entityElement.getAttribute(XmlBasedMapping.typeAttrName), nsContext);
    String path = entityElement.getAttribute(XmlBasedMapping.pathAttrName);
    Entity entity = new Entity(type, path, nsContext);
    return entity;
  }

  private Attribute createAttribute(Element attrElement, NsContext nsContext) {
    String type = parser.getUriFromPrefixedName(
            attrElement.getAttribute(XmlBasedMapping.typeAttrName), nsContext);
    String path = attrElement.getAttribute(XmlBasedMapping.pathAttrName);
    return new Attribute(type, path);
  }

  private Relation createRelation(Element relationElement, NsContext nsContext) {
    String name = parser.getUriFromPrefixedName(
            relationElement.getAttribute(XmlBasedMapping.nameAttrName), nsContext);
    String targetEntity = parser.getUriFromPrefixedName(
            relationElement.getAttribute(XmlBasedMapping.targetEntityAttrName), nsContext);
    String path = relationElement.getAttribute(XmlBasedMapping.pathAttrName);
    return new Relation(name, path, targetEntity);
  }

}