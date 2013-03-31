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
 * Represents an attribute in a schema.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class Attribute {
	// Eric: Moved member variables to top to be consistent
	// with other xcurator classes
	String name;
  String path;
  Schema parent;
  boolean key;

  Set<String> typeURIs = new HashSet<String>();
  Map<SchemaInstance, Set<AttributeInstance>> instanceMap;
  Map<String, Set<AttributeInstance>> reverseInstanceMap;
	
  public Attribute(Schema parent, String name, String path, boolean key) {
    super();
    this.name = name;
    this.path = path;
    this.parent = parent;
    this.key = key;

    instanceMap = new HashMap<SchemaInstance, Set<AttributeInstance>>();
    reverseInstanceMap = new HashMap<String, Set<AttributeInstance>>();
  }

  public void addInstance(AttributeInstance instance) {
    Set<AttributeInstance> attributes =
        instanceMap.get(instance.schemaInstance);
    if (attributes == null) {
      attributes = new HashSet<AttributeInstance>();
      instanceMap.put(instance.schemaInstance, attributes);
    }
    attributes.add(instance);

    attributes = reverseInstanceMap.get(instance.content);
    if (attributes == null) {
    	attributes = new HashSet<AttributeInstance>();
      reverseInstanceMap.put(instance.content, attributes);
    }
    attributes.add(instance);
  }

  public boolean isOneToOne() {
    for (Map.Entry<SchemaInstance, Set<AttributeInstance>> entry :
        instanceMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        return false;
      }
    }

    for (Map.Entry<String, Set<AttributeInstance>> entry :
        reverseInstanceMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        return false;
      }
    }

    return true;
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
      return attr.name.equals(this.name) && attr.parent.equals(this.parent) &&
          attr.path.equals(this.path);
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
