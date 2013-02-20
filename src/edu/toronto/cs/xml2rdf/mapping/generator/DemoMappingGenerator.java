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

public class DemoMappingGenerator implements MappingGenerator {

  // attributeMap = Map<"attribute name", "Map of unique attribute instances">
  // "Map of unique attribute instances" = Map<ID, "unique attribute text value">
  //
  // A map of attributes, with the key being the name of the
  // attribute, and the value being a map of "unique" instances
  // of the attribute. The uniqueness of the instance is defined
  // by the uniqueness of its text value. The instance map has
  // unique ID as its key and the attribute text value as its value.
  private Map<String, Map<Integer, String>> attributeMap = new HashMap<String, Map<Integer, String>>();

  // attributeMapR = Map<"attribute name", "Map of unique attribute instances">
  // "Map of unique attribute instances" = Map<"unique attribute text value", ID>
  //
  // The same as above, except the key and the value of the instance map are switched.
  // This is done so to quickly look up the unique ID of an attribute text value.
  private Map<String, Map<String, Integer>> attributeMapR = new HashMap<String, Map<String, Integer>>();

  // "entity" here means non-attribute elements.
  // relationsMap = Map<"entity name", "Map of unique entity instances">
  // "Map of unique entity instances" = Map<ID, "Map of unique combination of entities and attributes">
  // "Map of unique combination of entities and attributes" = Map<"attribute/entity name", "A set of unique instance ID's">
  //
  // The uniqueness of an entity instance is defined by the unique combination of its child entity and attribute instances,
  // which is in turn defined by the uniqueness of each child entity or attribute instance.
  private Map<String, Map<Integer, Map<String, Set<Integer>>>> relationsMap = new HashMap<String, Map<Integer, Map<String, Set<Integer>>>>();

  // The same as above, except the key and the value of the entity map are switched.
  // This is done so to quickly look up the unique ID of an entity instance.
  private Map<String, Map<Map<String, Set<Integer>>, Integer>> relationsMapR = new HashMap<String, Map<Map<String, Set<Integer>>, Integer>>();

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

  // Generate a schematic/instance view of the document
  //
  // Example:
  //
  //	 XML Document:
  //	
  //	 <clinical_study>
  //	    <location>
  //	       <facility>
  //	          <name>Eric Yao</name>
  //	          <name>Jia Xian Yao</name>
  //	          <phone>123456</phone>
  //	       </facility>
  //	       <country>Canada</country>
  //	    </location>
  //	 </clinical_study>
  //	
  //	 <clinical_study>
  //	    <location>
  //	       <facility>
  //	          <name>Oktie Hassanzadeh</name>
  //	          <phone>654321</phone>
  //	       </facility>
  //	       <country>Canada</country>
  //	    </location>
  //		  <location>
  //	       <facility>
  //	          <name>Soheil Hassas Yeganeh</name>
  //	          <phone>123456</phone>
  //	       </facility>
  //	       <country>Canada</country>
  //	    </location>
  //	 </clinical_study>
  //
  // Generate Maps by Java
  //
  // attributeMap = {
  //                   [ "name", ( <1, "Eric Yao">, <2, "Jia Xian Yao">, <3, "Oktie Hassanzadeh">, <4, "Soheil Hassas Yeganeh"> ) ],
  //                   [ "phone", ( <1, "123456">, <2, "654321"> ) ],
  //                   [ "country", ( <1, "Canada"> ) ]
  //                }
  //
  // relationsMap = {
  //                   [ "facility",
  //                                 ( < 1, { ["A^name", (1, 2)], ["A^phone", (1)] } >,
  //                                   < 2, { ["A^name", (3) ], ["A^phone", (2)] } >,
  //                                   < 3, { ["A^name", (4) ], ["A^phone", (1)] } > 
  //                                 )
  //                   ],
  //                   [ "location",
  //                                 ( < 1, { ["R^facility", (1)], ["A^country", (1)] } >,
  //                                   < 2, { ["R^facility", (2)], ["A^country", (2)] } >,
  //                                   < 3, { ["R^facility", (3)], ["A^country", (1)] } > 
  //                                 )
  //                   ],
  //                   [ "clinical_study",
  //                                 ( < 1, { ["R^location", (1)] } >,
  //                                   < 2, { ["R^location", (2, 3)] } >
  //                                 )
  //                   ]
  //                }
  //
  // A couple of things to note here:
  //
  // (1) Notice only unique attribute text values are stored, see <country> or <phone> as an examples.
  // (2) An entity can have child combination of only entities <clinical_study>, only attributes <facility>, and both <location>.
  // (3) All instances of the child elements are stored in Set<Integer>, see how two instances of <location> are stored under
  //     <clinical_study>, or two instances of <name> are stored under <facility>.
  //
  private String mergeWithSchema(Element element) {

    if (XMLUtils.isLeaf(element)) {
      // Base case, the element is a leaf node and thus an attribute

      // Get the attribute name and attribute text value
      String name = element.getNodeName();
      String value = element.getTextContent();

      // The unique ID of the attribute text value
      int id;

      // The attribute instance map and its reverse map of the
      // current attribute
      Map<Integer, String> attributeValues = attributeMap.get(name);
      Map<String, Integer> attributeValuesR;

      if (attributeValues == null) {
        // It is the first encounter of an attribute with
        // its name, no instance map has been created yet

        // Create instance map and its reverse map
        attributeValues = new HashMap<Integer, String>();
        attributeValuesR = new HashMap<String, Integer>();

        // Unique ID always starts at 1
        id = 1;

        // Initialize instance/reverse map
        attributeValues.put(id, value);
        attributeValuesR.put(value, id);

        // Add attribute name (key) and the instance/reverse map (value)
        // to their corresponding map
        attributeMap.put(name, attributeValues);
        attributeMapR.put(name, attributeValuesR);
      } else {
        // The attribute of its name has already been
        // encountered with instance map retrieved.

        // Retrieve the reverse instance map to check
        // if the attribute text value has been added,
        // and to look up its unique ID
        attributeValuesR = attributeMapR.get(name);

        if (!attributeValuesR.containsKey(value)) {
          // The attribute text value is new and thus unique

          // Assign the unique ID for the unique text value
          id = attributeValuesR.size() + 1;

          // Add the unique ID and text values to
          // instance/reverse map
          attributeValues.put(id, value);
          attributeValuesR.put(value, id);
        } else {
          // The attribute text value already exists
          // in the instance/reverse map

          // Retrieve the unique ID
          id = attributeValuesR.get(value);
        }
      }

      // Return a string containing the attribute name and the unique instance ID
      String retStr = "A^" + name + ":" + id; // "A^" stands for "Attribute".
      return retStr;

    } else {
      // Recursive case, the element is not a leaf node

      // Get the children of the current element, possibly
      // a combination of attributes and entities
      NodeList children = element.getChildNodes();

      // childrenMap = Map<"attribute/entity name", "A set of its unique instance ID's">
      // A map of the combination of the child attribute and entity instances
      Map<String, Set<Integer>> childrenMap = new HashMap<String, Set<Integer>>();

      // Iterate through all child nodes and process only those that are elements.
      for (int i = 0; i < children.getLength(); i++) {
        if (children.item(i) instanceof Element) {

          // Get the child element instance
          Element child = (Element) children.item(i);

          // Merge the child element instance and get
          // the a returning string of the form "A^/R^name:ID",
          // with "A^/R^" indicating if the child element
          // is an attribute or entity instance, and "ID" being
          // the unique instance ID
          String childStr = mergeWithSchema(child);

          // Get the child element name and its unique instance ID
          int index = childStr.indexOf(":");
          String childName = childStr.substring(0, index);
          int childId = Integer.parseInt(childStr.substring(index+1));

          // Get the set containing the unique instance ID's
          // of the child element
          Set<Integer> childSet = childrenMap.get(childName);

          if (childSet == null) {
            // This is the first encounter of the child element
            // with its name. Create the empty set.
            childSet = new HashSet<Integer>();
          }

          // Add the unique instance ID to the set and update the map
          childSet.add(childId);
          childrenMap.put(childName, childSet);

        }
      }

      // Now that we have gone through all the child elements (attribute and entity
      // instances), childrenMap now documents the different child attribute and entity
      // elements that exists (schematic view), as well as all their unique instances
      // (instance view)

      // Get the name of the current entity element
      String name = element.getNodeName();

      // The unique ID of the current entity instance
      int id;

      // Get the entity instance map of the current entity element
      Map<Integer, Map<String, Set<Integer>>> currRelationMap = relationsMap.get(name);
      Map<Map<String, Set<Integer>>, Integer> currRelationMapR;

      if (currRelationMap == null) {
        // This is the first encounter of the entity with its name

        // Create the entity instance/reverse map
        currRelationMap = new HashMap<Integer, Map<String, Set<Integer>>>();
        currRelationMapR = new HashMap<Map<String, Set<Integer>>, Integer>();

        // The unique ID starts at 1
        id = 1;

        // Initialize entity instance/reverse map
        currRelationMap.put(id, childrenMap);
        currRelationMapR.put(childrenMap, id);

        // Put them in their corresponding map
        relationsMap.put(name, currRelationMap);
        relationsMapR.put(name, currRelationMapR);
      } else {
        // Entity with its name has already been added, meaning
        // there exists an entity instance map (perhaps waiting
        // to be updated)

        // Retrieve the reverse entity instance map to check
        // if the current entity instance already exists, and
        // to look up its unique ID
        currRelationMapR = relationsMapR.get(name);

        if (!currRelationMapR.containsKey(childrenMap)) {
          // The combination of attribute and entity instances
          // of the current entity instance has not been added

          // Create the new unique ID
          id = currRelationMapR.size() + 1;

          // Update the entity instance/reverse map
          currRelationMap.put(id, childrenMap);
          currRelationMapR.put(childrenMap, id);
        } else {
          // The combination of attribute and entity instances
          // of the current entity instance already exists, this
          // means that, there is another instance of this entity
          // that has EXACTLY THE SAME combination of attribute
          // and entity instances (not just schematic, but also
          // the SAME instances)

          // Retrieve the unique ID of the already-existed entity instance
          id = currRelationMapR.get(childrenMap);
        }
      }

      // Return a string containing the entity name and the unique instance ID
      String retStr = "R^" + name + ":" + id; // "R" stands for "Relation".
      return retStr;

    }

    // Phew, DONE!

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
    Map<String, Set<String>> OTOMap = new HashMap<String, Set<String>>();

    // Iterate through all entity element names (schematic view)
    for (String name : relationsMap.keySet()) {

      // Get the entity instance map
      Map<Integer, Map<String, Set<Integer>>> instances = relationsMap.get(name);

      // A list of banned child entity names because they violate either of the two rules
      List<String> bannedRels = new ArrayList<String>();

      // OTORels = <"child entity name", "a set of its unique instance ID's">
      //
      // This is to check if the same instance of the child entity has appeared
      // under different instances of the parent entity
      Map<String, Set<Integer>> OTORels = new HashMap<String, Set<Integer>>();

      // Iterate through all child entity instances
      for (Integer id : instances.keySet()) {
        Map<String, Set<Integer>> instance = instances.get(id);

        // Get the child entity name (schematic view) and only
        // process when it is not an attribute
        for (String relName : instance.keySet()) {
          if (relName.startsWith("R^")) {

            // Only process when the entity element is not yet banned
            if (!bannedRels.contains(relName)) {

              // Get the number of instances this child entity element has
              // occurred under this particular parent entity element
              Set<Integer> relIds = instance.get(relName);

              if (relIds.size() > 1) {
                // More than one unique instance of the child entity
                // element are found under the same parent entity element.
                // Rule 1 is violated.

                // Add the child entity name to the banned list
                bannedRels.add(relName);
                // Remove the child entity from the one-to-one relation map
                OTORels.remove(relName);
              } else {
                // Only one unique instance of the child entity element
                // is found under the same parent entity element.

                // Get the set of child entity instances encountered so far
                Set<Integer> OTOIds = OTORels.get(relName);

                if (OTOIds == null) {
                  // This is the first encounter.

                  // Update the one-to-one relation map
                  OTORels.put(relName, relIds);
                } else {
                  // The child entity element has been encountered before

                  if (!OTOIds.addAll(relIds)) {
                    // The same instance of the child entity element
                    // exists under another differnt instance of the
                    // parent entity element. Rule 2 is violated.

                    // Add the child entity name to the banned list
                    bannedRels.add(relName);
                    // Remove the child entity from the one-to-one relation map
                    OTORels.remove(relName);
                  }
                }
              }
            }

          }
        }
      }

      // All those remained are child entity elements that share one-to-one relation
      // with the current parent entity element.
      if (!OTORels.isEmpty()) {
        OTOMap.put(name, OTORels.keySet());
      }

    }

    return OTOMap;

  }

}

















