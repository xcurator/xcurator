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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Relation implements MappingModel {

  Entity subject;
  
  Entity object;
  
  String rdfUri;
  
  String objectXmlTypeUri;
  
  Set<Reference> references;
  
  SearchPath paths;

  public Relation(Entity subject, Entity object, String rdfUri) {
    this.subject = subject;
    this.object = object;
    this.rdfUri = rdfUri;
    this.references = new HashSet<>();
    this.paths = new SearchPath();
  }
  
  // This constructor is for deserializing the mapping file
  // since the object entity may not have deserialized yet.
  public Relation(Entity subject, Entity object, String rdfUri, String objectXmlTypeUri) {
    this(subject, null, rdfUri);
    this.objectXmlTypeUri = objectXmlTypeUri;
  }
  
  public void addReference(Reference reference) {
    references.add(reference);
  }
  
  public Iterator<Reference> getReferenceIterator() {
    return references.iterator();
  }

  public String getObjectXmlTypeUri() {
    return object == null ? objectXmlTypeUri : object.getXmlTypeUri();
  }

  @Override
  public String getId() {
    return rdfUri+"_"+getObjectXmlTypeUri();
  }

  @Override
  public void addPath(String path) {
    paths.addPath(path);
  }

  @Override
  public String getPath() {
    return paths.getPath();
  }
  
  public String getRdfUri() {
    return rdfUri;
  }

}
