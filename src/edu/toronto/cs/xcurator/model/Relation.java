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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a relation between two schemas.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class Relation {
  String name;
  String path;
  Schema schema;
  Schema parent;

  Set<Attribute> lookupKeys;
  Map<SchemaInstance, Set<SchemaInstance>> instanceMap;
  Map<SchemaInstance, Set<SchemaInstance>> reverseInstanceMap;

  public Relation(Schema parent, String name, String path, Schema schema,
      Set<Attribute> lookupKeys) {
    super();
    this.name = name;
    this.path = path;
    this.lookupKeys = lookupKeys;
    instanceMap = new HashMap<SchemaInstance, Set<SchemaInstance>>();
    reverseInstanceMap = new HashMap<SchemaInstance, Set<SchemaInstance>>();

    this.setParent(parent);
    this.setSchema(schema);
  }

  public void setParent(Schema parent) {
    // TODO(soheil): We might need to remove it here.
    this.parent = parent;
    parent.addRelation(this);
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
    schema.addReverseRelation(this);
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
      return relation.name.equals(this.name) &&
          relation.schema.equals(this.schema);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public void addInstance(RelationInstance instance) {
    Set<SchemaInstance> relations = instanceMap.get(instance.from);
    if (relations == null) {
      relations = new HashSet<SchemaInstance>();
      instanceMap.put(instance.from, relations);
    }
    relations.add(instance.to);

    relations = reverseInstanceMap.get(instance.to);
    if (relations == null) {
      relations = new HashSet<SchemaInstance>();
      reverseInstanceMap.put(instance.to, relations);
    }
    relations.add(instance.from);
  }

  public boolean isOneToOne() {
    for (Map.Entry<SchemaInstance, Set<SchemaInstance>> entry :
        instanceMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        return false;
      }
    }

    for (Map.Entry<SchemaInstance, Set<SchemaInstance>> entry :
        reverseInstanceMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        return false;
      }
    }
    
    return true;
  }
}