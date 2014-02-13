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

public class BasicKeyIdentification implements MappingStep {
	
	private final double uniqunessThreshold;
	// A list of keys identified for GUI use later
	private List<String> keys = null;
	// A list of uniqueness values associated with the above keys
	private List<Double> uniquenessValues = null;
	
	public BasicKeyIdentification(double uniqunessThreshold) {
    this.uniqunessThreshold = uniqunessThreshold;
  }
	
	// Added lists as parameters to collect keys for GUI
	public BasicKeyIdentification(double uniqunessThreshold, List<String> keys, List<Double> uniquenessValues) {
    this.uniqunessThreshold = uniqunessThreshold;
    this.keys = keys;
    this.uniquenessValues = uniquenessValues;
  }

	@Override
	public void process(Element root, Map<String, Schema> schemas) {
		for (Schema schema: schemas.values()) {
			findAttributeKeysForSchema(schema);
		}
	}
	
	private void findAttributeKeysForSchema(Schema schema) {
		
		// A set of attributes that are potentials for keys
		Set<Attribute> potentialKeys = new HashSet<Attribute>();
		
		// Iterate through all attributes and find those
		// with no more than one one unique instance under
		// each unique schema instance
		for (Attribute attribute : schema.getAttributes()) {
			// In addition, compare schema instance set sizes
			// to make sure that such attribute is present
			// in all schema instances!
			if (attribute.hasUniqueAttributeInstance()
					&& attribute.getInstanceMap().size() == schema.getInstances().size()) {
				potentialKeys.add(attribute);
			}
		}
		
		// Find actual keys among potential attributes
		for (Attribute attribute : potentialKeys) {
			// For the current attribute, count for each of its text
	    // value, how many such text value has occurred across
	    // all schema instances
	    Map<String, Integer> valueMap = new HashMap<String, Integer>();
	    for (Set<AttributeInstance> aiSet : attribute.getInstanceMap().values()) {
	    	// We know there should be only one element in the set
	    	String content = aiSet.iterator().next().getContent();
	    	Integer count = valueMap.get(content);
        if (count == null) {
          count = 1;
        } else {
          count++;
        }
        valueMap.put(content, count);
	    }
	    
	    // For the current attribute, count the number of text values
      // that have occurred EXACTLY once, in another word, for the
      // current attribute, count the number of its text values that
      // have occurred EXACTLY once across all instances of the input
      // schema.
      //
      // Eric: Is this the right schema? Let's say there's 200 instances
      // of the schema, and the attribute has 100 unique values. 1 particular
      // attribute value has occurred in 101 instances of the schema, but the
      // other 99 attribute values occurred only once. This attribute will
      // be considered as a key, but is this correct?
	    //
	    // Eric: Change from "nonUnique" to "unique" to be more intuitive
      int unique = 0;
      for (Map.Entry<String, Integer> entry: valueMap.entrySet()) {
        Integer count = entry.getValue();
        if (count == 1) {
        	unique++;
        }
      }

      // Consider the attribute as a key if the attribute's text value
      // is unique "enough" (passing the threshold)
      double uniqueRatio = unique / (double) valueMap.size();
      if (uniqueRatio >= this.uniqunessThreshold) {
      	attribute.setKey(true);
      	// Add the key identified to the list for GUI use later
      	if (keys != null && uniquenessValues != null) {
      		keys.add(schema.getName() + " : " + attribute.getName());
      		uniquenessValues.add(uniqueRatio);
      	}
      }
      
		}
		
	}

}
