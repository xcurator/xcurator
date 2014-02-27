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

/**
 * When the target entity instance of a relation is not children of the 
 * entity instance, one more several references are needed to locate the
 * actual target entity instance. The XPath is not enough
 * @author ekzhu
 */
public class Reference {
  
  SearchPath path;
  
  SearchPath targetPath;
  
  // Currently we only uses equality search, that is, the value returned from
  // the two paths must be equal for the two entity instnaces to have a relation.
  public Reference(String path, String targetPath) {
    this.path = new SearchPath(path);
    this.targetPath = new SearchPath(targetPath);
  }
  
  public String getPath() {
    return path.getPath();
  }
  
  public String getTargetPath() {
    return targetPath.getPath();
  }
}
