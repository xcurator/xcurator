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

import java.util.HashMap;
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

// New Classes

abstract class SchemaDemo {

  // Name of the schema
  private String name;

  public SchemaDemo(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
  
  @Override
  public abstract boolean equals(Object obj);
  
  @Override
  public abstract int hashCode();

}

abstract class SchemaInstance {
  
  // Name of the schema this instance belongs to
  private String name;

  public SchemaInstance(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
  
  @Override
  public abstract boolean equals(Object obj);
  
  @Override
  public abstract int hashCode();
  
}

class AttributeDemo extends SchemaDemo {

  // A set of UNIQUE attribute instances
  private HashSet<AttributeInstance> instances;

  public AttributeDemo(String name) {
    super(name);
    instances = new HashSet<AttributeInstance>();
  }

  // Add the attribute instance to the set (duplicate one
  // will simply not be added)
  public void addInstance(AttributeInstance instance) {
    instances.add(instance);
  }
  
  public HashSet<AttributeInstance> getInstances() {
    return this.instances;
  }

  @Override
  public boolean equals(Object obj) {
    
    if (obj instanceof AttributeDemo) {
      AttributeDemo attr = (AttributeDemo) obj;
      return (this.getName().equals(attr.getName()))
          && (this.getInstances().equals(attr.getInstances()));
    }
    
    return false;
    
  }

  @Override
  public int hashCode() {
    String hash = this.getName().hashCode() + ":" + this.getInstances().hashCode();
    return hash.hashCode();
  }

}

class EntityDemo extends SchemaDemo {

  // A set of UNIQUE entity instances
  private HashSet<EntityInstance> instances;

  public EntityDemo(String name) {
    super(name);
    instances = new HashSet<EntityInstance>();
  }

  public void addInstance(EntityInstance instance) {
    instances.add(instance);
  }
  
  public HashSet<EntityInstance> getInstances() {
    return this.instances;
  }

  @Override
  public boolean equals(Object obj) {
    
    if (obj instanceof EntityDemo) {
      EntityDemo entity = (EntityDemo) obj;
      return (this.getName().equals(entity.getName()))
          && (this.getInstances().equals(entity.getInstances()));
    }
    
    return false;
    
  }

  @Override
  public int hashCode() {
    String hash = this.getName().hashCode() + ":" + this.getInstances().hashCode();
    return hash.hashCode();
  }

}

class AttributeInstance extends SchemaInstance {
  
  // The text value of the attribute instance
  private String value;

  // Two simple components of an attribute instance,
  // the name of the attribute it belongs to, and the
  // text value of the attribute instance
  public AttributeInstance(String name, String value) {
    super(name);
    this.value = value;
  }
  
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    
    if (obj instanceof AttributeInstance) {
      AttributeInstance attrIns = (AttributeInstance) obj;
      return (this.getName().equals(attrIns.getName()))
          && (this.getValue().equals(attrIns.getValue()));
    }
    
    return false;
        
  }

  @Override
  public int hashCode() {
    // Create a UNIQUE hashcode
    String hash = this.getName().hashCode() + ":" + this.getValue().hashCode();
    return hash.hashCode();
  }
  
}

class EntityInstance extends SchemaInstance {
  
  private HashMap<String, HashSet<AttributeInstance>> attrInsMap;
  private HashMap<String, HashSet<EntityInstance>> entityInsMap;

  public EntityInstance(String name) {
    super(name);
    attrInsMap = new HashMap<String, HashSet<AttributeInstance>>();
    entityInsMap = new HashMap<String, HashSet<EntityInstance>>();
  }

  public void addValue(AttributeInstance instance) {
    HashSet<AttributeInstance> attrInsSet = attrInsMap.get(instance.getName());
    if (attrInsSet == null) {
      attrInsSet = new HashSet<AttributeInstance>();
      attrInsSet.add(instance);
      attrInsMap.put(instance.getName(), attrInsSet);
    } else {
      attrInsSet.add(instance);
    }
  }
  
  public void addValue(EntityInstance instance) {
    HashSet<EntityInstance> entityInsSet = entityInsMap.get(instance.getName());
    if (entityInsSet == null) {
      entityInsSet = new HashSet<EntityInstance>();
      entityInsSet.add(instance);
      entityInsMap.put(instance.getName(), entityInsSet);
    } else {
      entityInsSet.add(instance);
    }
  }
  
  public HashMap<String, HashSet<AttributeInstance>> getAttributeMap() {
    return this.attrInsMap;
  }
  
  public HashMap<String, HashSet<EntityInstance>> getEntityMap() {
    return this.entityInsMap;
  }
  
  @Override
  public boolean equals(Object obj) {
    
    if (obj instanceof EntityInstance) {
      EntityInstance entityIns = (EntityInstance) obj;
      return (this.getName().equals(entityIns.getName()))
          && (this.getAttributeMap().equals(entityIns.getAttributeMap()))
          && (this.getEntityMap().equals(entityIns.getEntityMap()));
    }
    
    return false;
    
  }

  @Override
  public int hashCode() {
    // Create a UNIQUE hashcode
    String hash = this.getName().hashCode() + ":"
        + this.getAttributeMap() + ":"
        + this.getEntityMap();
    return hash.hashCode();
  }
  
}