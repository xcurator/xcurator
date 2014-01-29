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
package edu.toronto.cs.xcurator.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.toronto.cs.xcurator.model.AttributeOld;
import edu.toronto.cs.xcurator.model.OntologyLink;
import edu.toronto.cs.xcurator.model.RelationOld;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xcurator.xml.NsContext;
import edu.toronto.cs.xml2rdf.mapping.Entity;
import edu.toronto.cs.xml2rdf.opencyc.OpenCycOntology;
import edu.toronto.cs.xml2rdf.utils.DependencyDAG;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xcurator.xml.XMLUtils;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;


/**
 * A mapping generator is a pipeline of mapping steps that extracts the mapping
 * schema for a semi-structured data. Note that the generator does not have any
 * transformation logic, and the logic is implemented in the steps.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public final class MappingGenerator {
  
  private Schema rootSchema = null;
  private String xcuratorNamespaceUri = "http://www.cs.toronto.edu/xcurator";
  private final List<MappingStep> pipeline;
  
  public MappingGenerator() {
    pipeline = new ArrayList<>();
  }

  /**
   * Generates the mapping.
   *
   * @param root The root element of the source document.
   * @param typePrefix The type prefix for the generated mappings.
   * @return The XML document representing the mapping.
   */
  public Document generateMapping(Element root, String typePrefix) {
    Map<String, Schema> schemas = new HashMap<>();
    for (MappingStep  step : pipeline) {
      step.process(root, schemas);
    }
    rootSchema = new Schema(XMLUtils.getSchemaUri(root, typePrefix), 
            "/", new NsContext(root));
    return serializeSchemas(schemas, typePrefix);
  }

  /**
   * Adds a mapping step to the generator. This step will be appended to the
   * existing pipeline of steps.
   *
   * @param step The mapping step.
   * @return 
   */
  public MappingGenerator addStep(MappingStep step) {
    pipeline.add(step);
    return this;
  }

  /**
   * Serializes generated schemas into an XML document.
   *
   * @param schemas The map of schemas.
   * @param idBaseUri The base URI of the ids of instances.
   * @return The serialized XML document.
   */
  private Document serializeSchemas(Map<String, Schema> schemas, String idBaseUri) {
    DependencyDAG<Schema> dependecyDAG = new DependencyDAG<Schema>();

    for (Schema schema : schemas.values()) {
      dependecyDAG.addNode(schema);
      for (RelationOld rel : schema.getRelations()) {
        if (!schemas.containsKey(rel.getChild())) {
          LogUtils.error(MappingGenerator.class,
              rel.getChild() + " Does not exist: " + rel);
        }
      }
    }

    for (Schema schema : schemas.values()) {
      for (RelationOld rel : schema.getRelations()) {
        dependecyDAG.addDependency(schema, rel.getChild());
      }
    }

    Document mappingRoot = null;
    try {
      DocumentBuilder builder = 
              edu.toronto.cs.xml2rdf.xml.XMLUtils.createNsAwareDocumentBuilder();
      mappingRoot = builder.newDocument();

      Element mappingRootElement = mappingRoot.createElementNS(
          xcuratorNamespaceUri, "xcurator:mapping");
      addNsContextToEntityElement(rootSchema.getNamespaceContext(), mappingRootElement);
      mappingRoot.appendChild(mappingRootElement);

      while (dependecyDAG.size() != 0) {
        Schema schema = dependecyDAG.removeElementWithNoDependency();
        addSchema(schema, mappingRoot, idBaseUri);
      }

    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }

    return mappingRoot;
  }

  private void addNsContextToEntityElement(NsContext nsCtx, Element entity) {
    for (Entry<String, String> ns : nsCtx.getNamespaces().entrySet()) {
      String attributeName = XMLConstants.XMLNS_ATTRIBUTE;
      if (!ns.getKey().equals("")) {
        attributeName += ":" + ns.getKey();
      }
      entity.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
              attributeName, ns.getValue());
    }
  }
  
  private void addUriBasedAttrToElement(String attrName, String typeUri, 
          Schema entitySchema, Element element) {
    String[] uriSegs = typeUri.split("#");
    String typeName = uriSegs[1];
    String prefix = rootSchema.getNamespaceContext()
            .merge(entitySchema.getNamespaceContext()).getPrefix(uriSegs[0]);
    if (prefix != null) {
      typeName = prefix + ":" + typeName;
    }
    element.setAttribute(attrName, typeName);
  }
  
  /**
   * Adds an schema the generated mapping document.
   *
   * @param schema The schema.
   * @param mappingRoot The root of XML document.
   * @param idBaseUri
   */
  private void addSchema(Schema schema, Document mappingRoot, String idBaseUri) {
    Element schemaElement = mappingRoot.createElementNS(
        xcuratorNamespaceUri, "xcurator:entity");
    schemaElement.setAttribute("path", schema.getPath());
    addUriBasedAttrToElement("type", schema.getUri(), schema, schemaElement);
    addNsContextToEntityElement(schema.getNamespaceContext(), schemaElement);
    mappingRoot.getDocumentElement().appendChild(schemaElement);
    Element idElement = mappingRoot.createElementNS(
        xcuratorNamespaceUri, "xcurator:id");
    idElement.setTextContent(idBaseUri + "${" + Entity.AUTO_GENERATED + "}");
    schemaElement.appendChild(idElement);

    if (schema instanceof OntologyLink) {
//      Element attributeElement = mappingRoot.createElementNS(
//          xcuratorNamespaceUri, "property");
//      attributeElement.setAttribute("path", "text()");
//      attributeElement.setAttribute("name", typePrefix + "name_property");
//      attributeElement.setAttribute("key", "true");
//      schemaElement.appendChild(attributeElement);


//      for (String ontologyURI : ((OntologyLink) schema).getTypeURIs()) {
//
//        String label = OpenCycOntology.getInstance()
//            .getLabelForResource(ontologyURI);
//
//        Element ontologyElement = mappingRoot.createElementNS(
//            xcuratorNamespaceUri,
//            "ontology-link");
//        ontologyElement.setAttribute("uri", ontologyURI);
//        ontologyElement.setAttribute("label", label);
//        schemaElement.appendChild(ontologyElement);
//      }

    } else {
      // TODO: reload attributes
//      for (String ontologyURI : schema.getTypeURIs()) {
//        String label =
//            OpenCycOntology.getInstance().getLabelForResource(ontologyURI);
//
//        Element ontologyElement = mappingRoot.createElementNS(
//            xcuratorNamespaceUri,
//            "ontology-link");
//        ontologyElement.setAttribute("uri", ontologyURI);
//        ontologyElement.setAttribute("label", label);
//        schemaElement.appendChild(ontologyElement);
//      }


      for (AttributeOld attribute : schema.getAttributes()) {
        Element attributeElement = mappingRoot.createElementNS(
            xcuratorNamespaceUri,
            "xcurator:property");
        attributeElement.setAttribute("path", attribute.getPath());
        addUriBasedAttrToElement("name", attribute.getUri(), schema, attributeElement);
        attributeElement.setAttribute("key", String.valueOf(attribute.isKey()));

//        for (String ontologyURI: attribute.getTypeURIs()) {
//          Element ontologyElement = mappingRoot.createElementNS(
//              xcuratorNamespaceUri,
//              "ontology-link");
//          String label =
//              OpenCycOntology.getInstance().getLabelForResource(ontologyURI);
//
//          ontologyElement.setAttribute("uri", ontologyURI);
//          ontologyElement.setAttribute("label", label);
//          attributeElement.appendChild(ontologyElement);
//        }

        schemaElement.appendChild(attributeElement);
      }

      for (RelationOld relation : schema.getRelations()) {
        Element relationElement = mappingRoot.createElementNS(
            xcuratorNamespaceUri,
            "xcurator:relation");
        relationElement.setAttribute("path", relation.getPath());
        addUriBasedAttrToElement("targetEntity", relation.getChild().getUri(), 
                schema, relationElement);
        addUriBasedAttrToElement("name", relation.getUri(), schema, relationElement);
        schemaElement.appendChild(relationElement);

        Element lookupElement = mappingRoot.createElementNS(
            xcuratorNamespaceUri,
            "xcurator:lookupkey");

        for (AttributeOld attr: relation.getLookupKeys()) {
          Element targetPropertyElement = mappingRoot.createElementNS(
              xcuratorNamespaceUri,
              "xcurator:target-property");
          targetPropertyElement.setAttribute("path", attr.getPath());
          addUriBasedAttrToElement("name", attr.getUri(), schema, targetPropertyElement);
          lookupElement.appendChild(targetPropertyElement);
        }

        relationElement.appendChild(lookupElement);
      }

    }
  }
}
