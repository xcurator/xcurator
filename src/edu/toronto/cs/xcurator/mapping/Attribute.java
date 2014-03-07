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

public class Attribute implements MappingModel {
  
  SearchPath paths;
  
  String rdfUri;
  
  String xmlTypeUri;

  public Attribute(String rdfUri, String path, String xmlTypeUri) {
    this.rdfUri = rdfUri;
    this.xmlTypeUri = xmlTypeUri;
    this.paths = new SearchPath(path);
  }

  @Override
  public String getId() {
    return xmlTypeUri == null ? rdfUri : xmlTypeUri;
  }

  @Override
  public void addPath(String path) {
    paths.addPath(path);
  }

  @Override
  public String getPath() {
    return paths.getPath();
  }

  public String getRdfTypeUri() {
    return rdfUri;
  }
  
  public void resetRdfUri(String rdfUri) {
    this.rdfUri = rdfUri;
  }
}
