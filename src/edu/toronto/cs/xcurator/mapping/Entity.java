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
package edu.toronto.cs.xcurator.mapping;

import edu.toronto.cs.xcurator.common.NsContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Entity extends MappingModel {

  NsContext namespaceContext;

  Map<String, Relation> relations;

  Map<String, Attribute> attributes;

  public Entity(String typeUri, String path, NsContext nsContext) {
    super(typeUri, path);
    this.namespaceContext = nsContext;
    relations = new HashMap<>();
    attributes = new HashMap<>();
  }

  public void addAttribute(Attribute attr) {
    Attribute existAttr = attributes.get(attr.getTypeUri());
    if (existAttr != null) {
      existAttr.addPath(attr.getPath());
      return;
    }
    attributes.put(attr.getTypeUri(), attr);
  }

  public void addRelation(Relation rl) {
    Relation existRel = relations.get(rl.getTypeUri());
    if (existRel != null) {
      existRel.addPath(rl.getPath());
      return;
    }
    relations.put(rl.getTypeUri(), rl);
  }
  
  public void mergeNamespaceContext(NsContext nsContext, boolean override) {
    this.namespaceContext.merge(nsContext, override);
  }

  public boolean hasAttribute(String attributeTypeUri) {
    return attributes.containsKey(attributeTypeUri);
  }
  
  public boolean hasRelation(String relationTypeUri) {
    return relations.containsKey(relationTypeUri);
  }
  
  public Attribute getAttribute(String attributeTypeUri) {
    return attributes.get(attributeTypeUri);
  }
  
  public Relation getRelation(String relationTypeUri) {
    return relations.get(relationTypeUri);
  }
  
  public Iterator<Attribute> getAttributeIterator() {
    return attributes.values().iterator();
  }

  public Iterator<Relation> getRelationIterator() {
    return relations.values().iterator();
  }

  public NsContext getNamespaceContext() {
    return namespaceContext;
  }

}
