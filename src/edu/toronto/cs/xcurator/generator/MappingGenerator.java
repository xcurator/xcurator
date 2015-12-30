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

import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.OntologyLink;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xml2rdf.mapping.Entity;
import edu.toronto.cs.xml2rdf.opencyc.OpenCycOntology;
import edu.toronto.cs.xml2rdf.utils.DependencyDAG;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A mapping generator is a pipeline of mapping steps that extracts the mapping
 * schema for a semi-structured data. Note that the generator does not have any
 * transformation logic, and the logic is implemented in the steps.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public final class MappingGenerator {

    private final List<MappingStep> pipeline;

    public MappingGenerator() {
        pipeline = new ArrayList<MappingStep>();
    }

    /**
     * Generates the mapping.
     *
     * @param root The root element of the source document.
     * @param typePrefix The type prefix for the generated mappings.
     * @return The XML document representing the mapping.
     */
    public Document generateMapping(Element root, String typePrefix) {
        Map<String, Schema> schemas = new ConcurrentHashMap<String, Schema>();
        for (MappingStep step : pipeline) {
            step.process(root, schemas);
        }
        return serializeSchemas(schemas, typePrefix);
    }

    /**
     * Adds a mapping step to the generator. This step will be appended to the
     * existing pipeline of steps.
     *
     * @param step The mapping step.
     */
    public MappingGenerator addStep(MappingStep step) {
        pipeline.add(step);
        return this;
    }

    /**
     * Serializes generated schemas into an XML document.
     *
     * @param schemas The map of schemas.
     * @param typePrefix The URI type prefix.
     * @return The serialized XML document.
     */
    private Document serializeSchemas(Map<String, Schema> schemas,
            String typePrefix) {
        DependencyDAG<Schema> dependecyDAG = new DependencyDAG<Schema>();

        for (Schema schema : schemas.values()) {
            dependecyDAG.addNode(schema);
            for (Relation rel : schema.getRelations()) {
                if (!schemas.containsKey(rel.getChild())) {
                    LogUtils.error(MappingGenerator.class,
                            rel.getChild() + " Does not exist: " + rel);
                }
            }
        }

        for (Schema schema : schemas.values()) {
            for (Relation rel : schema.getRelations()) {
                dependecyDAG.addDependency(schema, rel.getChild());
            }
        }

        Document mappingRoot = null;
        try {
            DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
            mappingRoot = builder.newDocument();

            Element rootElement = mappingRoot.createElementNS(
                    "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "mapping");
            mappingRoot.appendChild(rootElement);

            while (dependecyDAG.size() != 0) {
                Schema schema = dependecyDAG.removeElementWithNoDependency();
                addSchema(schema, mappingRoot, typePrefix);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return mappingRoot;
    }

    /**
     * Adds an schema the generated mapping document.
     *
     * @param schema The schema.
     * @param mappingRoot The root of XML document.
     * @param path
     * @param typePrefix
     */
    private void addSchema(Schema schema, Document mappingRoot,
            String typePrefix) {
        Element schemaElement = mappingRoot.createElementNS(
                "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "entity");
        schemaElement.setAttribute("path", schema.getPath());
        schemaElement.setAttribute("type", typePrefix
                + schema.getName());
        mappingRoot.getDocumentElement().appendChild(schemaElement);
        Element idElement = mappingRoot.createElementNS(
                "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "id");
        idElement.setTextContent(typePrefix + "${" + Entity.AUTO_GENERATED + "}");
        schemaElement.appendChild(idElement);

        if (schema instanceof OntologyLink) {
            Element attributeElement = mappingRoot.createElementNS(
                    "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "property");
            attributeElement.setAttribute("path", "text()");
            attributeElement.setAttribute("name", typePrefix + "name_property");
            attributeElement.setAttribute("key", "true");
            schemaElement.appendChild(attributeElement);

            for (String ontologyURI : ((OntologyLink) schema).getTypeURIs()) {

                String label = OpenCycOntology.getInstance()
                        .getLabelForResource(ontologyURI);

                Element ontologyElement = mappingRoot.createElementNS(
                        "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                        "ontology-link");
                ontologyElement.setAttribute("uri", ontologyURI);
                ontologyElement.setAttribute("label", label);
                schemaElement.appendChild(ontologyElement);
            }

        } else {
            // TODO: reload attributes
            for (String ontologyURI : schema.getTypeURIs()) {
                String label
                        = OpenCycOntology.getInstance().getLabelForResource(ontologyURI);

                Element ontologyElement = mappingRoot.createElementNS(
                        "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                        "ontology-link");
                ontologyElement.setAttribute("uri", ontologyURI);
                ontologyElement.setAttribute("label", label);
                schemaElement.appendChild(ontologyElement);
            }

            for (Attribute attribute : schema.getAttributes()) {
                Element attributeElement = mappingRoot.createElementNS(
                        "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                        "property");
                attributeElement.setAttribute("path", attribute.getPath());
                attributeElement.setAttribute("name",
                        typePrefix + attribute.getName() + "_property");
                attributeElement.setAttribute("key", String.valueOf(attribute.isKey()));

                for (String ontologyURI : attribute.getTypeURIs()) {
                    Element ontologyElement = mappingRoot.createElementNS(
                            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                            "ontology-link");
                    String label
                            = OpenCycOntology.getInstance().getLabelForResource(ontologyURI);

                    ontologyElement.setAttribute("uri", ontologyURI);
                    ontologyElement.setAttribute("label", label);
                    attributeElement.appendChild(ontologyElement);
                }

                schemaElement.appendChild(attributeElement);
            }

            for (Relation relation : schema.getRelations()) {
                Element relationElement = mappingRoot.createElementNS(
                        "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                        "relation");
                relationElement.setAttribute("path", relation.getPath());
                relationElement.setAttribute("targetEntity", typePrefix
                        + relation.getChild().getName());
                relationElement.setAttribute("name",
                        typePrefix + relation.getName() + "_rel");
                schemaElement.appendChild(relationElement);

                Element lookupElement = mappingRoot.createElementNS(
                        "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                        "lookupkey");

                for (Attribute attr : relation.getLookupKeys()) {
                    Element targetPropertyElement = mappingRoot.createElementNS(
                            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
                            "target-property");
                    targetPropertyElement.setAttribute("path", attr.getPath());
//          String name = attr.getName();
//          String[] nameSplitted = name.split("\\.");
//          String newName = nameSplitted[0];
//          for (int i = 1; i < nameSplitted.length - 1; i++) {
//            newName += "." + nameSplitted[i] + "_rel";
//          }
//
//          if (nameSplitted.length == 1) {
//            newName += "_prop";
//          } else {
//            newName += nameSplitted[nameSplitted.length - 1] + "_prop";
//          }

                    targetPropertyElement.setAttribute("name",
                            typePrefix + attr.getName());
                    lookupElement.appendChild(targetPropertyElement);
                }

                relationElement.appendChild(lookupElement);
            }

        }
    }
}
