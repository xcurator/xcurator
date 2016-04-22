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

import java.util.Set;

import org.w3c.dom.Element;

/**
 * OntologyLink is a specific type of schema reprsenting a link to an external
 * ontology.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class OntologyLink extends Schema {
	
  public OntologyLink(Schema parent, Element element, String path,
      Set<String> typeURIs) {
    super(parent, element, path);
    this.typeURIs = typeURIs;
  }
  
  // New constructor to allow for the independent ontologyLink
  // mapping step, which does not have element
  public OntologyLink(Schema parent, String name, String path,
      Set<String> typeURIs) {
    super(parent, name, path);
    this.typeURIs = typeURIs;
  }
}
