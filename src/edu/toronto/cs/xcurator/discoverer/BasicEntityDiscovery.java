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
import edu.toronto.cs.xcurator.mapping.Schema;
import edu.toronto.cs.xcurator.mapping.Relation;
import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.RdfUriBuilder;
import edu.toronto.cs.xcurator.common.XmlParser;
import edu.toronto.cs.xcurator.common.XmlUriBuilder;
import edu.toronto.cs.xcurator.mapping.ValueAttribute;
import java.util.List;
import javax.xml.XMLConstants;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BasicEntityDiscovery implements MappingDiscoveryStep {

    private final XmlParser parser;
    private final RdfUriBuilder rdfUriBuilder;
    private final XmlUriBuilder xmlUriBuilder;
    private boolean discoverRootLevelEntity;
    static final Logger logger = Logger.getLogger(BasicEntityDiscovery.class);

    public BasicEntityDiscovery(XmlParser parser, RdfUriBuilder rdfUriBuilder,
            XmlUriBuilder xmlUriBuilder) {
        this.parser = parser;
        this.rdfUriBuilder = rdfUriBuilder;
        this.xmlUriBuilder = xmlUriBuilder;
        this.discoverRootLevelEntity = false;
    }

    public BasicEntityDiscovery(XmlParser parser, RdfUriBuilder rdfUriBuilder,
            XmlUriBuilder xmlUriBuilder, boolean discoverRootLevelEntity) {
        this(parser, rdfUriBuilder, xmlUriBuilder);
        this.discoverRootLevelEntity = discoverRootLevelEntity;
    }

    @Override
    public void process(List<DataDocument> dataDocuments, Mapping mapping) {
        
        logger.debug("datadoc#: " + dataDocuments.size());
        for (DataDocument dataDoc : dataDocuments) {
//            logger.debug("");
            // Create a root entity from the root element.
            Element root = dataDoc.Data.getDocumentElement();
            NsContext rootNsContext = new NsContext(root);
            String rdfTypeUri = rdfUriBuilder.getRdfTypeUri(root);
            String xmlTypeUri = xmlUriBuilder.getXmlTypeUri(root);
            String path = getElementPath(root, "", "/", rootNsContext);
            Schema rootEntity = new Schema(rdfTypeUri, xmlTypeUri, rootNsContext);
            rootEntity.addPath(path);
            rootEntity.addInstance(root);

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

    private void discoverEntitiesFromXmlElements(Element parent, Schema schema,
            DataDocument dataDoc, Mapping mapping) {

        NodeList children = parent.getChildNodes();
        final int childscount = children.getLength();
        logger.debug("childcount#: " + childscount);

        for (int i = 0; i < childscount; i++) {
//            final Node child2 = children.item(i);
//            logger.debug(">>  " + child2.getLocalName());
            if (children.item(i) instanceof Element) {

                Element child = (Element) children.item(i);

                final String tempcont = child.getTextContent().trim();
                logger.debug(">> " + child.getNodeName() + " || " + tempcont.subSequence(0, Math.min(20, tempcont.length())) + " <<");

                // If the child element has no attributes, then its an attribute of
                // its parent
                if (parser.isLeaf(child)
                        && child.getAttributes().getLength() == 0) {
                    logger.debug("Leaf!");
                    discoverAttributeFromLeafElement(child, schema);
                    continue;
                }

                // We have found another entity, get its URI and check if we have seen it.
                String xmlTypeUri = xmlUriBuilder.getXmlTypeUri(child);

                // Create the RDF.type URI for this entity.
                String rdfTypeUri = rdfUriBuilder.getRdfTypeUri(child);
                logger.debug(xmlTypeUri + " " + rdfTypeUri);
                Schema childSchema = mapping.getEntity(xmlTypeUri);

                // Create a new namespace context by inheriting from the parent
                // and discovering overriding definitions.
                NsContext nsContext = new NsContext(schema.getNamespaceContext());
                nsContext.discover(child);

                // Build the absolute path to this entity.
                String path = getElementPath(child, schema.getPath(), "/", nsContext);
                logger.debug("parentPath= " + schema.getPath());
                logger.debug("NodeName=" + child.getNodeName());
                logger.debug("path= " + path);
                if (childSchema == null) {
                    // If we have seen not seen this entity, create new.
                    childSchema = new Schema(rdfTypeUri, xmlTypeUri, nsContext, child.getLocalName());
                    childSchema.addPath(path);
                    childSchema.addInstance(child);
                    mapping.addEntity(childSchema);
                } else {
                    // If we have seen this entity, simply merge the paths (if differ)
                    childSchema.addPath(path);
                    childSchema.addInstance(child);
                    // We don't override namespace context here
                    // We are assuming the input XML documents are following good practice 
                    // - using the same namespace prefixes definitions across documents
                    // If the namespace prefixes are different, should consider generating
                    // mapping for each one of them individually instead of together.
                    // So overriding or not does not matter, as there should be no conflict
                    childSchema.mergeNamespaceContext(nsContext, true);
                }

                // Create a relation about the parent and this entity
                // Use relative path for direct-descendent relation
                String relationPath = getElementPath(child, ".", "/", nsContext);
                String relationUri = rdfUriBuilder.getRdfRelationUriFromElements(parent, child);
                if (schema.hasRelation(relationUri)) {
                    Relation relation = schema.getRelation(relationUri);
                    relation.addPath(relationPath);
                } else {
                    Relation relation = new Relation(schema, childSchema, relationUri);
                    relation.addPath(relationPath);
                    schema.addRelation(relation);
                }
                // During this step, only direct parent-child entity relations are 
                // discovered. Relations based on reference keys should be discovered
                // in other steps

                // Discover the attributes of this entity from the XML attributes
                discoverAttributesFromXmlAttributes(child, childSchema);

                // Discover the value from the XML text node
                discoverValueFromTextContent(child, childSchema);

                // Recursively discover the related entities of this one
                discoverEntitiesFromXmlElements(child, childSchema, dataDoc, mapping);
            }
        }
    }

    private void discoverAttributeFromLeafElement(Element element, Schema schema) {
        // Transform a leaf element with no XML attributes
        // into an attribute of the schema
        String rdfUri = rdfUriBuilder.getRdfPropertyUri(element);
        String xmlUri = xmlUriBuilder.getXmlTypeUri(element);
        // The path is ./child_node/text(), with . being the parent node
        String path = getElementPath(element, ".", "/",
                schema.getNamespaceContext()) + "/text()";
        addAttributeToSchema(schema, rdfUri, xmlUri, path, element.getTextContent());
    }

    private void discoverAttributesFromXmlAttributes(Element element, Schema entity) {

        // Get attribtues from the XML attributes of the element
        List<Attr> xmlAttrs = parser.getAttributes(element);
        for (Attr xmlAttr : xmlAttrs) {
            String rdfUri = rdfUriBuilder.getRdfPropertyUri(xmlAttr);
            String xmlUri = xmlUriBuilder.getXmlTypeUri(xmlAttr);
            // Use relative path for attribute
            String path = getAttrPath(xmlAttr, "", "@");
            addAttributeToSchema(entity, rdfUri, xmlUri, path, xmlAttr.getTextContent());
        }
    }

    private void discoverValueFromTextContent(Element element, Schema entity) {
        if (!parser.isLeaf(element)) {
            return;
        }
        String textContent = element.getTextContent().trim();
        if (!textContent.equals("")) {
            String rdfUri = rdfUriBuilder.getRdfPropertyUriForValue(element);
            Attribute attr = new ValueAttribute(entity, rdfUri);
            attr.addPath("text()");
            entity.addAttribute(attr);
        }
    }

    private void addAttributeToSchema(Schema schema, String rdfUri, String xmlUri,
            String path, String instanceValue) {
        Attribute attr = new Attribute(schema, rdfUri, xmlUri);
        attr.addPath(path);
        attr.addInstance(instanceValue);
        schema.addAttribute(attr);
    }

    private String getElementPath(Node node, String parentPath, String separator,
            NsContext nsContext) {
        String prefix = node.getPrefix();
        if (prefix != null) {
            // When there is a namespace used, juse use the full node name
            return parentPath + separator + node.getNodeName();
        } else if (nsContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)
                .equals(XMLConstants.NULL_NS_URI)) {
            // When there is no default namespace defined, just use the local name
            return parentPath + separator + node.getLocalName();
        } else {
            // When there is a default namespace defined for this node,
            // have it empty before : in the path.
            return parentPath + separator + ":" + node.getLocalName();
        }
    }

    private String getAttrPath(Node node, String parentPath, String separator) {
        String prefix = node.getPrefix();
        return parentPath + separator + (prefix != null ? prefix + ":" : "") + node.getLocalName();
    }
}
