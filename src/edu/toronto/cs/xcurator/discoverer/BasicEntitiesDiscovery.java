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

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Relation;
import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.UriBuilder;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.util.List;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BasicEntitiesDiscovery implements MappingDiscoveryStep {

  private final XmlParser parser;
  private final UriBuilder uriBuilder;
  private final boolean discoverRootLevelEntity;

  public BasicEntitiesDiscovery(XmlParser parser, UriBuilder uriBuilder) {
    this.parser = parser;
    this.uriBuilder = uriBuilder;
    this.discoverRootLevelEntity = false;
  }

  public BasicEntitiesDiscovery(XmlParser parser, UriBuilder uriBuilder,
          boolean disocverRootLevelEntity) {
    this.parser = parser;
    this.uriBuilder = uriBuilder;
    this.discoverRootLevelEntity = disocverRootLevelEntity;
  }

  @Override
  public void process(List<DataDocument> dataDocuments, Mapping mapping) {
    for (DataDocument dataDoc : dataDocuments) {

      // Create a root entity from the root element.
      Element root = dataDoc.Data.getDocumentElement();
      NsContext rootNsContext = new NsContext(root);
      uriBuilder.setNamespace(rootNsContext);
      String rdfTypeUri = uriBuilder.getRdfTypeUri(root, rootNsContext);
      String xmlTypeUri = uriBuilder.getXmlTypeUri(root);
      String path = getElementPath(root, "", "/", rootNsContext);
      Entity rootEntity = new Entity(rdfTypeUri, path, rootNsContext, xmlTypeUri);

      // If for some specific type of XML document, the root element to child node
      // relation is significant, we should add the root level entity
      if (discoverRootLevelEntity) {
        mapping.addEntity(rootEntity);
      }

      // Merge the current document namespace context to the mapping's.
      // The document namespace cannot be overrided as a document cannot be 
      // the child of another document.
      NsContext mappingNsContext = mapping.getBaseNamespaceContext();
      mappingNsContext.merge(rootNsContext, false);
      mapping.setBaseNamespaceContext(mappingNsContext);

      // Discover entities in this document
      discoverEntitiesFromXmlElements(root, rootEntity, dataDoc, mapping);
    }

    // set the mapping as initialized when this step is completed.
    mapping.setInitialized();
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
          String rdfUri = uriBuilder.getRdfPropertyUri(child, parent,
                  parentEntity.getNamespaceContext());
          String xmlUri = uriBuilder.getXmlTypeUri(child);
          // The path is ./child_node/text(), with . being the parent node
          String path = getElementPath(child, ".", "/", 
                  parentEntity.getNamespaceContext()) + "/text()";
          if (parentEntity.hasAttribute(xmlUri)) {
            Attribute attr = parentEntity.getAttribute(xmlUri);
            attr.addPath(path);
            if (rdfUri.length() < attr.getId().length()) {
              attr.resetRdfUri(rdfUri);
            }
          } else {
            Attribute attr = new Attribute(rdfUri, path, xmlUri);
            parentEntity.addAttribute(attr);
          }
          continue;
        }

        // We have found another entity, get its URI and check if we have seen it.
        String xmlTypeUri = uriBuilder.getXmlTypeUri(child);
        Entity childEntity = mapping.getEntity(xmlTypeUri);

        // Create a new namespace context by inheriting from the parent
        // and discovering overriding definitions.
        NsContext nsContext = new NsContext(parentEntity.getNamespaceContext());
        
        // Build the absolute path to this entity.
        String path = getElementPath(child, parentEntity.getPath(), "/", nsContext);
        
        // Create the RDF.type URI for this entity.
        String rdfTypeUri = uriBuilder.getRdfTypeUri(child, nsContext);
        
        if (childEntity == null) {
          // If we have seen not seen this entity, create new.
          nsContext.discover(child);
          childEntity = new Entity(rdfTypeUri, path, nsContext, xmlTypeUri);
          mapping.addEntity(childEntity);
        } else {
          // If we have seen this entity, simply merge the paths (if differ)
          childEntity.addPath(path);
          // We don't override namespace context here
          // We are assuming the input XML documents are following good practice 
          // - using the same namespace prefixes definitions across documents
          // If the namespace prefixes are different, should consider generating
          // mapping for each one of them individually instead of together.
          // So overriding or not does not matter, as there should be no conflict
          childEntity.mergeNamespaceContext(nsContext, true);
          // Also reset the RDF type uri if the new one is shorter
          if (rdfTypeUri.length() < childEntity.getId().length()) {
            childEntity.resetRdfTypeUri(rdfTypeUri);
          }
        }

        // Create a relation about the parent and this entity
        // Use relative path for direct-descendent relation
        String relationPath = getElementPath(child, ".", "/", nsContext);
        String relationUri = uriBuilder.getRdfRelationPropertyUri(parent, child,
                parentEntity.getNamespaceContext());
        if (parentEntity.hasRelation(relationUri)) {
          Relation relation = parentEntity.getRelation(relationUri);
          relation.addPath(relationPath);
        } else {
          Relation relation = new Relation(relationUri, relationPath, xmlTypeUri);
          parentEntity.addRelation(relation);
        }
        // During this step, only direct parent-child entity relations are 
        // discovered. Relations based on reference keys should be discovered
        // in other steps

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
      String rdfTypeUri = uriBuilder.getRdfPropertyUri(xmlAttr, element, entity.getNamespaceContext());
      // Use relative path for attribute
      String path = getAttrPath(xmlAttr, "", "@");
      if (entity.hasAttribute(rdfTypeUri)) {
        Attribute attr = entity.getAttribute(rdfTypeUri);
        attr.addPath(path);
      } else {
        Attribute attr = new Attribute(rdfTypeUri, path, null);
        entity.addAttribute(attr);
      }
    }
  }

  private void discoverValueFromTextContent(Element element, Entity entity) {
    if (!parser.isLeaf(element)) {
      return;
    }
    String textContent = element.getTextContent().trim();
    if (!textContent.equals("")) {
      entity.addAttribute(new Attribute(
              uriBuilder.getRdfPropertyUriForValue(element, entity.getNamespaceContext()),
              "text()", null));
    }
  }

  private String getElementPath(Node node, String parentPath, String separator, 
          NsContext nsContext) {
    String prefix = node.getPrefix();
    if (prefix != null) {
      // When there is a namespace used, juse use the full node name
      return parentPath + separator + node.getNodeName();
    } else {
      if (nsContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)
              .equals(XMLConstants.NULL_NS_URI)) {
        // When there is no default namespace defined, just use the local name
        return parentPath + separator + node.getLocalName();
      } else {
        // When there is a default namespace defined for this node,
        // have it empty before : in the path.
        return parentPath + separator + ":" + node.getLocalName();
      }
    }
  }

  private String getAttrPath(Node node, String parentPath, String separator) {
    String prefix = node.getPrefix();
    return parentPath + separator + (prefix != null ? prefix + ":" : "") + node.getLocalName();
  }
}
