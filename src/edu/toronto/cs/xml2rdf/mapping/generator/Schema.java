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
package edu.toronto.cs.xml2rdf.mapping.generator;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

class Attribute {
  String name;
  String path;
  Schema parent;
  boolean key;
  
  Set<String> typeURIs = new HashSet<String>();
  
  public Attribute(Schema parent, String name, String path, boolean key) {
    super();
    this.name = name;
    this.path = path;
    this.parent = parent;
    this.key = key;
  }
  
  public void setParent(Schema parent) {
    this.parent = parent;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  
  public boolean isKey() {
    return key;
  }
  
  public void setKey(boolean key) {
    this.key = key;
  }
  
  public Schema getParent() {
    return parent;
  }
  
  @Override
  public String toString() {
    return "A@ " + name;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Attribute) {
      Attribute attr = (Attribute) obj;
      return attr.name.equals(this.name) && attr.parent.equals(this.parent) && attr.path.equals(this.path);
    }
    return super.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return name.hashCode();
  }
  
  public Set<String> getTypeURIs() {
    return typeURIs;
  }

  public void setTypeURIs(Set<String> typeURIs) {
    this.typeURIs = typeURIs;
  }

}

class Relation {
  String name;
  String path;
  Schema schema;
  Schema parent;
  
  Set<Attribute> lookupKeys;
  
  public Relation(Schema parent, String name, String path, Schema schema, Set<Attribute> lookupKeys) {
    super();
    this.parent = parent;
    this.name = name;
    this.path = path;
    this.schema = schema;
    this.lookupKeys = lookupKeys; 
  }
  
  public void setParent(Schema parent) {
    this.parent = parent;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public Schema getSchema() {
    return schema;
  }
  public void setSchema(Schema schema) {
    this.schema = schema;
  }
  
  public Set<Attribute> getLookupKeys() {
    return lookupKeys;
  }
  
  @Override
  public String toString() {
    return "R@ " + name;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Relation) {
      Relation relation = (Relation) obj;
      return relation.name.equals(this.name) && relation.schema.equals(this.schema); 
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return name.hashCode();
  }
}

class Schema {
  Set<String> typeURIs = new HashSet<String>();
  Element element;
  
  String id;
  String name;
  
  Set<Attribute> attributes;
  Set<Relation> relations;

  String path;

  Schema parent;
  
  Schema(Schema parent, Element element, String path) {
    attributes = new HashSet<Attribute>();
    relations = new HashSet<Relation>();
    this.path = path;
    this.element = element;
    this.parent = parent;
    this.name = element.getNodeName();
  }
  
  Schema(Schema parent, String name, String path) {
    attributes = new HashSet<Attribute>();
    relations = new HashSet<Relation>();
    this.path = path;
    this.parent = parent;
    this.name = name;
  }

  
  void addAttribute(Attribute attribute) {
    attributes.add(attribute);
  }
  
  void addRelation(Relation relation) {
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
  
//  public Element getElement() {
//    return element;
//  }
//
//  void setElement(Element element) {
//    this.element = element;
//  }
  
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
  
}

class OntologyLink extends Schema {

  
  OntologyLink(Schema parent, Element element, String path, Set<String> typeURIs) {
    super(parent, element, path);
    this.typeURIs = typeURIs;
  }
  
  
}
