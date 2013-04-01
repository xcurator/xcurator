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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.AttributeInstance;
import edu.toronto.cs.xcurator.model.OntologyLink;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.model.RelationInstance;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xcurator.model.SchemaInstance;
import edu.toronto.cs.xml2rdf.mapping.generator.SchemaException;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class BasicSchemaIntralinking implements MappingStep {
	
	private final double intralinkingThreshold;
	
	public BasicSchemaIntralinking(double intralinkingThreshold) {
    this.intralinkingThreshold = intralinkingThreshold;
  }

	@Override
	public void process(Element root, Map<String, Schema> schemas) {
		
		for (Schema schema : schemas.values()) {
			
			for (Attribute attribute : schema.getAttributes()) {
				
				List<Attribute> matchedAttributes = new LinkedList<Attribute>();
				
				Set<String> propertyValues = new HashSet<String>();
				for (Set<AttributeInstance> aiSet : attribute.getInstanceMap().values()) {
					Iterator<AttributeInstance> iter = aiSet.iterator();
					while (iter.hasNext()) {
						propertyValues.add(iter.next().getValue());
					}
				}
				
				for (Schema targetSchema: schemas.values()) {
					
					// Skip current iteration when the two schemas are the same
	        if (targetSchema.equals(schema)) {
	          continue;
	        }
				
					for (Attribute targetAttribute: targetSchema.getAttributes()) {
						
						if (!targetAttribute.isKey() || targetAttribute.equals(attribute)) {
              continue;
            }
						
						Set<String> targetPropertyValues = new HashSet<String>();
						for (Set<AttributeInstance> aiSet : targetAttribute.getInstanceMap().values()) {
							Iterator<AttributeInstance> iter = aiSet.iterator();
							while (iter.hasNext()) {
								targetPropertyValues.add(iter.next().getValue());
							}
						}
						
						Set<String> sharedValues =
								org.openjena.atlas.lib.SetUtils.intersection(propertyValues,
										targetPropertyValues);
						
						if (sharedValues.size() / (double) propertyValues.size() >= this.intralinkingThreshold){
              matchedAttributes.add(targetAttribute);
              System.out.println(attribute.getName() + " : " + targetAttribute.getName());
            }

					}
				}
				
				for (Attribute matchedAttribute: matchedAttributes) {
					
					Schema taregetSchema = matchedAttribute.getParent();
					
					Set<Attribute> lookupKeys = new HashSet<Attribute>();
					
          lookupKeys.add(new Attribute(schema, matchedAttribute.getName(),
              attribute.getPath(), false));

          Relation rel = new Relation(schema, attribute.getName() + "_to_" +
              matchedAttribute.getName() + "_internal_relation", attribute.getPath(),
              taregetSchema, lookupKeys);

          schema.addRelation(rel);
					
				}
			
			}
		}
		
	}

}
