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

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Reference;
import edu.toronto.cs.xcurator.mapping.Relation;
import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
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

  private final InputStream mappingFileInputStream;
  private final XmlParser parser;

  public XmlBasedMappingDeserialization(InputStream mappingFileInputStream, XmlParser parser) {
    this.mappingFileInputStream = mappingFileInputStream;
    this.parser = parser;
  }

  @Override
  public void process(List<DataDocument> dataDocuments, Mapping mapping) {
    try {
      if (!(mapping instanceof XmlBasedMapping)) {
        throw new IllegalArgumentException("The mapping needs to be XML based.");
      }
      Document mapDoc = parser.parse(mappingFileInputStream, -1);
      Element root = mapDoc.getDocumentElement();

      // Get the namespace URI of the mapping element tags
      String namespaceUri = root.getNamespaceURI();
      ((XmlBasedMapping) mapping).setMappingNamespaceUri(namespaceUri);

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

        // Add created entities to the mapping
        mapping.addEntity(entity);

        // Set this mapping is initialized to allow further use
        mapping.setInitialized();
      }
    } catch (SAXException | IOException | ParserConfigurationException ex) {
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
      Attribute attr = createAttribute(entity, attributeElement, nsContext);
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
      Relation rel = createRelation(entity, relationElement, nsContext);
      // Discover the references from the children of relation element
      discoverReferences(rel, relationElement, namespaceUri);
      entity.addRelation(rel);
    }
  }

  private void discoverReferences(Relation relation, Element relationElement,
          String namespaceUri) {
    NodeList nl = relationElement.getElementsByTagNameNS(namespaceUri, XmlBasedMapping.referenceTagName);
    for (int i = 0; i < nl.getLength(); i++) {
      Element refElement = (Element) nl.item(i);
      if (refElement.getParentNode() != relationElement) {
        continue;
      }
      Reference ref = createReferece(refElement);
      relation.addReference(ref);
    }
  }

  private Entity createEntity(Element entityElement, NsContext nsContext) {
    String rdfTypeUri = getUriFromPrefixedName(
            entityElement.getAttribute(XmlBasedMapping.typeAttrName), nsContext);
    String xmlTypeUri = getUriFromPrefixedName(
            entityElement.getAttribute(XmlBasedMapping.xmlTypeAttrName), nsContext);
    String path = entityElement.getAttribute(XmlBasedMapping.pathAttrName);
    Entity entity = new Entity(rdfTypeUri, xmlTypeUri, nsContext);
    entity.addPath(path);
    return entity;
  }

  private Attribute createAttribute(Entity entity, Element attrElement, NsContext nsContext) {
    String rdfTypeUri = getUriFromPrefixedName(
            attrElement.getAttribute(XmlBasedMapping.nameAttrName), nsContext);
    String path = attrElement.getAttribute(XmlBasedMapping.pathAttrName);
    String xmlTypeUri = getUriFromPrefixedName(
            attrElement.getAttribute(XmlBasedMapping.xmlTypeAttrName), nsContext);
    Attribute attr = new Attribute(entity, rdfTypeUri, xmlTypeUri);
    attr.addPath(path);
    return attr;
  }

  private Relation createRelation(Entity subjectEntity, Element relationElement, NsContext nsContext) {
    String name = getUriFromPrefixedName(
            relationElement.getAttribute(XmlBasedMapping.nameAttrName), nsContext);
    String targetEntityXmlTypeUri = getUriFromPrefixedName(
            relationElement.getAttribute(XmlBasedMapping.targetEntityXmlTypeAttrName), nsContext);
    String path = relationElement.getAttribute(XmlBasedMapping.pathAttrName);
    Relation rel = new Relation(subjectEntity, null, name, targetEntityXmlTypeUri);
    rel.addPath(path);
    return rel;
  }

  private Reference createReferece(Element referenceElement) {
    String path = referenceElement.getAttribute(XmlBasedMapping.referencePathAttrName);
    String targetPath = referenceElement.getAttribute(XmlBasedMapping.referenceTargetPathAttrName);
    return new Reference(path, targetPath);
  }

  private String getUriFromPrefixedName(String prefixedName, NsContext nsContext) {
    if (prefixedName.contains(":")) {
      // Apply the split only 1 time to get the first prefix
      // There may be more prefix in the name but we choose to ignore them
      // for now. Need to change the way it was serialized first.
      String[] segs = prefixedName.split(":", 2);

      assert (segs.length == 2); // This is temporary.
      // We need more elaborate way of parsing to make sure the result is 
      // correct.
      String baseUri = nsContext.getNamespaceURI(segs[0]);
      return (baseUri.endsWith("/") ? baseUri : baseUri + "/") + segs[1];
    } else {
      return prefixedName;
    }
  }

}
