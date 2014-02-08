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

import edu.toronto.cs.xcurator.xml.NsContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Entity {

  String instanceIdPattern;

  String typeUri;

  String path;

  NsContext namespaceContext;

  List<Relation> relations;

  List<Attribute> attributes;

  public Entity(String typeUri, String path, String instanceIdPattern, NsContext nsContext) {
    this.typeUri = typeUri;
    this.path = path;
    this.instanceIdPattern = instanceIdPattern;
    this.namespaceContext = nsContext;
    relations = new ArrayList<>();
    attributes = new ArrayList<>();
  }

  public String getTypeUri() {
    return typeUri;
  }

  public String getPath() {
    return path;
  }

  public void addAttribute(Attribute attr) {
    attributes.add(attr);
  }

  public void addRelation(Relation rl) {
    relations.add(rl);
  }

  public Iterator<Attribute> getAttributeIterator() {
    return attributes.iterator();
  }

  public Iterator<Relation> getRelationIterator() {
    return relations.iterator();
  }

  public NsContext getNamespaceContext() {
    return namespaceContext;
  }

  public String getInstanceIdPattern() {
    return instanceIdPattern;
  }

}
