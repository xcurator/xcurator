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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import edu.toronto.cs.xml2rdf.opencyc.OpenCycOntology;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.utils.DependencyDAG;
import edu.toronto.cs.xml2rdf.utils.DisjointSet;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

/*
 * This is a "dummy" implementation of MappingGenerator interface.
 * TODO: Perhaps we can implement a brand new (not so dummy) class
 * that modularize each mapping step.
 */
/**
 * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
 */
public class DummyMappingGenerator implements MappingGenerator {

  // Flag for printing debugging information
  static boolean debug = true;

  // Ceilings
  private final int maxElement;
  private final int maxOnotlogyLookup;

  // Mapping essentials
  Map<String, Schema> schemas = new HashMap<String, Schema>();
  private final List<MappingStep> enabledSteps;

  // Metrics
  private final StringMetric stringMetric;
  private final SchemaSimilarityMetic schemaSimMetric;

  // All thresholds
  private final double ontologyMatchingThreshold;
  private final double schemaSimThreshold;
  private int leafPromotionThreshold = 5;
  private double matchThreshold = 0.75;
  private double ignoredNumbers = 0.25;
  private int minimumNumberOfAttributeToMerges = 2;
  private final double intralinkingThreshold;

  /*
   * Constructor that initialize all threshold parameters.
   * TODO: Design algorithms to estimate the thresholds so that they do not
   * need to be manually assigned.
   */
  public DummyMappingGenerator(double ontologyMatchingThreshold,
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

  /*
   * The root/mother function that calls all mapping step functions.
   * TODO: Modularize this function.
   */
  @Override
  public Document generateMapping(Element rootDoc, String typePrefix) {

    // Capture time at each step
    long start;
    long end;

    // The organization of the XML files should have "clinical_studies" as the
    // very root document element (which is passed in as rootDoc), with many
    // "clinical_study" child nodes, which is the children variable below.
    NodeList children = rootDoc.getChildNodes();
    System.out.println(children.getLength());

    // Step 1. Merge the child element nodes and their associated schemas

    start = System.currentTimeMillis();

    // Iterate through all child nodes or up to the maximum number specified,
    // and process (merge) ONLY child nodes that are elements.
    for (int i = 0; i < children.getLength() && (maxElement == -1 || i < maxElement); i++) {
      if (children.item(i) instanceof Element) {
        // Get the child element node.
        Element child = (Element) children.item(i);
        String name = child.getNodeName();
        // Create a schema for this child element node if one with the same node name does not exist.
        // Consequently, there will be only one schema for each unique node name.
        // The path of the schema is the ABSOLUTE path to the child element node, starting with "/"
        // and the root element node name, such as "/clinical_studies/clinical_study".
        Schema schema = schemas.get(name);

        if (schema == null) {
          // Eric: What if child nodes have the same name but at different layers of the
          // XML file and thus different path? Only the first path is used?
          schema = new Schema(null, child, "/" + rootDoc.getNodeName() + "/" + name);
          schemas.put(name, schema);
        }

        // Merge the child element node with its schema, that is, the schema of the same name
        try {
          mergeWithSchema(child, schema);
        } catch (Exception e) {
          if (debug) e.printStackTrace();
        }
      }
    }

    end = System.currentTimeMillis();
    System.out.println("Execution time of step 1 : schema merge was " + (end-start) + " ms.");

    cacheInstances(schemas, rootDoc);

    // Step 2. Flatten the schema

    start = System.currentTimeMillis();

    try {
      // Eric: the flat threshold is always 1?
      flattenSchema(rootDoc.getOwnerDocument(), 1);
    } catch (XPathExpressionException e1) {
      if (debug) e1.printStackTrace();
    }

    end = System.currentTimeMillis();
    System.out.println("Execution time of step 2 : schema flatten was " + (end-start) + " ms.");

    // Step 3. Remove duplicates

    start = System.currentTimeMillis();

    removeDuplicates();

    end = System.currentTimeMillis();
    System.out.println("Execution time of step 3 : duplicate remove was " + (end-start) + " ms.");

    // Step 4. Find a possible key for each identified schema

    start = System.currentTimeMillis();

    try {
      for (Schema schema: schemas.values()) {
        try {
          // Eric: uniqunessThreshold is always 0.0d?
          // TODO: find a way to automate this.
          findKeysForSchema(schema, rootDoc.getOwnerDocument(), 0.0d);
        } catch (XPathExpressionException e) {
          if (debug) e.printStackTrace();
        }
      }
    } catch (java.util.ConcurrentModificationException e1) {
      if (debug) e1.printStackTrace();
    }

    end = System.currentTimeMillis();
    System.out.println("Execution time of step 4 : key identification was " + (end-start) + " ms.");

    // Step 5. Intra-link schemas

    start = System.currentTimeMillis();

    try {
      intralinkSchemas(rootDoc.getOwnerDocument(), intralinkingThreshold);
    } catch (XPathExpressionException e1) {
      if (debug) e1.printStackTrace();
    }

    end = System.currentTimeMillis();
    System.out.println("Execution time of step 5 : intra-linking was " + (end-start) + " ms.");

    DependencyDAG<Schema> dependecyDAG = new DependencyDAG<Schema>();

    for (Schema schema : schemas.values()) {
      dependecyDAG.addNode(schema);
      // TODO(oktie): Haaji this is duplicate of the code on line 904.
      for (Relation rel : schema.getRelations()) {
        if (!schemas.containsKey(rel.getSchema())) {
          LogUtils.error(DummyMappingGenerator.class,
              "ERRRRRRRRRRRRR! " + rel.getSchema() + " Does not exist. " + rel);
        }
      }
    }

    for (Schema schema : schemas.values()) {
      for (Relation rel : schema.getRelations()) {
        dependecyDAG.addDependency(schema, rel.getSchema());
      }
    }

    Document mappingRoot = null;
    try {
      mappingRoot = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder().newDocument();

      Element rootElement = mappingRoot.createElementNS(
          "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "mapping");
      mappingRoot.appendChild(rootElement);

      while (dependecyDAG.size() != 0) {
        Schema schema = dependecyDAG.removeElementWithNoDependency();
        addEntities(schema, mappingRoot, "", typePrefix);
      }

    } catch (ParserConfigurationException e) {
      if (debug)
        e.printStackTrace();
    }

    return mappingRoot;
  }

  private void cacheInstances(Map<String, Schema> schemas, Element rootDoc) {
    for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
      String name = entry.getKey();
      entry.getValue();
    }
  }

  /*
   * Checking if the mapping step is toggled by the user.
   */
  public boolean isStepEnabled(MappingStep step) {
    return enabledSteps.contains(step);
  }

  SchemaInstance createSchemaInstance(Element element, Schema schema) {
    SchemaInstance instance = null;
    try {
      instance = new SchemaInstance(element);
      schema.instances.add(instance);
    } catch (IOException e) {}
    return instance;
  }

  /*
   * Step 1. Merge the schemas
   */
  private SchemaInstance mergeWithSchema(Element element, Schema schema)
      throws SchemaException, XPathExpressionException {
    // Cache the instance.
    SchemaInstance instance = createSchemaInstance(element, schema);

    // Set the schema name, if null, to the name of the element;
    // or check if the two names are the same, as they should be
    // Eric: I believe this is unnecessary and should be removed
    String schemaName = schema.getName();
    if (schemaName == null) {
      schema.setName(element.getNodeName());
    } else {
      if (!schema.getName().equals(element.getNodeName())) {
        throw new SchemaException("Schema element names do not match.");
      }
    }

    // Never merge leaf element nodes.
    //
    // Eric: Technically, this "if statement" will always be true because
    // if the element if a leaf, then "mergeWithSchema" function will never
    // be called on this leaf element in the first place
    if (!XMLUtils.isLeaf(element)) {

      // Get all the (immediate next level) child nodes of the given element node
      NodeList children = element.getChildNodes();

      // Iterate through all child nodes, but process
      // ONLY those that are elements
      for (int i = 0; i < children.getLength(); i++) {
        if (children.item(i) instanceof Element) {

          // Process child element node that is NOT a leaf element node.
          if (!XMLUtils.isLeaf(children.item(i))) {

            // Get the non-leaf child element node, which means it has
            // leaf (and possibly non-leaf) child element nodes under it
            Element child = (Element) children.item(i);

            // The boolean value to indicate if a previous instance of this
            // non-leaf child element with the same name has already been
            // processed/merged.
            boolean found = false;

            // Find out if this non-leaf child element already exists
            // in parent element's relations, meaning that a previous
            // instance of the non-leaf child element with the same name
            // has already been processed and put into the relations of
            // the parent element.
            //
            // If so, merge this instance of the non-leaf child element
            // with the already consolidated associated schema, during
            // which new relations or attributes might be added to this
            // schema
            for (Relation childRelation : schema.getRelations()) {
              if (childRelation.getName().equals(child.getNodeName())) {
                SchemaInstance childInstance =
                    mergeWithSchema(child, childRelation.getSchema());
                createRelationInstnace(childRelation, instance, childInstance);
                found = true;
                break;
              }
            }

            // This is the first encounter of the non-leaf child element
            // with this node name
            if (!found) {

              // Get the name of the non-leaf child element node
              String name = child.getNodeName();
              // Create the path, which is the ABSOLUTE path to this
              // non-leaf child element node, starting with "/"
              String path = schema.getPath() + "/" + name;

              // Create a schema for this non-leaf child element node,
              // if none exists yet
              Schema childSchema = schemas.get(name);
              if (childSchema == null) {
                // Eric: Why not set the parent to the current schema?
                childSchema = new Schema(null, child, path);
                schemas.put(child.getNodeName(), childSchema);
              }

              // Merge this non-leaf child element node first before
              // further processing this node
              SchemaInstance childInstance =
                  mergeWithSchema(child, childSchema);

              // Create the lookupKeys for the creation of relation later
              // This is essentially a list of all leaf elements that
              // exist under the current child node
              Set<Attribute> lookupKeys = new HashSet<Attribute>();

              // Get the list of RELATIVE path to all leaf element nodes
              // of the current non-leaf child element node, with path
              // starting with the name of the current non-leaf child
              // element node (and not "/"), and ending with the name
              // of the leaf element nodes
              List<String> leaves = XMLUtils.getAllLeaves(child);

              // Iterate through all paths to the leaf element nodes
              for (String leafPath: leaves) {

                // Get the name of the current LEAF element node
                int lastNodeIndex = leafPath.lastIndexOf('/');
                String lastNodeName = leafPath.substring(lastNodeIndex + 1);

                // Create leafName by simply replacing all "/" with "."
                String leafName = leafPath.replace('/', '.');

                // Append ".name" to the end of leafName if the current
                // leaf element node has been promoted and has an
                // OntologyLink schema associated with it
                //
                // Eric: Is it correct to say that the ONLY case where
                // lastNodeSchema is NOT null is when the child node has
                // been promoted, which means lastNodeSchema is ALWAYS
                // an OntologyLink schema?
                Schema lastNodeSchema = schemas.get(lastNodeName);
                if (lastNodeSchema instanceof OntologyLink) {
                  // Eric: Why ".name"? What's the meaning behind this?
                  leafName += ".name";
                }

                // Create leafPath through removing the name of the parent non-leaf
                // element node at the beginning, along with the "/", and then append
                // "/text()" at the end of the leafPath.
                //
                // This is essentially the RELATIVE path to the TEXT VALUE of the
                // current leaf element node under the parent non-leaf element node,
                // and this path will be understood correctly by XPath
                leafPath = leafPath.replaceAll("^" + child.getNodeName() + "/?", "");
                // Eric: Why would leafPath ever be empty anyways? It must at least
                // contain the name of the LAEF node.
                leafPath = leafPath.length() > 0 ? leafPath + "/text()" : "text()";

                // Create an entry to the lookupKeys, which keeps track of the parent
                // non-leaf element node's schema, the name and the RELATIVE path to
                // all the TEXT VALUES of the leaf element nodes under it, and whether
                // these element nodes are keys or not
                //
                // Eric: I'm still unclear about the answer to the email question
                // regarding the lookupKeys (Question 1.2).
                lookupKeys.add(new Attribute(schema, leafName, leafPath, false));
              }

              // Eric: Why is path (the third parameter) set to name?
              // Set the parent-child (schema-childSchema) relation, with lookupKeys essentially
              // a list of LEAF nodes of the child (childSchema) and their parent is set to
              // schema
              // One can think of the path to the childSchema as schema.getPath() + "/" + name
              // (name is the name of the childSchema)
              Relation relation = new Relation(schema, name, name, childSchema, lookupKeys);
              schema.addRelation(relation);
              createRelationInstnace(relation, instance, childInstance);
            }
          }

          // Process child element node that IS INDEED a leaf element node
          else {

            // Get the leaf child element and its name
            Element child = (Element) children.item(i);
            String name = child.getNodeName();

            // Get the ABSOLUTE path to the leaf child element,
            // starting with "/"
            String path = schema.getPath() + "/" + name;

            // Find out if a previous instance of the leaf child element
            // with the same name has already been added to the attributes
            // or relations. Since the leaf child element has no children,
            // the previous instance will be exactly the same as the current
            // instance (structure-wise), the current instance does not need
            // to be processed anymore.
            boolean found = false;

            for (Attribute childAttribute : schema.getAttributes()) {
              if (childAttribute.getName().equals(child.getNodeName())) {
                found = true;
                break;
              }
            }

            for (Relation childRelation : schema.getRelations()) {
              if (childRelation.getSchema() instanceof OntologyLink
                  && childRelation.getName().equals(child.getNodeName())) {
                found = true;
                break;
              }
            }

            // If no previous instance has found, which means this is the first
            // encounter of the leaf child node with this name
            if (!found) {

              LogUtils.debug(this.getClass(), "searching in ontology for " + path);

              // values contains all the text values of the elements with the same ABSOLUTE path
              Set<String> values = new HashSet<String>();
              // types contains all typeIDs (above threshold) based on the above text values
              Set<String> types = findTypeInOntology(path, element.getOwnerDocument(),
                  values, matchThreshold, ignoredNumbers);

              // If types contains some typeIDs and values contains enough text values
              // Eric: What's the significance of values.size() >= leafPromotionThreshold since
              // values are merely the different text values of the current leaf node?
              if (types != null && types.size() > 0 && values.size() >= leafPromotionThreshold) {

                LogUtils.debug(this.getClass(), "Types found for " + element + " " + types);

                // Find out if a previous instance of the leaf child
                // element with the same name has already been processed
                found = false;

                // If a previous instance of the leaf child element has already been
                // processed and added to parent's relation, merge the current instance
                // of the leaf child node
                //
                // Eric: This leaf child node will NEVER get merged because mergeWithSchema
                // function only process non-leaf elements. Is this correct?
                for (Relation childRelation : schema.getRelations()) {
                  if (childRelation.getName().equals(child.getNodeName())) {
                    mergeWithSchema(child, childRelation.getSchema());
                    found = true;
                    break;
                  }
                }

                // If no previous instance of the leaf child element is found
                // and this is the first encounter of the leaf child element with
                // this node name
                if (!found) {

                  // Create a schema for the current leaf child element,
                  // if none exists yet
                  OntologyLink childSchema = (OntologyLink) schemas.get(child.getNodeName());
                  if (childSchema == null) {
                    childSchema = new OntologyLink(null, child, path, types);
                    schemas.put(child.getNodeName(), childSchema);
                  }

                  // Merge the current leaf child element before further processing
                  //
                  // Eric: Again, this leaf child element will NEVER get merged
                  // because mergeWithSchema function only process non-leaf elements.
                  // Is this correct?
                  SchemaInstance childInstance =
                      mergeWithSchema(child, childSchema);

                  // Eric: Because the current child element is a leaf, it does NOT contain
                  // other child elements, which means the list leaves contains ONLY ONE
                  // string, which is the name of the current child element, and consequently
                  // the set lookupKeys contains ONLY ONE attribute, with its path being "text()".
                  // Is this the correct understanding? Once again, I'm not sure why lookupKeys
                  // are needed.
                  //
                  // Eric: The following relation creation process is exactly the same as before,
                  // which I believe should and must be simplified for the reasoning above.
                  Set<Attribute> lookupKeys = new HashSet<Attribute>();

                  // Eric: Would this just return child itself as it is the leaf element?
                  List<String> leaves = XMLUtils.getAllLeaves(child);

                  for (String leafPath: leaves) {
                    int lastNodeIndex = leafPath.lastIndexOf('/');
                    String lastNodeName = leafPath.substring(lastNodeIndex + 1);
                    // Eric: Here, the lastNodeSchema is actually just the schema
                    // of the CURRENT child, which is the OntologyLink just created
                    // above. Is this the intention?
                    // FIXME: The following code can be simplified for the reasoning above.
                    Schema lastNodeSchema = schemas.get(lastNodeName);

                    String leafName = leafPath.replace('/', '.');

                    if (lastNodeSchema instanceof OntologyLink) {
                      leafName += ".name";
                    }

                    leafPath = leafPath.replaceAll
                        ("^" + child.getNodeName() + "/?", "");
                    leafPath = leafPath.length() > 0 ?
                        leafPath + "/text()" : "text()";
                    
                    lookupKeys.add(new Attribute(schema, leafName, leafPath,
                        false));
                  }

                  Relation relation = new Relation(schema, name, name, childSchema, lookupKeys);
                  createRelationInstnace(relation, instance, childInstance);
                }
              }

              // If the current leaf child node is not promoted, make it an attribute
              else {

                // The attribute is created with path being the ABSOLUTE path
                // to the TEXT VALUE of the leaf child node
                //
                // Eric: Why use "setPath" when name and path can be set when
                // the attribute is initialized

                Attribute attribute = new Attribute(schema, name, path, false);
                attribute.setName(child.getNodeName());
                attribute.setPath(child.getNodeName() + "/text()");

                schema.addAttribute(attribute);
                createAttributeInstance(attribute, instance, child);

                // ?????
                if (types != null && types.size() != 0) {
                  LogUtils.debug(this.getClass(),
                      "Types found for " + element + " " + types);
                  attribute.setTypeURIs(types);
                }
              }
            }
          }
        }
      }
    }
    return instance;
  }

  private AttributeInstance createAttributeInstance(Attribute attribute,
      SchemaInstance schemaInstance, Element attributeElement) {
    AttributeInstance instance = null;
    try {
      instance = new AttributeInstance(schemaInstance, attributeElement);
      attribute.addInstance(instance);
    } catch (IOException e) {}
    return instance;
  }

  private RelationInstance createRelationInstnace(Relation relation,
      SchemaInstance from, SchemaInstance to) {
    RelationInstance instance = new RelationInstance(from, to);
    relation.addInstance(instance);
    return instance;
  }

  /*
   * Step 1. Merge the schemas - Helper Function
   */
  private Set<String> findTypeInOntology(String path, Document doc,
      Set<String> visitedTerms, double matchThreshold, double ignoredNumebers)
          throws XPathExpressionException {

    // Perform ontology finding only if "INTRALINKING" is enabled
    if (!isStepEnabled(MappingStep.INTRALINKING)) {
      return new HashSet<String>();
    }

    // OpenCycOntology currently is NOT in use
    OpenCycOntology ontology = OpenCycOntology.getInstance();

    // Instantiate FreeBaseLinker
    FreeBaseLinker freebase = new FreeBaseLinker();

    // Get all instances of the nodes with the same ABSOLUTE path.
    // This means all the nodes have the same node name and they must
    // be all leaf element nodes since findTypeInOntology only calls
    // on leaf element nodes
    NodeList nl = XMLUtils.getNodesByPath(path, null, doc);

    // For each String typeIDs, count how many Integer times they are
    // returned from freebase
    Map<String, Integer> commonTypes = new HashMap<String, Integer>();

    // Count how many times no typeIDs is returned for a text value
    int count = 0;

    // Iterate through all instances of nodes with the same ABSOLUTE path
    for (int i = 0; i < nl.getLength() &&
        (maxOnotlogyLookup == -1 || i < maxOnotlogyLookup); i++) {

      // Break all iterations if there are too many times (count) where no typeIDs is returned,
      // or enough different typeIDs have already returned.
      if (count > 100 && commonTypes.size() < 100) {
        break;
      }

      // Skip the current iteration if the text value of the
      // current instance has already been processed
      // Eric: This "term" is incorrect for the Patent data
      String term = nl.item(i).getTextContent();

      // Eric: The above "term" is wrong because it includes in-tag attributes,
      // and the "term" ends up being "Candy holderd2e53", which should've been "Candy holder".
      // The following line of code fixes the problem.
      // String term = nl.item(i).getChildNodes().item(0).getNodeValue();

      if (visitedTerms.contains(term)) {
        continue;
      }

      // If not, the text value of the current instance is
      // added to the visistedTerms, and these added terms
      // are not processed, such as having digits removed, etc
      visitedTerms.add(term);

      // Skip the current iteration if the text value of the current instance is empty,
      // longer than 50 characters, or consists entirely of digits
      if (term.trim().length() == 0 || term.length() > 50 || term.matches("^\\d+$") ) {
        continue;
      }

      // Remove all digits from the text value of the current instance
      String withoutNumbers = term.trim().replaceAll("\\d", "");

      // Skip the current iteration (once again) if the ratio of the length of digits over
      // the total length of the text value is too high
      if ((term.length() - withoutNumbers.length()) / (double) term.length() >=
          ignoredNumebers) {
        continue;
      }

      // A set that holds all freebase typeIDs that look something
      // like "http://rdf.freebase.com/rdf/music.release"
      Set<String> types = new HashSet<String>(); //ontology.findTypesForResource(term, stringMetric, ontologyMatchingThreshold);

      // Get the list of typeIDs based on the text value, and the typeIDs look like the following:
      // "http://rdf.freebase.com/rdf/music.release"
      Set<String> freebaseTypes = freebase.findTypesForResource(term,
          stringMetric, ontologyMatchingThreshold);

      // Add all typeIDs if freebaseTypes is not null
      if (freebaseTypes != null) {
        types.addAll(freebaseTypes);
      }

      // If no typeIds is added and the length of the term is less than 20,
      // which could mean that there might be spelling mistakes
      if (types.size() == 0 && term.length() < 20) {

        // Get the Google spell checker and get the spell response
        SpellChecker checker = new SpellChecker();
        SpellResponse spellResponse = checker.check(term);

        // If there are spell corrections
        if (spellResponse.getCorrections() != null &&
            spellResponse.getCorrections().length > 0) {

          // Get the spell checked text value
          //
          // Eric: It seems like only one word is returned for text values of any length,
          // so for example, "Daniel Aradi MD PhD" is spell checked as "Abadi", which is
          // obviously wrong
          term = "";
          for (int j = 0; j < spellResponse.getCorrections().length; j++) {
            term += spellResponse.getCorrections()[j].getValue().split("\t")[0];
          }

          // Try add typeIDs based on the new spell-checked text value
          if (term.length() > 0) {
            types = new HashSet<String>(); //.findTypesForResource(term, stringMetric, ontologyMatchingThreshold);
            freebaseTypes = freebase.findTypesForResource(term, stringMetric,
                ontologyMatchingThreshold);
            if (freebaseTypes != null) {
              types.addAll(freebaseTypes);
            }
          }
        }
      }

      // Skip the current iteration if still no typeIDs is found
      if (types.size() == 0) {
        count++;
        continue;
      }

      // Count for each typeID, the number of times it has occurred
      for (String type: types) {
        Integer typeCount = commonTypes.get(type);
        if (typeCount == null) {
          typeCount = 0;
        }
        typeCount++;
        commonTypes.put(type, typeCount);
      }

      //      if (commonTypes == null) {
      //        commonTypes = types;
      //      } else {
      //        Set<String> tempCommonTypes = SetUtils.intersection(commonTypes, types);
      //        if (tempCommonTypes.size() == 0) {
      //          count++;
      //        } else {
      //          commonTypes = tempCommonTypes;
      //        }
      //      }

    }

    // double ratio = (visitedTerms.size() - count) / visitedTerms.size();

    // Add typeIDs to types this typeID has occurred enough times (over the threshold)
    Set<String> types = new HashSet<String>();
    for (Map.Entry<String, Integer> entry: commonTypes.entrySet()) {
      // System.out.println("Score for " + entry.getKey() + " is " + entry.getValue() / (double) visitedTerms.size());
      if (entry.getValue() / (double) visitedTerms.size() >= matchThreshold) {
        types.add(entry.getKey());
      }
    }

    // System.err.println("returning " + types + " for " + path);
    return types;

  }

  /*
   * Step 2. Flatten the schemas
   */
  private void flattenSchema(Document doc, double flatThreshold)
      throws XPathExpressionException {

    // Only perform schema flattening if enabled
    if (!isStepEnabled(MappingStep.SCHEMA_FLATTENING)) {
      return;
    }

    DependencyDAG<Schema> dependecyDAG = new DependencyDAG<Schema>();

    for (Schema schema : schemas.values()) {
      dependecyDAG.addNode(schema);
    }

    for (Schema schema : schemas.values()) {
      for (Relation rel : schema.getRelations()) {
        dependecyDAG.addDependency(schema, rel.getSchema());
      }
    }

    while (dependecyDAG.size() != 0) {
      Schema schema = dependecyDAG.removeElementWithNoDependency();
      // Eric: The line below serves no purpose!!!
      schema.getAttributes();

      Set<Relation> oneToOneRelations = findOneToOneRelations(doc, schema);

      for (Relation rel: oneToOneRelations) {
        LogUtils.debug(getClass(), "is one to one : " + schema + " . " + rel);
        flattenRelation(schema, rel);
      }
    }

  }

  /*
   * Step 2. Flatten the schemas - Helper Function
   */
  private Set<Relation> findOneToOneRelations(Document doc, Schema schema)
      throws XPathExpressionException {
    // If there is no relation in the schema, just return an empty set.
    if (schema.getRelations().size() == 0) {
      return new HashSet<Relation>();
    }

    Set<Relation> oneToOneRelations = new HashSet<Relation>();

    for (Relation rel : schema.getRelations()) {
      if (rel.isOneToOne()) {
        oneToOneRelations.add(rel);
      }
    }

    return oneToOneRelations;
  }

  /*
   * Step 2. Flatten the schemas - Helper Function
   */
  private void flattenRelation(Schema schema, Relation rel) {

    Schema targetSchema = rel.getSchema();

    // The promoted (relational) leaf node is now demoted
    // back to an attribute because of one-to-one'ness
    if (targetSchema instanceof OntologyLink) {
      // Eric: Serious?!
      String name = "name";
      name = targetSchema.getName();

      String path = rel.getPath() + "/text()";

      Attribute attr = new Attribute(schema, name, path, false);
      attr.setTypeURIs(targetSchema.getTypeURIs());

      schema.addAttribute(attr);
      attr.setParent(schema);
    }

    // Add attributes of the relation to the schema,
    // with modification to attributes' name, path, and
    // parent schema
    for (Attribute attr: targetSchema.getAttributes()) {
      String name = attr.getName();
      name = targetSchema.getName() + "_" + name;
      attr.setName(name);

      String path = attr.getPath();
      path = rel.getPath() + "/" + path;
      attr.setPath(path);

      schema.addAttribute(attr);
      attr.setParent(schema);
    }

    // Add relations of the relation to the schema,
    // with modification to relations' name, path, and
    // lookupKey attributes
    for (Relation targetRel: targetSchema.getRelations()) {
      String path = rel.getPath() + "/" + targetRel.getName();
      targetRel.setPath(path);

      String name = targetRel.getName();
      name = targetSchema.getName() + "_" + name;
      targetRel.setName(name);

      schema.addRelation(targetRel);
      targetRel.setParent(schema);

      // Eric: Shouldn't we also update the parent schema to the new one?
      for (Attribute lookupKey: targetRel.getLookupKeys()) {
        lookupKey.setPath(rel.getPath() + "/" + lookupKey.getPath());
        lookupKey.setName(lookupKey.getName().replace(rel.getName() + ".",
            rel.getName() + "_"));
      }
    }

    // Now that we port over all relations and attributes of the relation
    // to its one-to-one parent schema, remove this relation and complete
    // the flatten process
    schema.getRelations().remove(rel);

    // Remove the relation schema altogether, iff this schema is not a
    // relation of any other schemas
    maybeRemoveSchema(targetSchema);
  }

  /*
   * Step 3. Remove duplicates
   */
  private void removeDuplicates() {

    if (!isStepEnabled(MappingStep.DUPLICATE_REMOVAL)) {
      return;
    }

    // The value of dSets is a hierarchy set of schemas that are considered to be
    // similar or duplicates
    Map<Schema, DisjointSet<Schema>> dSets = new HashMap<Schema, DisjointSet<Schema>>();

    for (Schema schema : schemas.values()) {
      DisjointSet<Schema> set = new DisjointSet<Schema>(schema);
      dSets.put(schema, set);
    }

    // TODO: Better way to detect duplicate pairs? For example, instead of iterating all
    // possible schema pairs, only compare those that are at the same level because its
    // unlikely the very top schema will be similar to that of almost leaf schemas
    for (Schema schema1 : schemas.values()) {
      for (Schema schema2 : schemas.values()) {
        // Skip the current schema pair if they are the same, if they do have enough
        // attributes, or if schema1 name > schema2 name to avoid inspecting
        // <schema1, schema2> and <schema2, schema1>
        if (schema1 == schema2
            || schema1.getAttributes().size() < minimumNumberOfAttributeToMerges
            || schema2.getAttributes().size() < minimumNumberOfAttributeToMerges
            || schema1.getName().compareTo(schema2.getName()) > 0) {
          continue;
        }

        // TODO: better similarity schema
        double similarity = schemaSimMetric.getSimiliarity(schema1, schema2);

        if (similarity >= schemaSimThreshold) {
          dSets.get(schema1).union(dSets.get(schema2));
          LogUtils.info(this.getClass(), "Merging " + schema1 + " with " + schema2);
        }
      }
    }

    while (dSets.size() > 0) {

      Set<Schema> listOfSchemas = new HashSet<Schema>();

      Schema schema = dSets.keySet().iterator().next();
      listOfSchemas.add(schema);
      DisjointSet<Schema> dset = dSets.remove(schema);
      DisjointSet<Schema> root = dset.find();

      for (DisjointSet<Schema> set : root.getChildren()) {
        Schema similarSchema = set.getData();
        if (!schema.equals(similarSchema)) {
          listOfSchemas.add(similarSchema);
          dSets.remove(similarSchema);
        }
      }

      if (listOfSchemas.size() > 1) {
        Schema newSchema = mergeSchemas(listOfSchemas);

        // Replace old relation schema with the merged one
        for (Schema oldSchema : schemas.values()) {
          for (Relation rel : oldSchema.getRelations()) {
            if (listOfSchemas.contains(rel.getSchema())) {
              rel.setSchema(newSchema);
            }
          }
        }

        // Remove all the pre-merged schemas
        for (Schema s: listOfSchemas) {
          for (Attribute attr : s.getAttributes()) {
            attr.setParent(newSchema);
          }
          schemas.remove(s.getName());
        }

        // Place the new merged schema
        schemas.put(newSchema.getName(), newSchema);
      }
    }
  }

  /*
   * Step 3. Remove duplicates - Helper Function
   */
  private Schema mergeSchemas(Set<Schema> listOfSchemas) {
    String path = "";
    String name = "";

    Set<Attribute> attributes = new HashSet<Attribute>();
    Set<Relation> relations = new HashSet<Relation>();

    for (Schema s: listOfSchemas) {
      attributes.addAll(s.getAttributes());
      relations.addAll(s.getRelations());
      path += s.getPath() + "|";
      name += s.getName() + "_or_";
    }

    path = path.substring(0, path.length() - 1);
    name = name.substring(0, name.length() - 4);

    Schema schema = new Schema(null, name, path);
    schema.setAttributes(attributes);
    schema.setRelations(relations);
    return schema;
  }

  /*
   * Step 4. Find a possible key for each identified schema
   */
  private void findKeysForSchema(Schema schema, Document doc,
      double uniqunessThreshold) throws XPathExpressionException {

    // A set of maps, with each map corresponds to one instance of
    // the input schema. The keys of the map are the names of all
    // relations and attributes of the input schema, and the values
    // of the map are the text values of relations or attributes.
    Set<Map<String, String>> entities = new HashSet<Map<String, String>>();

    if (schema instanceof OntologyLink) {
      return;
    }

    // A set of schema names (either relation schemas or attribute
    // schemas of the current schema) that are NOT keys
    Set<String> bannedKeys = new HashSet<String>();

    // Get all instances of the input schema, such as all
    // instances of "/clinical_studies/clinical_study"
    NodeList entityNL = XMLUtils.getNodesByPath(schema.getPath(), null, doc);

    // Iterate through all instances of the input schema, inspect its
    // attributes and relations, find those that cannot be keys (that is,
    // one schema instance has more than one attribute/relation schemas
    // with the same name), and fill variable entities defined above
    // TODO: This loop can be made faster!!! Think!!!
    for (int i = 0; i < entityNL.getLength(); i++) {

      // The keys of the map are the names of all relations and
      // attributes of the current instance of the input schema, and
      // the values of the map are the text values of these relations
      // or attributes.
      HashMap<String, String> instance = new HashMap<String, String>();

      // Get the current instance of the input schema
      Element element = (Element) entityNL.item(i);

      // For each attribute, find all its instances under the current
      // instance of the input schema, identify those that cannot be
      // keys, and fill the variable instance defined above
      for (Attribute attr: schema.getAttributes()) {

        // Skip if the current attribute is already banned
        if (bannedKeys.contains(attr.getName())) {
          continue;
        }

        // Identify if the current attribute should be banned
        NodeList attributeNL = XMLUtils.getNodesByPath(attr.getPath(), element, doc);
        if (attributeNL.getLength() != 1) {
          bannedKeys.add(attr.getName());
          attr.setKey(false);
        }

        // Fill the variable instance defined above with the text value of the attribute
        // Eric: Only adds one text value when more than one instances can occur. WRONG? (Map
        // can only perserve one value per key, that's why).
        // Eric: Should the banned instances be added as well?  I don't think they should.
        instance.put(attr.getName(), XMLUtils.getStringByPath(attr.getPath(), element, doc));
      }

      // For each relation, find all its instances under the current
      // instance of the input schema, identify those that cannot be
      // keys, and fill the variable instance defined above
      for (Relation rel: schema.getRelations()) {

        // Skip if the current relation is already banned
        if (bannedKeys.contains(rel.getName())) {
          continue;
        }

        // Identify if the current attribute should be banned
        // NodeList relNL = XMLUtils.getNodesByPath(rel.getPath() + "/text()", element, doc);
        NodeList relNL = XMLUtils.getNodesByPath(rel.getPath(), element, doc);
        if (relNL.getLength() != 1) {
          bannedKeys.add(rel.getName());
          continue;
        }

        // Fill the variable instance defined above with the text value of the relation,
        // which the text values of all its leaf children
        // Eric: Only adds one text value when more than one instances can occur. WRONG? (Map
        // can only perserve one value per key, that's why).
        // Eric: Should the banned instances be added as well? I don't think they should.
        instance.put(rel.getName(), XMLUtils.getStringByPath(rel.getPath(), element, doc));

      }

      entities.add(instance);
    }

    // Find possible keys among all attributes of the input schema
    for (Attribute attr: schema.getAttributes()) {

      if (bannedKeys.contains(attr.getName())) {
        continue;
      }

      // For the current attribute, count for each of its text
      // value, how many such text value has occurred across
      // all maps (entities)
      Map<String, Integer> valueMap = new HashMap<String, Integer>();
      for (Map<String, String> instance: entities) {
        String value = instance.get(attr.getName());
        Integer count = valueMap.get(value);
        if (count == null) {
          count = 1;
        } else {
          count++;
        }
        valueMap.put(value, count);
      }

      // For the current attribute, count the number of text values
      // that have occurred more than once, in another word, for the
      // current attribute, count the number of its text values that
      // have occurred more than once across all instances of the input
      // schema
      //
      // Eric: Is this the right schema? Let's say there's 200 instances
      // of the schema, and the attribute has 100 unique values. 1 particular
      // attribute value has occurred in 101 instances of the schema, but the
      // other 99 attribute values occurred only once. This attribute will
      // be considered as a key, but is this correct?
      int nonUnique = 0;
      for (Map.Entry<String, Integer> entry: valueMap.entrySet()) {
        Integer count = entry.getValue();
        if (count != 1) {
          nonUnique++;
        }
      }

      // Consider the attribute as a key if the attribute's text value
      // is unique "enough" (passing the threshold)
      int total = valueMap.size();
      if (nonUnique / (double) total <= uniqunessThreshold) {
        attr.setKey(true);
        LogUtils.debug(this.getClass(), schema.getName() + "." + attr.getName() + " is unique");
      }
    }

    Set<Relation> depromotedRels = new HashSet<Relation>();

    for (Relation rel: schema.getRelations()) {

      if (!(rel.getSchema() instanceof OntologyLink) ||
          bannedKeys.contains(rel.getName())) {
        continue;
      }

      // For the current relation, count for each of its text
      // value, how many such text value has occurred across
      // all maps (entities)
      Map<String, Integer> valueMap = new HashMap<String, Integer>();
      for (Map<String, String> instance: entities) {
        String value = instance.get(rel.getName());
        Integer count = valueMap.get(value);
        if (count == null) {
          count = 1;
        } else {
          count++;
        }
        valueMap.put(value, count);
      }

      // For the current relation, count the number of text values
      // that have occurred more than once, in another word, for the
      // current relation, count the number of its text values that
      // have occurred more than once across all instances of the input
      // schema
      // Eric: Considering relation's text value is the aggregate text
      // values of all its leaf children, it seems EXTREMELY LIKELY
      // that relations will almost always be considered as keys. SO IS
      // THIS RIGHT? WHY CONSIDER RELATIONS AS KEYS?
      int nonUnique = 0;
      for (Map.Entry<String, Integer> entry: valueMap.entrySet()) {
        Integer count = entry.getValue();
        if (count != 1) {
          nonUnique++;
        }
      }

      // Consider the relation as a key if the text value of relation
      // is unique "enough" (passing the threshold), then convert
      // such relation to attribute (NOT CLEAR!!! WHAT ABOUT ITS CHILDREN)
      int total = valueMap.size();
      if (nonUnique / (double) total <= uniqunessThreshold) {
        // Eric: Why are you so sure this is an ontology schema?
        OntologyLink promotedLeafSchema = (OntologyLink) rel.getSchema();
        Set<String> typeURIs = promotedLeafSchema.getTypeURIs();
        depromotedRels.add(rel);
        schema.setTypeURIs(typeURIs);
        Attribute attr = new Attribute(schema, promotedLeafSchema.getName(), rel.getPath(), true);
        schema.addAttribute(attr);
        LogUtils.debug(getClass(), schema.getName() + "." + attr.getName() + " is unique");
      }
    }

    // Remove such identified key relation from the input,
    // schema and possibly remove such relation altogether
    for (Relation rel: depromotedRels) {
      schema.getRelations().remove(rel);
      maybeRemoveSchema(rel.getSchema());
    }

  }

  /*
   * Helper Function to remove a schema iff this schema
   * is not a relation of any other schemas
   */
  private void maybeRemoveSchema(Schema schemaToBeRemoved) {

    for (Schema schema : schemas.values()) {
      // The if-continue just skips the schema of the
      // same name
      if (schema.equals(schemaToBeRemoved)) {
        continue;
      }
      // Now we know the current schema has a
      // different name
      for (Relation relation: schema.relations) {
        if (relation.schema.equals(schemaToBeRemoved)) {
          return;
        }
      }
    }
    // Only remove the schema if it's not a relation
    // of any other schemas
    // Eric: WRONG, should be schemaToBeRemoved.getName()
    // schemas.remove(schemaToBeRemoved);
    schemas.remove(schemaToBeRemoved.getName());
  }

  /*
   * Step 5. Intra-link schemas
   */
  private void intralinkSchemas(Document doc, double linkingThreshold)
      throws XPathExpressionException {

    // Eric: Why is INTERLINKING used under intralinkSchemas()?
    if (!isStepEnabled(MappingStep.INTERLINKING)) {
      return;
    }

    for (Schema schema: schemas.values()) {

      // Get all instances of the schema
      NodeList nl = XMLUtils.getNodesByPath(schema.getPath(), null, doc);

      for (Attribute attr: schema.getAttributes()) {

        List<Attribute> matchedAttributes = new LinkedList<Attribute>();

        // Eric: THIS IS WRONG FOR "facility", for example, because
        // the path of the attribute includes "facility"? Design choice?
        Set<String> propertyValues = XMLUtils.getStringsByPath(
            schema.getPath() + "/" + attr.getPath(), null, doc);

        for (Schema targetSchema: schemas.values()) {

          // Skip current iteration when the two schemas are the same
          if (targetSchema.equals(attr.getParent())) {
            continue;
          }

          for (Attribute targetAttribute: targetSchema.getAttributes()) {
            if (!targetAttribute.isKey()) {
              continue;
            }

            if (targetAttribute.equals(attr)) {
              continue;
            }

            Set<String> targetPropertyValues = XMLUtils.getStringsByPath(
                targetSchema.getPath() + "/" + targetAttribute.getPath(), null, doc);

            Set<String> sharedValues =
                org.openjena.atlas.lib.SetUtils.intersection(propertyValues,
                    targetPropertyValues);

            if (sharedValues.size() / (double) propertyValues.size() >= linkingThreshold){
              matchedAttributes.add(targetAttribute);
            }
          }
        }

        for (Attribute matchedAttribute: matchedAttributes) {
          Schema taregetSchema = matchedAttribute.getParent();

          Set<Attribute> lookupKeys = new HashSet<Attribute>();

          // Eric: What's the point of this add?
          lookupKeys.add(new Attribute(schema, matchedAttribute.getName(),
              attr.getPath(), false));

          Relation rel = new Relation(schema, attr.getName() + "_to_" +
              matchedAttribute.getName() + "_internal_relation", attr.getPath(),
              taregetSchema, lookupKeys);

          schema.addRelation(rel);
        }
      }

    }
  }

  private void addEntities(Schema schema, Document mappingRoot, String path,
      String typePrefix) {

    if (schema instanceof OntologyLink) {
      Element entityElement = mappingRoot.createElementNS(
          "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "entity");
      entityElement.setAttribute("path", schema.getPath());
      entityElement.setAttribute("type", typePrefix
          + schema.getName());
      mappingRoot.getDocumentElement().appendChild(entityElement);

      Element idElement = mappingRoot.createElementNS(
          "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "id");
      idElement.setTextContent(typePrefix + "${" + Entity.AUTO_GENERATED + "}");
      entityElement.appendChild(idElement);

      Element attributeElement = mappingRoot.createElementNS(
          "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "property");
      attributeElement.setAttribute("path", "text()");
      attributeElement.setAttribute("name", typePrefix + "name_property");
      attributeElement.setAttribute("key", "true");
      entityElement.appendChild(attributeElement);


      for (String ontologyURI : ((OntologyLink) schema).getTypeURIs()) {

        String label = OpenCycOntology.getInstance()
            .getLabelForResource(ontologyURI);

        Element ontologyElement = mappingRoot.createElementNS(
            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
            "ontology-link");
        ontologyElement.setAttribute("uri", ontologyURI);
        ontologyElement.setAttribute("label", label);
        entityElement.appendChild(ontologyElement);
      }

    } else {
      Element entityElement = mappingRoot.createElementNS(
          "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "entity");
      entityElement.setAttribute("path", schema.getPath());
      entityElement.setAttribute("type", typePrefix
          + schema.getName());
      mappingRoot.getDocumentElement().appendChild(entityElement);

      Element idElement = mappingRoot.createElementNS(
          "http://www.cs.toronto.edu/xml2rdf/mapping/v1", "id");
      idElement.setTextContent(typePrefix + "${" + Entity.AUTO_GENERATED + "}");
      entityElement.appendChild(idElement);

      // TODO: reload attributes

      for (String ontologyURI : schema.getTypeURIs()) {
        String label =
            OpenCycOntology.getInstance().getLabelForResource(ontologyURI);

        Element ontologyElement = mappingRoot.createElementNS(
            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
            "ontology-link");
        ontologyElement.setAttribute("uri", ontologyURI);
        ontologyElement.setAttribute("label", label);
        entityElement.appendChild(ontologyElement);
      }


      for (Attribute attribute : schema.getAttributes()) {
        Element attributeElement = mappingRoot.createElementNS(
            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
            "property");
        attributeElement.setAttribute("path", attribute.getPath());
        attributeElement.setAttribute("name",
            typePrefix + attribute.getName() + "_property");
        attributeElement.setAttribute("key", String.valueOf(attribute.isKey()));

        for (String ontologyURI: attribute.getTypeURIs()) {
          Element ontologyElement = mappingRoot.createElementNS(
              "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
              "ontology-link");
          String label =
              OpenCycOntology.getInstance().getLabelForResource(ontologyURI);

          ontologyElement.setAttribute("uri", ontologyURI);
          ontologyElement.setAttribute("label", label);
          attributeElement.appendChild(ontologyElement);
        }

        entityElement.appendChild(attributeElement);
      }

      for (Relation relation : schema.getRelations()) {
        Element relationElement = mappingRoot.createElementNS(
            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
            "relation");
        relationElement.setAttribute("path", relation.getPath());
        relationElement.setAttribute("targetEntity", typePrefix
            + relation.getSchema().getName());
        relationElement.setAttribute("name", typePrefix + relation.getName() + "_rel");
        entityElement.appendChild(relationElement);

        Element lookupElement = mappingRoot.createElementNS(
            "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
            "lookupkey");

        for (Attribute attr: relation.getLookupKeys()) {
          Element targetPropertyElement = mappingRoot.createElementNS(
              "http://www.cs.toronto.edu/xml2rdf/mapping/v1",
              "target-property");
          targetPropertyElement.setAttribute("path", attr.getPath());
          String name = attr.getName();
          String[] nameSplitted = name.split("\\.");
          String newName = nameSplitted[0];
          for (int i = 1; i < nameSplitted.length - 1; i++) {
            newName += "." + nameSplitted[i] + "_rel";
          }

          if (nameSplitted.length == 1) {
            newName += "_prop";
          } else {
            newName += nameSplitted[nameSplitted.length - 1] + "_prop";
          }

          targetPropertyElement.setAttribute("name", typePrefix + attr.getName());
          lookupElement.appendChild(targetPropertyElement);
        }

        relationElement.appendChild(lookupElement);
      }

    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // Old Functions no longer in use
  ////////////////////////////////////////////////////////////////////////////

  /*
   * The functions below are NOT used currently and possibly obsolete!
   */

  private void intralinkSchemasOld(Document doc, double linkingThreshold)
      throws XPathExpressionException {
    for (Schema schema: schemas.values()) {
      NodeList nl = XMLUtils.getNodesByPath(schema.getPath(), null, doc);

      for (Attribute attr: schema.getAttributes()) {

        Map<Attribute, Integer> attributeMatchMap =
            new HashMap<Attribute, Integer>();

        attributeLoop:  for (int i = 0; i < nl.getLength(); i++) {
          if (nl.item(i) instanceof Element) {
            Element entityElement = (Element) nl.item(i);
            Set<String> propertyValues = XMLUtils.getStringsByPath(
                attr.getPath(), entityElement, doc);

            for (Schema targetSchema: schemas.values()) {
              for (Attribute targetAttribute: targetSchema.getAttributes()) {
                if (!targetAttribute.isKey()) {
                  continue;
                }

                if (targetAttribute.equals(attr)) {
                  continue;
                }

                NodeList valueNodeList = XMLUtils.getNodesByPath(
                    targetSchema.getPath() + "/" + targetAttribute.getPath(),
                    null, doc);

                for (int j = 0; j < valueNodeList.getLength(); j++) {
                  Node node = valueNodeList.item(j);
                  if (propertyValues.contains(node.getTextContent().trim())) {
                    Integer count = attributeMatchMap.get(targetAttribute);
                    if (count == null) {
                      count = 0;
                    }
                    attributeMatchMap.put(targetAttribute, count + 1);
                    continue attributeLoop;
                  }
                }

              }
            }

          }
        }

        Attribute matchedAttribute = null;
        for (Map.Entry<Attribute, Integer> entry: attributeMatchMap.entrySet()) {
          if (entry.getValue() / (double) nl.getLength() >= linkingThreshold) {
            matchedAttribute = entry.getKey();
            break;
          }
        }

        if (matchedAttribute != null) {
          Schema taregetSchema = matchedAttribute.getParent();

          Set<Attribute> lookupKeys = new HashSet<Attribute>();
          lookupKeys.add(new Attribute(schema, matchedAttribute.getName(),
              attr.getPath(), false));
          Relation rel = new Relation(schema,
              attr.getName() + "_interanl_relation", attr.getPath(),
              taregetSchema, lookupKeys);
          schema.addRelation(rel);
        }
      }
    }
  }

  private boolean isRelationOneToOne(Document doc, Schema schema, Relation rel)
      throws XPathExpressionException {
    Map<Set<String>, Set<Set<String>>> relMap =
        new HashMap<Set<String>, Set<Set<String>>>();
    Map<Set<String>, Set<Set<String>>> reverseRelMap =
        new HashMap<Set<String>, Set<Set<String>>>();

    String path = schema.getPath();
    NodeList entitiesNL = XMLUtils.getNodesByPath(path, null, doc);
    for (int i = 0; i < entitiesNL.getLength(); i++) {
      Element entityElement = (Element) entitiesNL.item(i);
      Set<String> entityValue =
          new HashSet<String>(XMLUtils.getAllLeaveValues(entityElement));

      NodeList relationsNL = XMLUtils.getNodesByPath(rel.getPath(),
          entityElement, doc);
      for (int j = 0; j < relationsNL.getLength(); j++) {
        Set<String> relValue = new HashSet<String>(
            XMLUtils.getAllLeaveValues((Element) relationsNL.item(j)));
        Set<Set<String>> entitySet = relMap.get(relValue);
        if (entitySet == null) {
          entitySet = new HashSet<Set<String>>();
          relMap.put(relValue, entitySet);
        }

        entitySet.add(entityValue);



        Set<Set<String>> relSet = reverseRelMap.get(entityValue);
        if (relSet == null) {
          relSet = new HashSet<Set<String>>();
          reverseRelMap.put(entityValue, relSet);
        }

        relSet.add(relValue);
        if (entitySet.size() > 1 || relSet.size() > 1) {
          LogUtils.debug(getClass(), schema + " . " + rel +
              " is not one to one because of " + relValue);
          return false;
        }
      }
    }


    //    for (Map.Entry<Set<String>, Set<Set<String>>> relEntry: relMap.entrySet()) {
    //      if (relEntry.getValue().size() > 1) {
    //        LogUtils.debug(getClass(), schema + " . " + rel + " is not one to one because of " + relEntry);
    //        return false;
    //      }
    //    }
    //
    //    for (Map.Entry<Set<String>, Set<Set<String>>> entityEntry: reverseRelMap.entrySet()) {
    //      if (entityEntry.getValue().size() > 1) {
    //        LogUtils.debug(getClass(), schema + " . " + rel + " is not one to one because of " + entityEntry);
    //        return false;
    //      }
    //    }

    return relMap.size() > 0 && reverseRelMap.size() > 0;
  }

  private double getEntropyOfRelation(Document doc, Schema schema, Relation rel)
      throws XPathExpressionException {
    Map<Set<String>, Set<Set<String>>> relMap =
        new HashMap<Set<String>, Set<Set<String>>>();

    String path = schema.getPath();
    NodeList entitiesNL = XMLUtils.getNodesByPath(path, null, doc);
    for (int i = 0; i < entitiesNL.getLength(); i++) {
      Element entityElement = (Element) entitiesNL.item(i);
      Set<String> entityValue = new HashSet<String>(
          XMLUtils.getAllLeaveValues(entityElement));

      NodeList relationsNL = XMLUtils.getNodesByPath(rel.getPath(),
          entityElement, doc);
      for (int j = 0; j < relationsNL.getLength(); j++) {
        Set<String> relValue = new HashSet<String>(
            XMLUtils.getAllLeaveValues((Element) relationsNL.item(j)));
        Set<Set<String>> entitySet = relMap.get(relValue);
        if (entitySet == null) {
          entitySet = new HashSet<Set<String>>();
          relMap.put(relValue, entitySet);
        }
        entitySet.add(entityValue);
      }
    }

    int sum = 0;
    for (Map.Entry<Set<String>, Set<Set<String>>> entry: relMap.entrySet()) {
      sum += entry.getValue().size();
    }

    double entropy = 0;
    for (Map.Entry<Set<String>, Set<Set<String>>> entry: relMap.entrySet()) {
      double p = entry.getValue().size() / (double) sum;
      entropy += -p * Math.log(p);
    }
    return entropy;
  }


}
