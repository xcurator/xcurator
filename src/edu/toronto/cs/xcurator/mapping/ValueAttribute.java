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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ekzhu
 */
public class ValueAttribute extends Attribute {

  public ValueAttribute(Entity entity, String rdfUri) {
    super(entity, rdfUri, "value");
  }
  
  @Override
  public void asKey() {
    try {
      throw new Exception("Value attribute cannot be used as key.");
    } catch (Exception ex) {
      Logger.getLogger(ValueAttribute.class.getName()).log(Level.WARNING, null, ex);
    }
  }
  
  
  @Override
  public void addInstance(String value) {
    try {
      throw new Exception("Value attribute does not keep instances.");
    } catch (Exception ex) {
      Logger.getLogger(ValueAttribute.class.getName()).log(Level.WARNING, null, ex);
    }
  }
  
  /**
   * The value attribute must not be the key
   * @return 
   */
  @Override
  public boolean isKey() {
    return false;
  }
}
