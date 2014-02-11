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

import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.xml.NsContext;
import edu.toronto.cs.xcurator.xml.UriBuilder;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BasicEntitiesDiscovery implements MappingDiscoveryStep {

  private final XmlParser parser;
  private final UriBuilder uriBuilder;

  public BasicEntitiesDiscovery(XmlParser parser, UriBuilder uriBuilder) {
    this.parser = parser;
    this.uriBuilder = uriBuilder;
  }

  @Override
  public void process(List<DataDocument> dataDocuments, Mapping mapping) {
    for (DataDocument dataDoc : dataDocuments) {
      Element root = dataDoc.Data.getDocumentElement();
      NsContext rootNsContext = new NsContext(root);
      String uri = uriBuilder.getElementUri(root, rootNsContext);
      Entity rootEntity = new Entity(uri, "/" + root.getNodeName(),
              dataDoc.EntityIdPattern, rootNsContext);
      mapping.addEntity(rootEntity);
      mapping.setBaseNamespaceContext(rootNsContext);
      discoverEntitiesFromXmlElements(root, rootEntity, dataDoc, mapping);
      mapping.setInitialized();
    }
  }

  private void discoverEntitiesFromXmlElements(Element parent, Entity parentEntity,
          DataDocument dataDoc, Mapping mapping) {

    NodeList children = parent.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {

      if (children.item(i) instanceof Element) {

        Element child = (Element) children.item(i);

        if (parser.isLeaf(child)
                && child.getAttributes().getLength() == 0) {
          // Transform a leaf element with no XML attributes
          // into an attribute of the parent entity
          String uri = uriBuilder.getLeafElementUri(child, parent,
                  parentEntity.getNamespaceContext());
          String path = "/" + child.getNodeName() + "/text()";
          Attribute attr = new Attribute(uri, path);
          parentEntity.addAttribute(attr);
          continue;
        }

        // We have found another entity, get its URI and check if we have seen it.
        String uri = uriBuilder.getElementUri(child, parentEntity.getNamespaceContext());

        Entity childEntity = mapping.getEntity(uri);

        // Build the absolute path to this entity.
        String path = parentEntity.getPath() + "/" + child.getNodeName();

        if (childEntity == null) {
          // If we have seen not seen this entity, create new.
          // Create a new namespace context by inheriting from the parent
          // and discovering overriding definitions.
          NsContext nsContext = new NsContext(parentEntity.getNamespaceContext());
          nsContext.discover(child);
          childEntity = new Entity(uri, path, dataDoc.EntityIdPattern, nsContext);
          mapping.addEntity(childEntity);
        } else {
          // If we have seen this entity, simply merge the paths (if differ)
          childEntity.addPath(path);
        }

        // Create a relation about the parent and this entity
        String relationPath = child.getNodeName();
        String relationUri = uriBuilder.getRelationUri(parent, child,
                parentEntity.getNamespaceContext());
        Relation relation = new Relation(relationUri, relationPath, uri);
        parentEntity.addRelation(relation);

        // Discover the attributes of this entity from the XML attributes
        discoverAttributesFromXmlAttributes(child, childEntity);

        // Discover the value from the XML text node
        discoverValueFromTextContent(child, childEntity);

        // Recursively discover the related entities of this one
        discoverEntitiesFromXmlElements(child, childEntity, dataDoc, mapping);
      }
    }
  }

  private void discoverAttributesFromXmlAttributes(Element element, Entity entity) {

    // Get attribtues from the XML attributes of the element
    List<Attr> xmlAttrs = parser.getAttributes(element);
    for (Attr xmlAttr : xmlAttrs) {
      String uri = uriBuilder.getAttributeUri(xmlAttr, element, entity.getNamespaceContext());
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
              uriBuilder.getValueAttributeUri(element, entity.getNamespaceContext()),
              "text()"));
    }
  }

}