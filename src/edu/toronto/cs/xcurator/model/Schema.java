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
package edu.toronto.cs.xcurator.model;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * Represents a mapped type schema.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class Schema {
	
	// Eric: Moved member variables to top to be consistent
	// with other xcurator classes
	Set<String> typeURIs = new HashSet<String>();
  Element element;

  // Eric: What is the id of a schema? I do not think
  // it is currently being used.
  String id;
  String name;
  // Path should be the ABSOLUTE path
  String path;
  Schema parent;

  Set<Attribute> attributes;
  // A set of relations where the current schema is the parent
  Set<Relation> relations;
  // A set of relations where the current schema is the child
  // Eric: Why does this exist?
  // Eric: We are ALWAYS under the assumption that one schema
  // can ONLY have one parent, but multiple children, which
  // means that this set will ALWAYS only be of size one!?
  Set<Relation> reverseRelations;
  
  Set<SchemaInstance> instances;
	
  // Eric: the parent parameter is almost never set?!
  public Schema(Schema parent, Element element, String path) {
    attributes = new HashSet<Attribute>();
    relations = new HashSet<Relation>();
    reverseRelations = new HashSet<Relation>();
    instances = new HashSet<SchemaInstance>();
    this.path = path;
    this.element = element;
    this.parent = parent;
    this.name = element.getNodeName();
  }

  // Eric: Why do we need this constructor?
  public Schema(Schema parent, String name, String path) {
  	// Eric: Why isn't "instances" variable initialized here?
  	// Will come back to this.
    attributes = new HashSet<Attribute>();
    relations = new HashSet<Relation>();
    reverseRelations = new HashSet<Relation>();
    this.path = path;
    this.parent = parent;
    this.name = name;
  }

  public void addAttribute(Attribute attribute) {
    attributes.add(attribute);
  }

  public void addReverseRelation(Relation relation) { 	
    reverseRelations.add(relation);
    // This is to confirm that the schema
   	// can only have one parent schema
   	if (reverseRelations.size() > 1) {
   		// System.out.println("THIS CANNOT HAPPEN");
   	}
  }

  public Set<SchemaInstance> getInstances() {
  	return this.instances;
  }
  
  public void addRelation(Relation relation) {
    relations.add(relation);
  }

  public Set<Relation> getRelations() {
    return relations;
  }

  public void setRelations(Set<Relation> relations) {
    this.relations = relations;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Element getElement() {
    return element;
  }

  void setElement(Element element) {
    this.element = element;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<Attribute> attributes) {
    this.attributes = attributes;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return "S@ " + name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Schema) {
      Schema schema = (Schema) obj;
      return name.equals(schema.name);
    }

    return false;
  }

  public Set<String> getTypeURIs() {
    return typeURIs;
  }

  public void setTypeURIs(Set<String> typeURIs) {
    this.typeURIs = typeURIs;
  }

  public void addInstace(SchemaInstance instance) {
    instances.add(instance);
  }

}
