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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.AttributeInstance;
import edu.toronto.cs.xcurator.model.OntologyLink;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.model.RelationInstance;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xcurator.model.SchemaInstance;
import edu.toronto.cs.xcurator.generator.SchemaSimilarityMetic;
import edu.toronto.cs.xml2rdf.mapping.generator.SchemaException;
import edu.toronto.cs.xml2rdf.utils.DependencyDAG;
import edu.toronto.cs.xml2rdf.utils.DisjointSet;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class BasicDuplicateRemoval implements MappingStep {
	
	private final double schemaSimThreshold;
	private final int minimumNumberOfAttributeToMerges;
	private final SchemaSimilarityMetic schemaSimMetric;
	
	/**
   * @param minimumNumberOfAttributeToMerges The minimum number
   *  of attributes required for schemas to be merged
   */
  public BasicDuplicateRemoval(double schemaSimThreshold,
  		int minimumNumberOfAttributeToMerges,
  		SchemaSimilarityMetic schemaSimMetric) {
  	this.schemaSimThreshold = schemaSimThreshold;
    this.minimumNumberOfAttributeToMerges = minimumNumberOfAttributeToMerges;
    this.schemaSimMetric = schemaSimMetric;
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
        Schema newSchema = mergeSchemas(listOfSchemas);

        // Replace old relation schema with the merged one
        for (Schema oldSchema : schemas.values()) {
          for (Relation rel : oldSchema.getRelations()) {
            if (listOfSchemas.contains(rel.getSchema())) {
            	// Eric: This WILL result in newSchema
            	// having multiple parent schemas!!!
              rel.setSchema(newSchema);
            }
          }
        }

        // Remove all the pre-merged schemas
        for (Schema s: listOfSchemas) {
          for (Attribute attr : s.getAttributes()) {
            attr.setParent(newSchema);
          }
          schemas.remove(s.getName());
        }

        // Place the new merged schema
        schemas.put(newSchema.getName(), newSchema);
      }
    }
				
	}
	
	/*
   * Helper Function
   */
  private Schema mergeSchemas(Set<Schema> listOfSchemas) {
    String path = "";
    String name = "";

    Set<Attribute> attributes = new HashSet<Attribute>();
    Set<Relation> relations = new HashSet<Relation>();

    for (Schema s: listOfSchemas) {
    	System.out.println(s.getName());
      attributes.addAll(s.getAttributes());
      relations.addAll(s.getRelations());
      path += s.getPath() + "|";
      name += s.getName() + "_or_";
    }

    path = path.substring(0, path.length() - 1);
    name = name.substring(0, name.length() - 4);

    Schema schema = new Schema(null, name, path);
    schema.setAttributes(attributes);
    schema.setRelations(relations);
    return schema;
  }

}
