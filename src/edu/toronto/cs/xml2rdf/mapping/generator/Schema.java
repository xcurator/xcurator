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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;

/**
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
// TODO(soheil, eric): We need to update instances when merging two schemas.
class AttributeInstance {

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
            return content.equals(that.content) && schemaInstance.equals(that.schemaInstance);
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

class Attribute {

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
        Set<AttributeInstance> attributes
                = instanceMap.get(instance.schemaInstance);
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
        for (Map.Entry<SchemaInstance, Set<AttributeInstance>> entry
                : instanceMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                return false;
            }
        }

        for (Map.Entry<String, Set<AttributeInstance>> entry
                : reverseInstanceMap.entrySet()) {
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

class RelationInstance {

    public RelationInstance(SchemaInstance from, SchemaInstance to) {
        this.from = from;
        this.to = to;
    }

    SchemaInstance from;
    SchemaInstance to;

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

class Relation {

    String name;
    String path;
    Schema schema;
    Schema parent;

    Set<Attribute> lookupKeys;
    Map<SchemaInstance, Set<RelationInstance>> instanceMap;
    Map<SchemaInstance, Set<RelationInstance>> reverseInstanceMap;

    public Relation(Schema parent, String name, String path, Schema schema, Set<Attribute> lookupKeys) {
        super();
        this.name = name;
        this.path = path;
        this.lookupKeys = lookupKeys;
        instanceMap = new HashMap<SchemaInstance, Set<RelationInstance>>();
        reverseInstanceMap = new HashMap<SchemaInstance, Set<RelationInstance>>();

        this.setParent(parent);
        this.setSchema(schema);
    }

    public void setParent(Schema parent) {
        // TODO(soheil): We might need to remove it here.
        this.parent = parent;
        parent.addRelation(this);
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
        schema.addReverseRelation(this);
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

    public void addInstance(RelationInstance instance) {
        Set<RelationInstance> relations = instanceMap.get(instance.from);
        if (relations == null) {
            relations = new HashSet<RelationInstance>();
            instanceMap.put(instance.from, relations);
        }
        relations.add(instance);

        relations = reverseInstanceMap.get(instance.to);
        if (relations == null) {
            relations = new HashSet<RelationInstance>();
            reverseInstanceMap.put(instance.to, relations);
        }
        relations.add(instance);
    }

    public boolean isOneToOne() {
        for (Map.Entry<SchemaInstance, Set<RelationInstance>> entry
                : instanceMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                return false;
            }
        }

        for (Map.Entry<SchemaInstance, Set<RelationInstance>> entry
                : reverseInstanceMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                return false;
            }
        }

        return true;
    }
}

class SchemaInstance {

    public SchemaInstance(Element element) throws IOException {
        this(XMLUtils.asString(element));
    }

    SchemaInstance(String content) {
        this.content = content;
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

    String content;
}

class Schema {

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

    Schema(Schema parent, Element element, String path) {
        attributes = new HashSet<Attribute>();
        relations = new HashSet<Relation>();
        reverseRelations = new HashSet<Relation>();
        instances = new HashSet<SchemaInstance>();
        this.path = path;
        this.element = element;
        this.parent = parent;
        this.name = element.getNodeName();
    }

    Schema(Schema parent, String name, String path) {
        attributes = new HashSet<Attribute>();
        relations = new HashSet<Relation>();
        reverseRelations = new HashSet<Relation>();
        this.path = path;
        this.parent = parent;
        this.name = name;
    }

    void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    void addReverseRelation(Relation relation) {
        reverseRelations.add(relation);
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
