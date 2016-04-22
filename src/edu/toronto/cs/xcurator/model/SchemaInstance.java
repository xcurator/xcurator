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

import java.io.IOException;

import org.w3c.dom.Element;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;

/**
 * Represents an instance of a schema.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class SchemaInstance {
	
	// Eric: Moved member variables to top to be consistent
	// with other xcurator classes
	// The actual XML content of the element (for now)
	String content;
	
  public SchemaInstance(Element element) throws IOException {
    this(XMLUtils.asString(element));
  }

  // Made public to allow for the independent ontologyLink
  // mapping step, which does not have element
  public SchemaInstance(String content) {
    this.content = content;
  }
  
  public String getContent() {
  	return this.content;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SchemaInstance) {
      SchemaInstance that = (SchemaInstance) obj;
      return content.equals(that.content);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return content.hashCode();
  }

  @Override
  public String toString() {
    return content;
  }
}