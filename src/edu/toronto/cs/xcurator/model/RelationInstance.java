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

/**
 * Represents an instance of a relation.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class RelationInstance {
	
	// Eric: Moved member variables to top to be consistent
	// with other xcurator classes
	SchemaInstance from;
  SchemaInstance to;
	
  public RelationInstance(SchemaInstance from, SchemaInstance to) {
    this.from = from;
    this.to = to;
  }
  
  @Override
  public int hashCode() {
    return from.hashCode() ^ to.hashCode() << 7;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RelationInstance) {
      RelationInstance that = (RelationInstance) obj;
      return from.equals(that.from) && to.equals(that.to);
    }
    return false;
  }

  @Override
  public String toString() {
    return "(" + from + "," + to + ")";
  }
}