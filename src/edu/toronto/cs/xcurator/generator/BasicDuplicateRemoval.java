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

import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.AttributeInstance;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xcurator.model.SchemaInstance;
import edu.toronto.cs.xml2rdf.utils.DisjointSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;

public class BasicDuplicateRemoval implements MappingStep {

    private final double schemaSimThreshold;
    private final int minimumNumberOfAttributeToMerges;
    private final SchemaSimilarityMetic schemaSimMetric;
    private List<List<String>> duplicates = null;

    /**
     * @param minimumNumberOfAttributeToMerges The minimum number of attributes
     * required for schemas to be merged
     */
    public BasicDuplicateRemoval(double schemaSimThreshold,
            int minimumNumberOfAttributeToMerges,
            SchemaSimilarityMetic schemaSimMetric) {
        this.schemaSimThreshold = schemaSimThreshold;
        this.minimumNumberOfAttributeToMerges = minimumNumberOfAttributeToMerges;
        this.schemaSimMetric = schemaSimMetric;
    }

    // Added lists as parameters to collect keys for GUI
    public BasicDuplicateRemoval(double schemaSimThreshold,
            int minimumNumberOfAttributeToMerges,
            SchemaSimilarityMetic schemaSimMetric,
            List<List<String>> duplicates) {
        this.schemaSimThreshold = schemaSimThreshold;
        this.minimumNumberOfAttributeToMerges = minimumNumberOfAttributeToMerges;
        this.schemaSimMetric = schemaSimMetric;
        this.duplicates = duplicates;
    }

    @Override
    public void process(Element root, Map<String, Schema> schemas) {

        // The value of dSets is a hierarchy set of schemas that are considered to be
        // similar or duplicates
        Map<Schema, DisjointSet<Schema>> dSets = new HashMap<Schema, DisjointSet<Schema>>();

        for (Schema schema : schemas.values()) {
            DisjointSet<Schema> set = new DisjointSet<Schema>(schema);
            dSets.put(schema, set);
        }

        // TODO: Better way to detect duplicate pairs? For example, instead of iterating all
        // possible schema pairs, only compare those that are at the same level because its
        // unlikely the very top schema will be similar to that of almost leaf schemas
        for (Schema schema1 : schemas.values()) {
            for (Schema schema2 : schemas.values()) {
                // Skip the current schema pair if they are the same, if they do have enough
                // attributes, or if schema1 name > schema2 name to avoid inspecting
                // <schema1, schema2> and <schema2, schema1>
                if (schema1 == schema2
                        || schema1.getAttributes().size() < minimumNumberOfAttributeToMerges
                        || schema2.getAttributes().size() < minimumNumberOfAttributeToMerges
                        || schema1.getName().compareTo(schema2.getName()) > 0) {
                    continue;
                }

                // TODO: better similarity schema
                double similarity = schemaSimMetric.getSimiliarity(schema1, schema2);

                if (similarity >= schemaSimThreshold) {
                    dSets.get(schema1).union(dSets.get(schema2));
                }
            }
        }

        while (dSets.size() > 0) {

            Set<Schema> listOfSchemas = new HashSet<Schema>();

            Schema schema = dSets.keySet().iterator().next();
            listOfSchemas.add(schema);
            DisjointSet<Schema> dset = dSets.remove(schema);
            DisjointSet<Schema> rootSet = dset.find();

            for (DisjointSet<Schema> set : rootSet.getChildren()) {
                Schema similarSchema = set.getData();
                if (!schema.equals(similarSchema)) {
                    listOfSchemas.add(similarSchema);
                    dSets.remove(similarSchema);
                }
            }

            if (listOfSchemas.size() > 1) {
                // Add to the list for GUI
                if (duplicates != null) {
                    List<String> dups = new ArrayList<String>();
                    for (Schema s : listOfSchemas) {
                        dups.add(s.getName());
                    }
                    duplicates.add(dups);
                }

                // Merge similar schemas into one schema
                Schema mergedSchema = mergeSchemas(listOfSchemas);

                // Remove all the pre-merged schemas
                for (Schema s : listOfSchemas) {
                    schemas.remove(s.getName());
                }

                // Place the new merged schema
                schemas.put(mergedSchema.getName(), mergedSchema);
            }
        }

    }

    /*
     * Helper Function to merge similar schemas into one
     */
    private Schema mergeSchemas(Set<Schema> listOfSchemas) {

        // Accumulate name and path for the merged schema
        String path = "";
        String name = "";
        for (Schema s : listOfSchemas) {
            path += s.getPath() + "|"; // Works in XPath
            name += s.getName() + "_or_";
        }
        path = path.substring(0, path.length() - 1);
        name = name.substring(0, name.length() - 4);

        // Create the new merged schema
        Schema mergedSchema = new Schema(null, name, path);

        // Add all attributes, relations, and reverse relations;
        // and update their parent schema
        // All other properties of attributes, INCLUDING their
        // schema and attribute instances, remain the same
        Set<Attribute> attributes = new HashSet<Attribute>();
        Set<Relation> relations = new HashSet<Relation>();
        Set<Relation> reverseRelations = new HashSet<Relation>();

        for (Schema s : listOfSchemas) {

            for (Attribute attr : s.getAttributes()) {
                attr.setParent(mergedSchema);
                // Check if "attr" of the same name has already been
                // added to the set
                if (!attributes.contains(attr)) {
                    attributes.add(attr);
                } else {
                    // Find this existing attribute
                    Attribute existingAttr = null;
                    for (Attribute a : attributes) {
                        if (a.equals(attr)) {
                            existingAttr = a;
                        }
                    }
                    // Add instances of "attr" to "existingAttr" because
                    // they have the SAME name, but were under DIFFERENT
                    // (yet similar and to-be-merged) schemas
                    for (SchemaInstance si : attr.getInstanceMap().keySet()) {
                        for (AttributeInstance ai : attr.getInstanceMap().get(si)) {
                            existingAttr.addInstance(ai);
                        }
                    }
                }
            }

            for (Relation rel : s.getRelations()) {
                rel.setParent(mergedSchema);
                relations.add(rel);
            }

            // Accumulate name and path for the merged relation
            // Eric: Are these the correct name and path?
            String relPath = "";
            String relName = "";
            for (Relation rel : s.getReverseRelations()) {
                relPath += rel.getPath() + "|";
                relName += rel.getName() + "_or_";
            }
            relPath = relPath.substring(0, relPath.length() - 1);
            relName = relName.substring(0, relName.length() - 4);

            for (Relation rel : s.getReverseRelations()) {
                rel.setPath(relPath);
                rel.setName(relName);
                rel.setChild(mergedSchema);
                reverseRelations.add(rel);
            }

        }

        // Set attributes, relations, and reverse relations
        mergedSchema.setAttributes(attributes);
        mergedSchema.setRelations(relations);
        mergedSchema.setReverseRelations(reverseRelations);

        // Add and set all schema instances
        Set<SchemaInstance> instances = new HashSet<SchemaInstance>();
        for (Schema s : listOfSchemas) {
            instances.addAll(s.getInstances());
        }
        mergedSchema.setInstances(instances);

        return mergedSchema;
    }

}
