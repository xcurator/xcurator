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
package edu.toronto.cs.xcurator.generator;

import java.util.HashSet;
import java.util.Set;

import edu.toronto.cs.xcurator.model.AttributeOld;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xcurator.model.RelationOld;

public class BasicSimilarityMetric implements SchemaSimilarityMetic {
	
	@Override
  public double getSimiliarity(Schema schema1, Schema schema2) {
    Set<AttributeOld> cAttrs = getCommonAttributes(schema1, schema2);
    Set<RelationOld> cRelations = getCommonRelations(schema1, schema2);
    
    double size = schema1.getAttributes().size() + 
          schema2.getAttributes().size() +
          schema1.getRelations().size() +
          schema2.getRelations().size();
    return 2*(cAttrs.size() + cRelations.size()) / size;
  }

  private Set<RelationOld> getCommonRelations(Schema schema1, Schema schema2) {
    Set<RelationOld> ret = new HashSet<RelationOld>();
    
    for (RelationOld rel1: schema1.getRelations()) {
      for (RelationOld rel2: schema2.getRelations()) {
        if ( rel1.getName().equals(rel2.getName()) ) {
          ret.add(rel1);
          break; 
        }
      }
    }
    
    return ret;
  }

  private Set<AttributeOld> getCommonAttributes(Schema schema1, Schema schema2) {
    Set<AttributeOld> ret = new HashSet<AttributeOld>();
    
    for (AttributeOld attr1: schema1.getAttributes()) {
      for (AttributeOld attr2: schema2.getAttributes()) {
        if ( attr1.getName().equals(attr2.getName()) ) {
          ret.add(attr1);
          break; 
        }
      }
    }
    
    return ret;
  }

}
