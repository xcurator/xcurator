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

import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.xml.NsContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author zhuerkan
 */
public class XmlBasedMapping implements Mapping {

  private boolean initialized;

  private String namespaceUri;

  private NsContext baseNamespaceContext;

  private Map<String, Entity> entities;

  private Map<String, Attribute> attributes;

  private Map<String, Relation> relations;

  public XmlBasedMapping() {
    this("http://www.cs.toronto.edu/xcurator");
  }

  public XmlBasedMapping(String namespaceUri) {
    this.namespaceUri = namespaceUri;
    entities = new HashMap<>();
    attributes = new HashMap<>();
    relations = new HashMap<>();
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public boolean setInitialized() {
    if (baseNamespaceContext == null) {
      return false;
    }
    if (namespaceUri == null) {
      return false;
    }
    return initialized = true;
  }

  public void setMappingNamespaceUri(String uri) {
    namespaceUri = uri;
  }

  public String getMappingNamespaceUri() {
    return namespaceUri;
  }

  @Override
  public void setBaseNamespaceContext(NsContext nsContext) {
    this.baseNamespaceContext = nsContext;
  }

  @Override
  public NsContext getBaseNamespaceContext() {
    return baseNamespaceContext;
  }

  @Override
  public void addEntity(Entity entity) {
    entities.put(entity.getTypeUri(), entity);
  }

  @Override
  public Entity getEntity(String typeUri) {
    return entities.get(typeUri);
  }

  @Override
  public Iterator<Entity> getEntityIterator() {
    return entities.values().iterator();
  }

  @Override
  public void addRelation(Relation relation) {
    relations.put(relation.getTypeUri(), relation);
  }

  @Override
  public Relation getRelation(String typeUri) {
    return relations.get(typeUri);
  }

  @Override
  public void addAttribute(Attribute attribute) {
    attributes.put(attribute.getTypeUri(), attribute);
  }

  @Override
  public Attribute getAttribute(String typeUri) {
    return attributes.get(typeUri);
  }

  public static final String entityTagName = "entity";
  public static final String attributeTagName = "property";
  public static final String relationTagName = "relation";
  public static final String idTagName = "id";
  public static final String keyAttrName = "key";
  public static final String nameAttrName = "name";
  public static final String typeAttrName = "type";
  public static final String pathAttrName = "path";
  public static final String targetEntityAttrName = "targetEntity";
}
