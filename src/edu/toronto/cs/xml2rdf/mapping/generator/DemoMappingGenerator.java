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
package edu.toronto.cs.xml2rdf.mapping.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xeustechnologies.googleapi.spelling.SpellChecker;
import org.xeustechnologies.googleapi.spelling.SpellResponse;

import edu.toronto.cs.xml2rdf.freebase.FreeBaseLinker;
import edu.toronto.cs.xml2rdf.mapping.Entity;
import edu.toronto.cs.xml2rdf.mapping.generator.MappingGenerator.MappingStep;
import edu.toronto.cs.xml2rdf.opencyc.OpenCycOntology;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.utils.DependencyDAG;
import edu.toronto.cs.xml2rdf.utils.DisjointSet;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

/*
 * Author: Eric Yao
 */
public class DemoMappingGenerator implements MappingGenerator {

  private HashMap<String, AttributeDemo> attributeMap = new HashMap<String, AttributeDemo>();
  private HashMap<String, EntityDemo> entityMap = new HashMap<String, EntityDemo>();

  // Flag for printing debugging information
  static boolean debug = false;

  // Ceilings
  private int maxElement;
  private int maxOnotlogyLookup;

  // Mapping essentials
  Map<String, Schema> schemas = new HashMap<String, Schema>();
  private List<MappingStep> enabledSteps;

  // Metrics
  private StringMetric stringMetric;
  private SchemaSimilarityMetic schemaSimMetric;

  // All thresholds
  private double ontologyMatchingThreshold;
  private double schemaSimThreshold;
  private int leafPromotionThreshold = 5;
  private double matchThreshold = 0.75;
  private double ignoredNumbers = 0.25;
  private int minimumNumberOfAttributeToMerges = 2;
  private double intralinkingThreshold;

  /*
   * Constructor that initialize all threshold parameters.
   * TODO: Design algorithms to estimate the thresholds so that they do not
   * need to be manually assigned.
   */
  public DemoMappingGenerator(double ontologyMatchingThreshold,
      StringMetric stringMetric, double schemaSimThreshold,
      SchemaSimilarityMetic schemaSimMetric, 
      int leafPromotionThreshold, double matchThreshold,
      int maxElement, int maxOntologyLookup,
      double ignoredNumbers, 
      int minimumNumberOfAttributeToMerges,
      double internalLinkingThreshold, MappingStep... enabledSteps) {
    this.ontologyMatchingThreshold = ontologyMatchingThreshold;
    this.schemaSimMetric = schemaSimMetric;
    this.stringMetric = stringMetric;
    this.schemaSimThreshold = schemaSimThreshold;
    this.matchThreshold = matchThreshold;
    this.leafPromotionThreshold = leafPromotionThreshold;
    this.maxElement = maxElement;
    this.maxOnotlogyLookup = maxOntologyLookup;
    this.ignoredNumbers = ignoredNumbers;
    this.minimumNumberOfAttributeToMerges = minimumNumberOfAttributeToMerges;
    this.intralinkingThreshold = internalLinkingThreshold;
    this.enabledSteps = Arrays.asList(
        enabledSteps == null || enabledSteps.length == 0 ?
            MappingStep.values() : enabledSteps);
  }

  @Override
  public Document generateMapping(Element rootDoc, String typePrefix) {

    // The organization of the XML files should have "clinical_studies" as the
    // very root document element (which is passed in as rootDoc), with many
    // "clinical_study" child nodes, which is the children variable below.
    NodeList children = rootDoc.getChildNodes();

    // Iterate through all child nodes or up to the maximum number specified,
    // and process (merge) ONLY child nodes that are elements.
    for (int i = 0; i < children.getLength() && (maxElement == -1 || i < maxElement); i++) {
      if (children.item(i) instanceof Element) {

        // Get the child element instance
        Element child = (Element) children.item(i);

        // Merge the child element instance
        mergeWithSchema(child);
      }
    }

    // The function is not flattening the schema just yet.
    // It is now only returning the One-to-One relations of the schema.
    Map<String, Set<String>> OTOMap = flattenSchema();

    // Print the One-to-One relation
    for (String key : OTOMap.keySet()) {
      System.out.println(key + "\t" + OTOMap.get(key));
    }

    // Debug code for printing maps of maps of maps. Please ignore.
    //
    //		for (String k1 : relationsMap.keySet()) {
    //			if (k1.equals("clinical_study")){
    //				System.out.println(k1);
    //				Map<Integer, Map<String, Set<Integer>>> v1 = relationsMap.get(k1);
    //				for (Integer k2 : v1.keySet()) {
    //					System.out.println("\t" + k2);
    //					Map<String, Set<Integer>> v2 = v1.get(k2);
    //					for (String k3 : v2.keySet()) {
    //						System.out.println("\t\t" + k3);
    //						Set<Integer> v3 = v2.get(k3);
    //						System.out.println("\t\t\t" + v3);
    //					}
    //				}
    //			}
    //		}
    //
    //		System.out.println("----------");
    //
    //		for (String k1 : attributeMap.keySet()) {
    //			System.out.println(k1);
    //			Map<Integer, String> v1 = attributeMap.get(k1);
    //			for (Integer k2 : v1.keySet()) {
    //				System.out.println("\t" + k2 + "\t" + v1.get(k2));
    //			}
    //		}
    //
    //		System.out.println(relationsMapR.get("intervention_browse"));
    //		System.out.println(attributeMapR.get("mesh_term").get("Omeprazole"));

    return null;
  }

  private SchemaInstance mergeWithSchema(Element element) {

    if (XMLUtils.isLeaf(element)) {
      // Base case, the element is a leaf node and thus an attribute

      // Get the attribute name and attribute text value
      String name = element.getNodeName();
      String value = element.getTextContent();

      // Check to see if the attribute exists
      AttributeDemo attr = attributeMap.get(name);
      if (attr == null) {
        attr = new AttributeDemo(name);
        attributeMap.put(name, attr);
      }
      
      // Create an attribute instance
      AttributeInstance attrIns = new AttributeInstance(name, value);
      
      // Add the attribute instance to the attribute
      // Note: the duplicate attribute instance will not
      // be added to the attribute due to the use of set
      attr.addInstance(attrIns);

      // Return the attribute instance
      return attrIns;

    } else {
      // Recursive case, the element is not a leaf node

      // Get the name of the current entity
      String name = element.getNodeName();
      
      // Check to see if the attribute exists
      EntityDemo entity = entityMap.get(name);
      if (entity == null) {
        entity = new EntityDemo(name);
        entityMap.put(name, entity);
      }
      
      // Get the children of the current element, possibly
      // a combination of attributes and entities
      NodeList children = element.getChildNodes();

      // Create a entity instance to be filled by the for loop
      EntityInstance entityIns = new EntityInstance(name);

      // Iterate through all child nodes and process only those that are elements.
      for (int i = 0; i < children.getLength(); i++) {
        if (children.item(i) instanceof Element) {

          // Get the child element instance
          Element child = (Element) children.item(i);

          // Merge the child element instance
          SchemaInstance instance = mergeWithSchema(child);
          
          if (instance instanceof AttributeInstance) {
            entityIns.addValue((AttributeInstance) instance);
          } else {
            entityIns.addValue((EntityInstance) instance);
          }

        }
      }
      
      // Add the entity instance to the entity
      // Note: the duplicate entity instance will not
      // be added to the entity due to the use of set
      entity.addInstance(entityIns);
      
      // Return the attribute instance
      return entityIns;

    }

  }

  // For now, find one-to-one relation between entities.
  //
  // For a parent entity and its child entity (not child attribute) to have
  // one-to-one relation, they must satisfy two rules.
  //
  // (1) For each unique parent entity instance, there can be only one unique
  //     child entity instance.
  // (2) For each unique child entity instance, it must belong to only one
  //     unique parent entity instance. That is, if two unique parent entity
  //     instance both have the same unique child entity instance, they do
  //     not have the one-to-one relation.
  //
  private Map<String, Set<String>> flattenSchema() {
    
    // OTOMap = Map<"parent entity name", "a set of its child entity name whose relation is one-to-one">
    HashMap<String, Set<String>> OTOMap = new HashMap<String, Set<String>>();

    // Iterate through all entity element
    for (EntityDemo entity : entityMap.values()) {
      
      // A set of banned child entity names because they violate either of the two rules
      HashSet<String> bannedEntities = new HashSet<String>();
      
      // Get the map of entity instances of the current entity
      HashSet<EntityInstance> entityInsSet = entity.getInstances();

      // OTORels = <"child entity name", "a set of its unique instance ID's">
      //
      // This is to check if the same instance of the child entity has appeared
      // under different instances of the parent entity
      HashMap<String, HashSet<EntityInstance>> OTORels = new HashMap<String, HashSet<EntityInstance>>();

      // Iterate through all child entity instances
      for (EntityInstance entityIns : entityInsSet) {
        
        // Get the map of child entities
        HashMap<String, HashSet<EntityInstance>> childEntityInsMap = entityIns.getEntityMap();
        
        // Iterate through all child entity names
        for (String childEntity : childEntityInsMap.keySet()) {

            // Only process when the entity is not yet banned
            if (!bannedEntities.contains(childEntity)) {

              // Get the number of instances this child entity element has
              // occurred under this particular parent entity element
              HashSet<EntityInstance> childEntityInsSet = childEntityInsMap.get(childEntity);

              if (childEntityInsSet.size() > 1) {
                // More than one unique instance of the child entity
                // instances are found under the same parent entity.
                // Rule 1 is violated.

                // Add the child entity name to the banned list
                bannedEntities.add(childEntity);
                
                // Remove the child entity from the one-to-one relation map
                OTORels.remove(childEntity);
              } else {
                // Only one child entity instance is found
                // under the same parent entity element.

                // Get the set of child entity instances encountered so far
                HashSet<EntityInstance> OTOInsSet = OTORels.get(childEntity);

                if (OTOInsSet == null) {
                  // This is the first encounter.

                  // Update the one-to-one relation map
                  OTORels.put(childEntity, childEntityInsSet);
                } else {
                  // The child entity element has been encountered before

                  if (!OTOInsSet.addAll(childEntityInsSet)) {
                    // The same instance of the child entity element
                    // exists under another different instance of the
                    // parent entity element. Rule 2 is violated.

                    // Add the child entity name to the banned list
                    bannedEntities.add(childEntity);
                    
                    // Remove the child entity from the one-to-one relation map
                    OTORels.remove(childEntity);
                  }
                }
              }
            }

          }
        }
      
        // All those remained are child entity elements that
        // share one-to-one relation with the current parent
        // entity element.
        if (!OTORels.isEmpty()) {
          OTOMap.put(entity.getName(), OTORels.keySet());
        }
      }

      return OTOMap;

    }

}

















