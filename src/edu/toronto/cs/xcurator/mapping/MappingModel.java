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

public class MappingModel {
  
  SearchPath searchPath;
  
  String typeUri;
  
  public MappingModel(String typeUri, String path) {
    this.typeUri = typeUri;
    this.searchPath = new SearchPath(path);
  }
  
  public String getTypeUri() {
    return typeUri;
  }
  
  public void addPath(String path) {
    searchPath.addPath(path);
  }
  
  public String getPath() {
    return searchPath.getPath();
  }
}
