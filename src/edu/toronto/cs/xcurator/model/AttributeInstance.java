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
 * Represents an instace of an attribute.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class AttributeInstance {
  public AttributeInstance(SchemaInstance instance, Element element)
      throws IOException {
    this(instance, XMLUtils.asString(element));
  }

  AttributeInstance(SchemaInstance instance, String content) {
    this.content = content;
    this.schemaInstance = instance;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeInstance) {
      AttributeInstance that = (AttributeInstance) obj;
      return content.equals(that.content) &&
          schemaInstance.equals(that.schemaInstance);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return content.hashCode() ^ schemaInstance.hashCode() << 7;
  }

  @Override
  public String toString() {
    return schemaInstance + "::" + content;
  }

  SchemaInstance schemaInstance;
  String content;
}