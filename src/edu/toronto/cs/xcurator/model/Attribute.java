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
  private String uri;
  String path;
  Schema parent;
  boolean key;

  Set<String> typeURIs = new HashSet<String>();
  Map<SchemaInstance, Set<AttributeInstance>> instanceMap;
  Map<String, Set<AttributeInstance>> reverseInstanceMap;
	
  // Eric: I think when an attribute is created, its key value should
  // automatically be false, until later specified otherwise (most
  // probably by the key identification mapping step). This means that
  // the constructor does not need to take key as a parameter.
  public Attribute(Schema parent, String name, String path, boolean key) {
  	// Eric: Do we need super() here? Attribute class does not extend
  	// any class.
    super();
    this.name = name;
    this.path = path;
    this.parent = parent;
    this.key = key;

    instanceMap = new HashMap<SchemaInstance, Set<AttributeInstance>>();
    reverseInstanceMap = new HashMap<String, Set<AttributeInstance>>();
  }
  
  public Attribute(Schema parent, String uri, String path) {
    this.uri = uri;
    this.path = path;
    this.parent = parent;
    this.key = false;

    instanceMap = new HashMap<>();
    reverseInstanceMap = new HashMap<>();
  }

  public void addInstance(AttributeInstance instance) {
  	addInstance(instance, this.instanceMap,
  			this.reverseInstanceMap);
  }
  
  // Added maps as parameters for the function to be re-used.
  public void addInstance(AttributeInstance instance,
  		Map<SchemaInstance, Set<AttributeInstance>> instanceMap,
  		Map<String, Set<AttributeInstance>> reverseInstanceMap) {
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
  
  public boolean hasUniqueAttributeInstance() {
  	for (Map.Entry<SchemaInstance, Set<AttributeInstance>> entry :
	      instanceMap.entrySet()) {
	    if (entry.getValue().size() > 1) {
	      return false;
	    }
	  }
  	return true;
  }
  
  // Used during schema flattening, where the original parent schema
  // is replaced by the new parent schema, with whom the original parent
  // schema has a one-to-one relation.
  public void updateAttributeInstances(Relation rel) {
  	
  	// Create new maps to replace the current ones later
  	Map<SchemaInstance, Set<AttributeInstance>> newInstanceMap =
  	 new HashMap<SchemaInstance, Set<AttributeInstance>>();
    Map<String, Set<AttributeInstance>> newReverseInstanceMap =
    		new HashMap<String, Set<AttributeInstance>>();
    
    // Iterate through all schema instances
    for (SchemaInstance parentSI : this.instanceMap.keySet()) {
    	// Find the parent schema instance to the current
    	// schema instance.
    	// Due to a schema can have multiple parents, we know that for
    	// the current schema instance, if it exists in the current
    	// relation, there must be EXACTLY one parent schema due to 
    	// one-to-one relation
    	Set<SchemaInstance> grandSISet = rel.getReverseInstanceMap().get(parentSI);
    	SchemaInstance grandSI = null;
    	// Take into account that the current schema instance may not exist
    	// in the current relation, but some other relation because this schema
    	// can have multiple parents
    	if (grandSISet != null) {
    		if (grandSISet.size() != 1) {
	    		System.out.println("MORE THAN ONE GRAND PARENT SCHEMA INSTANCE. SOMETHING IS WRONG!");
	    	} else {
	    		grandSI = grandSISet.iterator().next();
	    	}
	    	// Iterate through all attribute instances
	    	for (AttributeInstance ai : this.instanceMap.get(parentSI)) {
	    		// Update its schema instance
	    		ai.setSchemaInstance(grandSI);
	    		// Add it to the new maps
	    		addInstance(ai, newInstanceMap, newReverseInstanceMap);
	    	}
	    }
    }
    
    // Replace current maps with the new ones
    this.instanceMap = newInstanceMap;
    this.reverseInstanceMap = newReverseInstanceMap;
  	
  }
  
  public Map<SchemaInstance, Set<AttributeInstance>> getInstanceMap() {
  	return this.instanceMap;
  }
  
  public Map<String, Set<AttributeInstance>> getReverseInstanceMap() {
  	return this.reverseInstanceMap;
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
    return "A@ " + uri;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Attribute) {
      Attribute attr = (Attribute) obj;
      return attr.uri.equals(this.uri) && attr.parent.equals(this.parent) &&
          attr.path.equals(this.path);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  public Set<String> getTypeURIs() {
    return typeURIs;
  }

  public void setTypeURIs(Set<String> typeURIs) {
    this.typeURIs = typeURIs;
  }

  /**
   * @return the uri
   */
  public String getUri() {
    return uri;
  }
}
