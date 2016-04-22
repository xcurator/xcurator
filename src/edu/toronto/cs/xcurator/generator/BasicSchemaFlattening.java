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
import edu.toronto.cs.xcurator.model.OntologyLinkInstance;
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

    	// Get the name and path
      String name = targetSchema.getName();
      String path = rel.getPath() + "/text()";

      // Create the new attribute
      Attribute attr = new Attribute(schema, name, path, false);
      attr.setTypeURIs(targetSchema.getTypeURIs());

      // Add the attribute
      schema.addAttribute(attr);
      
      // Convert the relation instances back to attribute instances
      for (SchemaInstance from : rel.getInstanceMap().keySet()) {
      	for (SchemaInstance to : rel.getInstanceMap().get(from)) {
      		// We know "to" must be an ontologyLinkInstance
      		OntologyLinkInstance toOI = (OntologyLinkInstance) to;
      		// Create and add an attribute instance
      		AttributeInstance ai = new AttributeInstance(from, toOI.getContent(), toOI.getValue());
      		attr.addInstance(ai);
      	}
      }
      
    }

    // Add attributes of the relation to the schema,
    // with modification to attributes' name, path, and
    // parent schema
    //
    // NOTE: If targetSchema is an ontologyLink schema,
    // then it should NOT have any attributes
    for (Attribute attr: targetSchema.getAttributes()) {
    	
      String name = targetSchema.getName() + "_" +  attr.getName();
      attr.setName(name);

      String path = rel.getPath() + "/" + attr.getPath();
      attr.setPath(path);

      schema.addAttribute(attr);
      attr.setParent(schema);
      
      // Because the parent schema of the attribute is changed, its
      // instance caching, that is, its map instanceMap and
      // reverseInstanceMap must be modified to reflect that.
      // This CAN be done because we know the attribute's original
      // parent schema and the new parent schema (original parent
      // schema's parent schema) have a one-to-one relation.
      attr.updateAttributeInstances(rel);
      
    }

    // Add relations of the relation to the schema,
    // with modification to relations' name, path, and
    // lookupKey attributes
    //
    // NOTE: If targetSchema is an ontologyLink schema,
    // then it should NOT have any child relation, BUT
    // it will have one element in its reverseRelations,
    // aka, its relation with its parent schema
    for (Relation targetRel: targetSchema.getRelations()) {
    	
    	// Get the new name and path
    	String name = targetSchema.getName() + "_" + targetRel.getName();
    	String path = rel.getPath() + "/" + targetRel.getName();
    	
    	// Update the new lookupKeys
    	for (Attribute lookupKey: targetRel.getLookupKeys()) {
        lookupKey.setPath(rel.getPath() + "/" + lookupKey.getPath());
        // The following name seem to make more sense 
        lookupKey.setName(lookupKey.getName().replace(rel.getName() + ".",
        		targetSchema.getName() + "_" + targetRel.getName()));
        // lookupKey.setName(lookupKey.getName().replace(rel.getName() + ".",
        //     rel.getName() + "_"));
      }
    	
    	// Create a new relation
    	Relation newRel = new Relation(schema, name, path, targetRel.getChild(),
    			targetRel.getLookupKeys());

    	// Update and add all the relation instances
    	for (SchemaInstance from : targetRel.getInstanceMap().keySet()) {
    		// From the parent schema instance of "from" and we know
    		// there must be only one because of the one-to-one relation
    		// Due to a schema can have multiple parents, we know that for
      	// the current schema instance, if it exists in the current
      	// relation, there must be EXACTLY one parent schema due to 
      	// one-to-one relation
    		Set<SchemaInstance> grandFromSet = rel.getReverseInstanceMap().get(from);
      	SchemaInstance grandFrom = null;
      	// Take into account that the current schema instance may not exist
      	// in the current relation, but some other relation because this schema
      	// can have multiple parents
      	if (grandFromSet != null) {
	      	if (grandFromSet.size() != 1) {
	      		System.out.println("MORE THAN ONE GRAND PARENT SCHEMA INSTANCE. SOMETHING IS WRONG!");
	      	} else {
	      		grandFrom = grandFromSet.iterator().next();
	      	}
	      	for (SchemaInstance to : targetRel.getInstanceMap().get(from)) {
	      		RelationInstance ri = new RelationInstance(grandFrom, to);
	      		newRel.addInstance(ri);
	      	}
	      }
    	}
    	
    }

    // Now that we port over all relations and attributes of the relation
    // to its one-to-one parent schema, remove this relation and complete
    // the flattening process
    schema.getRelations().remove(rel);
    targetSchema.getReverseRelations().remove(rel);

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
      // Now we know the current schema has a different name
      for (Relation relation: schema.getRelations()) {
        if (relation.getChild().equals(schemaToBeRemoved)) {
        	// Eric: This would ONLY happen if schemaToBeRemoved
        	// has more than one element in its reverseRelations.
        	// Am I correct to say this?
          return;
        }
      }
    }
    // Only remove the schema if it's not a relation
    // of any other schemas
    //
    // Eric: By doing this, we discard ALL its instance
    // caching, is this the right thing to do?
    schemas.remove(schemaToBeRemoved.getName());
  }

}
