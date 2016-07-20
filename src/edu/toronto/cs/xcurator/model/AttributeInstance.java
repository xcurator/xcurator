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
 * Represents an instance of an attribute.
 *
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class AttributeInstance {

    // Eric: Moved member variables to top to be consistent
    // with other xcurator classes
    SchemaInstance schemaInstance;
    String content;
    // Need actual value for intra-linking, not just the XML tag content
    String value;

    public AttributeInstance(SchemaInstance instance, Element element)
            throws IOException {
        // Eric: The value was wrong because it includes in-tag attributes,
        // and the "term" ends up being "Candy holderd2e53", which should've been "Candy holder".
        // The following line of code fixes the problem.
        // this(instance, XMLUtils.asString(element), element.getTextContent());
        this(instance, XMLUtils.asString(element), element.getChildNodes().item(0).getNodeValue());
    }

    // Made public for schema flattening, during which the element is no longer available
    public AttributeInstance(SchemaInstance instance, String content, String value) {
        this.content = content;
        this.value = value;
        this.schemaInstance = instance;
    }

    public void setSchemaInstance(SchemaInstance si) {
        this.schemaInstance = si;
    }

    public String getContent() {
        return this.content;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeInstance) {
            AttributeInstance that = (AttributeInstance) obj;
            return content.equals(that.content)
                    && schemaInstance.equals(that.schemaInstance);
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
}
