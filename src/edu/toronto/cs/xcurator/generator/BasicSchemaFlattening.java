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
import edu.toronto.cs.xml2rdf.mapping.generator.SchemaException;
import edu.toronto.cs.xml2rdf.utils.DependencyDAG;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class BasicSchemaFlattening implements MappingStep {

	@Override
	public void process(Element root, Map<String, Schema> schemas) {
		DependencyDAG<Schema> dependecyDAG = new DependencyDAG<Schema>();
		
		for (Schema schema : schemas.values()) {
      dependecyDAG.addNode(schema);
    }
		
		for (Schema schema : schemas.values()) {
      for (Relation rel : schema.getRelations()) {
        dependecyDAG.addDependency(schema, rel.getChild());
      }
    }
		
		 while (dependecyDAG.size() != 0) {
			 Schema schema = dependecyDAG.removeElementWithNoDependency();
			
			 Set<Relation> oneToOneRelations = findOneToOneRelations(schema);
			 
			 for (Relation rel: oneToOneRelations) {
	        System.out.println(schema.getName() + " : " + rel.getName());
	        flattenRelation(schema, rel, schemas);
	      }
			 
		 }
	}
	
	/*
	 * Helper function
	 */
	private Set<Relation> findOneToOneRelations(Schema schema) {
		
		Set<Relation> oneToOneRelations = new HashSet<Relation>();

    for (Relation rel : schema.getRelations()) {
      if (rel.isOneToOne()) {
        oneToOneRelations.add(rel);
      }
    }

    return oneToOneRelations;
  }
	
	/*
	 * Helper function
	 */
	private void flattenRelation(Schema schema, Relation rel,
			Map<String, Schema> schemas) {

    Schema targetSchema = rel.getChild();

    // The promoted (relational) leaf node is now demoted
    // back to an attribute because of one-to-one'ness
    if (targetSchema instanceof OntologyLink) {

      String name = targetSchema.getName();

      String path = rel.getPath() + "/text()";

      Attribute attr = new Attribute(schema, name, path, false);
      attr.setTypeURIs(targetSchema.getTypeURIs());

      schema.addAttribute(attr);
      attr.setParent(schema);
    }

    // Add attributes of the relation to the schema,
    // with modification to attributes' name, path, and
    // parent schema
    for (Attribute attr: targetSchema.getAttributes()) {
      String name = targetSchema.getName() + "_" +  attr.getName();
      attr.setName(name);

      String path = rel.getPath() + "/" + attr.getPath();
      attr.setPath(path);

      schema.addAttribute(attr);
      attr.setParent(schema);
    }

    // Add relations of the relation to the schema,
    // with modification to relations' name, path, and
    // lookupKey attributes
    for (Relation targetRel: targetSchema.getRelations()) {
      String path = rel.getPath() + "/" + targetRel.getName();
      targetRel.setPath(path);

      String name = targetSchema.getName() + "_" + targetRel.getName();
      targetRel.setName(name);

      schema.addRelation(targetRel);
      targetRel.setParent(schema);

      // Eric: Shouldn't we also update the parent schema to the new one?
      for (Attribute lookupKey: targetRel.getLookupKeys()) {
        lookupKey.setPath(rel.getPath() + "/" + lookupKey.getPath());
        lookupKey.setName(lookupKey.getName().replace(rel.getName() + ".",
            rel.getName() + "_"));
      }
    }

    // Now that we port over all relations and attributes of the relation
    // to its one-to-one parent schema, remove this relation and complete
    // the flatten process
    schema.getRelations().remove(rel);

    // Remove the relation schema altogether, iff this schema is not a
    // relation of any other schemas
    maybeRemoveSchema(targetSchema, schemas);
  }
	
	/*
   * Helper Function to remove a schema iff this schema
   * is not a relation of any other schemas
   */
  private void maybeRemoveSchema(Schema schemaToBeRemoved,
  		Map<String, Schema> schemas) {

    for (Schema schema : schemas.values()) {
      // The if-continue just skips the schema of the same name
      if (schema.equals(schemaToBeRemoved)) {
        continue;
      }
      // Now we know the current schema has a
      // different name
      for (Relation relation: schema.getRelations()) {
        if (relation.getChild().equals(schemaToBeRemoved)) {
          return;
        }
      }
    }
    // Only remove the schema if it's not a relation
    // of any other schemas
    schemas.remove(schemaToBeRemoved.getName());
  }

}
