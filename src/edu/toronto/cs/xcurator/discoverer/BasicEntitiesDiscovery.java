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
package edu.toronto.cs.xcurator.discoverer;

import com.hp.hpl.jena.vocabulary.RDF;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.xml.NsContext;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BasicEntitiesDiscovery implements MappingDiscoveryStep {

  private final String defaultTypeUriBase;
  private final XmlParser parser;
  private final String entityIdPattern;

  public BasicEntitiesDiscovery(XmlParser parser, String defaultTypeUriBase,
          String entityIdPattern) {
    this.parser = parser;
    this.defaultTypeUriBase = defaultTypeUriBase;
    this.entityIdPattern = entityIdPattern;
  }

  @Override
  public void process(Document dataDoc, Mapping mapping) {
    Element root = dataDoc.getDocumentElement();
    NsContext rootNsContext = new NsContext(root);
    String uri = parser.getElementUri(root, defaultTypeUriBase);
    Entity rootEntity = new Entity(uri, "/" + root.getNodeName(), entityIdPattern, rootNsContext);
    mapping.addEntity(rootEntity);
    mapping.setBaseNamespaceContext(rootNsContext);
    discoverEntitiesFromXmlElements(root, rootEntity, mapping);
    mapping.setInitialized();
  }

  private void discoverEntitiesFromXmlElements(Element parent, Entity parentEntity,
          Mapping mapping) {

    NodeList children = parent.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {

      if (children.item(i) instanceof Element) {
        
        Element child = (Element) children.item(i);
        
        if (parser.isLeaf(child) && 
                child.getAttributes().getLength() == 0) {
          // Transform a leaf element with no XML attributes
          // into an attribute of the parent entity
          String uri = parser.getLeafElementUri(child, parent, defaultTypeUriBase);
          String path = "/" + child.getNodeName() + "/text()";
          Attribute attr = new Attribute(uri, path);
          parentEntity.addAttribute(attr);
          continue;
        }
        
        // We have found another entity
        String uri = parser.getElementUri(child, defaultTypeUriBase);
        
        Entity childEntity = mapping.getEntity(uri);
        
        // Build the absolute path to this entity.
        String path = parentEntity.getPath() + "/" + child.getNodeName();
        
        if (childEntity == null) {
          // If we have seen not seen this entity, create new.
          childEntity = new Entity(uri, path, entityIdPattern, 
                  new NsContext(parentEntity.getNamespaceContext()));
          mapping.addEntity(childEntity);
        } else {
          // If we have seen this entity, simply merge the paths (if differ)
          childEntity.addPath(path);
        }
        
        // Create a relation about the parent and this entity
        String relationPath = child.getNodeName();
        String relationUri = parser.getRelationUri(parent, child, defaultTypeUriBase);
        Relation relation = new Relation(relationUri, relationPath, uri);
        parentEntity.addRelation(relation);
        
        // Discover the attributes of this entity from the XML attributes
        discoverAttributesFromXmlAttributes(child, childEntity);
        
        // Discover the value from the XML text node
        discoverValueFromTextContent(child, childEntity);
        
        // Recursively discover the related entities of this one
        discoverEntitiesFromXmlElements(child, childEntity, mapping);
      }
    }
  }
  
  private void discoverAttributesFromXmlAttributes(Element element, Entity entity) {
    
    // Get attribtues from the XML attributes of the element
    List<Attr> xmlAttrs = parser.getAttributes(element);
    for (Attr xmlAttr : xmlAttrs) {
      String uri = parser.getAttributeUri(xmlAttr, element, defaultTypeUriBase);
      String path = "@" + xmlAttr.getNodeName();
      Attribute attribute = new Attribute(uri, path);
      entity.addAttribute(attribute);
    }
  }
  
  private void discoverValueFromTextContent(Element element, Entity entity) {
    if (!parser.isLeaf(element)) {
      return;
    }
    String textContent = element.getTextContent().trim();
    if (!textContent.equals("")) {
      entity.addAttribute(new Attribute(
              parser.getValueAttributeUri(element, defaultTypeUriBase), 
              "text()"));
    }
  }
  
}
