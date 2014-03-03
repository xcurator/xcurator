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

  String targetEntityXmlTypeUri;
  
  String rdfUri;
  
  Set<Reference> references;
  
  SearchPath paths;

  public Relation(String rdfUri, String path, String targetEntityXmlTypeUri) {
    this.rdfUri = rdfUri;
    this.paths = new SearchPath(path);
    this.targetEntityXmlTypeUri = targetEntityXmlTypeUri;
    this.references = new HashSet<>();
  }
  
  public void addReference(Reference reference) {
    references.add(reference);
  }
  
  public Iterator<Reference> getReferenceIterator() {
    return references.iterator();
  }

  public String getTargetEntityXmlTypeUri() {
    return targetEntityXmlTypeUri;
  }

  @Override
  public String getId() {
    return rdfUri;
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
