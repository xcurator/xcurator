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

import edu.toronto.cs.xcurator.xml.NsContext;
import java.util.HashSet;
import java.util.Map;
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
  // Eric: I think the parent schema should be removed as
  // it is (they are) represented in the set reverseRelations.
  Schema parent;
  
  // The uri of the the schema, use it as the schema's unqiue id
  private String uri;
  
  // Namespace context for this schema, used for resolving xPath 
  // and URI prefixes of RDF resources
  private NsContext nscontext;

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
	
  // Eric: Why do we need parent parameter when it is NEVER set?!
  // Eric: Why do we need to keep the element? Won't keeping just
  // the name suffice?
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
  
  // ekzhu: elements are not used so ignore them for now
  public Schema(String uri, String path, NsContext nscontext) {
    attributes = new HashSet<>();
    relations = new HashSet<>();
    reverseRelations = new HashSet<>();
    instances = new HashSet<>();
    this.uri = uri;
    this.path = path;
    this.nscontext = nscontext;
  }

  // Eric: We need this constructor for duplicate removal,
  // during which the element is not accessible
  public Schema(Schema parent, String name, String path) {
    attributes = new HashSet<Attribute>();
    relations = new HashSet<Relation>();
    reverseRelations = new HashSet<Relation>();
    instances = new HashSet<SchemaInstance>();
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
  
  public void setInstances(Set<SchemaInstance> instances) {
  	this.instances = instances;
  }
  
  public void addRelation(Relation relation) {
    relations.add(relation);
  }

  public Set<Relation> getRelations() {
    return relations;
  }

  public Set<Relation> getReverseRelations() {
    return reverseRelations;
  }
  
  public void setRelations(Set<Relation> relations) {
    this.relations = relations;
  }
  
  public void setReverseRelations(Set<Relation> reverseRelations) {
    this.reverseRelations = reverseRelations;
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
  
  public void setPath(String path) {
  	this.path = path;
  }

  @Override
  public String toString() {
    return "S@ " + uri;
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Schema) {
      Schema schema = (Schema) obj;
      return uri.equals(schema.uri);
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
  
  /**
   * Get the namespace context of this schema
   * @return 
   */
  public NsContext getNamespaceContext() {
    return nscontext;
  }

  /**
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

}
