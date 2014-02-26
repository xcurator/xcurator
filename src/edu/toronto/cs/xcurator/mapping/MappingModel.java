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

public class MappingModel {
  
  Set<String> paths;
  
  String typeUri;
  
  public MappingModel(String typeUri, String path) {
    this.typeUri = typeUri;
    this.paths = new HashSet<>();
    paths.add(path);
  }
  
  public String getTypeUri() {
    return typeUri;
  }
  
  public void addPath(String additionalPath) {
    paths.add(additionalPath);
  }
  
  public String getPath() {
    String pathsString = "";
    int i = 0;
    Iterator<String> iter = paths.iterator();
    while (iter.hasNext()) {
      pathsString += iter.next();
      if (i != paths.size() - 1) {
        pathsString += "|";
      }
      i++;
    }
    return pathsString;
  }
  
  public String getPathWithPostfix(String postfix) {
    String pathsString = "";
    int i = 0;
    Iterator<String> iter = paths.iterator();
    while (iter.hasNext()) {
      pathsString += iter.next() + postfix;
      if (i != paths.size() - 1) {
        pathsString += "|";
      }
      i++;
    }
    return pathsString;
  }
}
