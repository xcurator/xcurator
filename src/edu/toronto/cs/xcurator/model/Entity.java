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

import java.util.ArrayList;
import java.util.List;

public class Entity {
    
    String typeUri;
    
    String path;
    
    List<Namespace> namespaces;
    
    List<RelationOld> relations;
    
    List<AttributeOld> attributes;
    
    public static final String tagName = "entity";
    public static final String typeAttrName = "type";
    public static final String pathAttrName = "path";
    
    public Entity(String typeUri, String path) {
        this.typeUri = typeUri;
        this.path = path;
        namespaces = new ArrayList<>();
        relations = new ArrayList<>();
        attributes = new ArrayList<>();
    }
    
    public String getTypeUri() {
        return typeUri;
    }
    
    public String getPath() {
        return path;
    }
    
    public void addNamespace(Namespace ns) {
        namespaces.add(ns);
    }
    
    public void addAttribute(AttributeOld attr) {
        attributes.add(attr);
    }
    
    public void addRelation(RelationOld rl) {
        relations.add(rl);
    }
    
}
