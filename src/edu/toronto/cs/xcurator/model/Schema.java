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

  public Schema(Schema parent, String name, String path) {
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

  public void addInstace(SchemaInstance instance) {
    instances.add(instance);
  }

  Set<String> typeURIs = new HashSet<String>();
  Element element;

  String id;
  String name;

  Set<Attribute> attributes;
  Set<Relation> relations;
  Set<Relation> reverseRelations;

  String path;

  Schema parent;

  Set<SchemaInstance> instances;
}
