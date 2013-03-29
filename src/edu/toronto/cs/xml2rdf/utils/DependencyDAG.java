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
package edu.toronto.cs.xml2rdf.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyDAG<T> {

  public Map<T, List<T>> dependencyMap;
  Map<T, List<T>> reverseDependencyMap;
  boolean debug = false;
  
  public DependencyDAG() {
    dependencyMap = new HashMap<T, List<T>>();
    reverseDependencyMap = new HashMap<T, List<T>>();
  }
  
  public void addNode(T node) {
    dependencyMap.put(node, new ArrayList<T>());
    reverseDependencyMap.put(node, new ArrayList<T>());
    if (debug) 
      System.out.println("adding node " + node + "  h " + node.hashCode());
  }
  
  public void addDependency(T from, T to) {
    
    if (from.equals(to)){
      LogUtils.warn(this.getClass(),"from and to are the same: " + from.toString() + " !!!");
      return;
    }
    
    if (debug)
      System.out.println("adding dependency from " + from + " to " + to + " hashcode1 : " + from.hashCode() + " hashcode2 : " + to.hashCode());
    
    List<T> list = dependencyMap.get(from);
    list.add(to);
    
    list = reverseDependencyMap.get(to);
    if (list == null) {
      System.err.println("Cannot find " + to);
    }
    list.add(from);
  }
  
  public T removeElementWithNoDependency() {
    T dependencyFreeElement = null;
    
    boolean found = false;
    // FIXME: revert to tree map!
    for (Map.Entry<T, List<T>> entry: dependencyMap.entrySet()) {
      dependencyFreeElement = entry.getKey();
      if (entry.getValue().size() == 0) {
        found = true;
        break;
      }
    }
    
    if (!found) {
      LogUtils.error(DependencyDAG.class, "ERRRRRRRRRRRRRR!");
    }
    
    if (dependencyFreeElement != null) {
      dependencyMap.remove(dependencyFreeElement);
      for (T element: reverseDependencyMap.get(dependencyFreeElement)) {
        List<T> list = dependencyMap.get(element);
        if (list != null) {
          list.remove(dependencyFreeElement);
        }
      }
      if (debug)
        System.out.println("Dependency removal : " + dependencyFreeElement);
    } else {
      System.out.println("ERRRRRR!");
    }
    
    return dependencyFreeElement;
  }
  
  public int size() { 
    return dependencyMap.size();
  }
}
