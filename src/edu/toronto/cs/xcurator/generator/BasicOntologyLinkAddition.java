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
import edu.toronto.cs.xcurator.model.OntologyLinkInstance;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.model.RelationInstance;
import edu.toronto.cs.xcurator.model.Schema;
import edu.toronto.cs.xcurator.model.SchemaInstance;
import edu.toronto.cs.xml2rdf.freebase.FreeBaseLinker;
import edu.toronto.cs.xml2rdf.mapping.generator.SchemaException;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class BasicOntologyLinkAddition implements MappingStep {

    private final int maxOnotlogyLookup;
    private final int leafPromotionThreshold;
    private final double matchThreshold;
    private final double digitThreshold;
    private final double ontologyMatchingThreshold;
    private final StringMetric stringMetric;

    public BasicOntologyLinkAddition(int maxOnotlogyLookup,
            int leafPromotionThreshold, double digitThreshold,
            double matchThreshold, double ontologyMatchingThreshold,
            StringMetric stringMetric) {
        this.maxOnotlogyLookup = maxOnotlogyLookup;
        this.leafPromotionThreshold = leafPromotionThreshold;
        this.matchThreshold = matchThreshold;
        this.digitThreshold = digitThreshold;
        this.ontologyMatchingThreshold = ontologyMatchingThreshold;
        this.stringMetric = stringMetric;
    }

    @Override
    public void process(Element root, Map<String, Schema> schemas) {

        // Inspect all attributes to see if they can be linked
        // externally and become an ontologyLink.
        // Iterate through each schema
        for (Schema schema : schemas.values()) {

            // Remember the list of attributes, in case it may be modified later
            Set<Attribute> attrSet = new HashSet<Attribute>();
            attrSet.addAll(schema.getAttributes());

            // Iterate through all attributes of the current schema
            for (Attribute attribute : attrSet) {

                // Get all attribute values of all attribute instances
                Set<String> attrValues = new HashSet<String>();
                for (Set<AttributeInstance> aiSet : attribute.getInstanceMap().values()) {
                    Iterator<AttributeInstance> aiIter = aiSet.iterator();
                    while (aiIter.hasNext()) {
                        attrValues.add(((AttributeInstance) aiIter.next()).getValue());
                    }
                }

                // Find all typeIDs (above threshold) based on attribute values
                Set<String> types = findOntologyTypes(attrValues, this.matchThreshold,
                        this.digitThreshold, this.maxOnotlogyLookup, this.ontologyMatchingThreshold,
                        this.stringMetric);

                // If types contains some typeIDs and values contains enough text values
                // Eric: What's the significance of attrValues.size() >= leafPromotionThreshold
                // since values are merely the different text values of the current leaf node?
                // Put it differently, if the attribute does not have enough attribute values,
                // it probably will not return any typeIDs anyways.
                if (types != null && types.size() > 0 && attrValues.size() >= this.leafPromotionThreshold) {

                    // When this (or any other) attribute is created for a schema, it goes through
                    // the following steps.
                    //
                    // A1. The attribute is created with its parent schema being the current schema.
                    // A2. The attribute is added to a set of attributes of the current schema.
                    // A3. The current schema and attribute instance is stored in the set
                    //     instanceMap and reverseInstanceMap of the attribute.
                    //
                    // We must reverse ALL this and CHANGE the attribute into a ontologyLink schema.
                    // When a schema is created, it goes through the following step.
                    //
                    // S1. Create the schema and added to the global schemas map.
                    // S2. Add the current instance of the schema to the set of schema instances
                    //     of the current schema.
                    // S3. Merge the current schema. (DOES NOT APPLY HERE.)
                    // S4. Create the lookupKeys for the current schema.
                    // S5. Create the relation between the parent schema and the current schema, which 
                    //     adds this relation to the set relations of the parent schema, and to the
                    //     set reverseRelations of the current (child) schema.
                    // S6. Create the relation instance involving the parent schema instance and the
                    //     current (child) schema instance, and then store to the map instanceMap
                    //     and reverseInstanceMap of this relation.
                    // Get the name and the path of the new ontologyLink schema
                    String name = attribute.getName();
                    String path = schema.getPath() + "/" + name; // ABSOLUTE PATH

                    // Follow S1 above
                    // Create a schema for the current leaf child element, if none exists yet
                    // Eric: QUESTION: In the current implementation, an attribute of the same
                    // name CAN exist under different schemas, however, when these attributes
                    // are promoted to become a ontologyLink schema, ONLY ONE can exist. ISN'T
                    // THERE A CONFLICT HERE?
                    OntologyLink ontologyLink = (OntologyLink) schemas.get(name);
                    if (ontologyLink == null) {
                        ontologyLink = new OntologyLink(null, name, path, types);
                        schemas.put(name, ontologyLink);
                    }

                    // Follow S2 above and skip S3 above
                    // Create and add ALL schema instances, which are just ALL attribute instances
                    for (Set<AttributeInstance> aiSet : attribute.getInstanceMap().values()) {
                        Iterator<AttributeInstance> aiIter = aiSet.iterator();
                        while (aiIter.hasNext()) {
                            SchemaInstance instance = new SchemaInstance(aiIter.next().getContent());
                            ontologyLink.addInstace(instance);
                        }
                    }

                    // Follow S4 above
                    // Create the lookupKeys
                    //
                    // Eric: Because the attribute element is a leaf, it does NOT contain
                    // other child elements, which means the set lookupKeys contains ONLY ONE attribute,
                    // with its path being "text()".
                    // Is this the correct understanding? Once again, I'm not sure why lookupKeys
                    // are needed.
                    Set<Attribute> lookupKeys = new HashSet<Attribute>();
                    lookupKeys.add(new Attribute(schema, name + ".name", "text()", false));

                    // Follow S5 above
                    // Create the relation
                    Relation relation = new Relation(schema, name, name, ontologyLink, lookupKeys);

                    // Follow S6 above
                    // Create and all ALL relation instances, which are just ALL attribute instances
                    for (SchemaInstance from : attribute.getInstanceMap().keySet()) {
                        for (AttributeInstance ai : attribute.getInstanceMap().get(from)) {
                            // Create an ontologyLink instance to preserve both its XML tag content
                            // and the actual value, in case this instance needs to be converted 
                            // back to attribute instance again during schema flattening step
                            OntologyLinkInstance to = new OntologyLinkInstance(ai.getContent(),
                                    ai.getValue());
                            RelationInstance ri = new RelationInstance(from, to);
                            relation.addInstance(ri);
                        }
                    }

                    // Now that all steps of schema creation is done, we reverse all steps
                    // of attribute creation.
                    // Reverse A3 above
                    attribute.getInstanceMap().clear();
                    attribute.getReverseInstanceMap().clear();

                    // Reverse A2 above
                    schema.getAttributes().remove(attribute);

                    // Reverse A1 above
                    // Let Java Garbage Collector do its job, :).
                }

            }
        }

    }

    private Set<String> findOntologyTypes(Set<String> attrValues,
            double matchThreshold, double digitThreshold, int maxOnotlogyLookup,
            double ontologyMatchingThreshold, StringMetric stringMetric) {

        // Instantiate FreeBaseLinker
        FreeBaseLinker freebase = new FreeBaseLinker();

        // For each String typeIDs, count how many Integer times they are
        // returned from freebase
        Map<String, Integer> commonTypes = new HashMap<String, Integer>();

        // Count how many times no typeIDs is returned for a text value
        int noTypeIDTimes = 0;

        // Count how many attribute values have been inspected
        int avInspected = 0;

        // Iterate through all instances of attribute values
        Iterator<String> avIter = attrValues.iterator();
        while (avIter.hasNext() && (maxOnotlogyLookup == -1 || avInspected < maxOnotlogyLookup)) {

            // Break all iterations if there are too many times (count) where no
            // typeIDs is returned, or enough different typeIDs have already returned.
            if (noTypeIDTimes > 100 && commonTypes.size() < 100) {
                break;
            }

            // Get the attribute value
            String attrValue = (String) avIter.next();

            // Validate the attribute value and skip the current iteration if invalid
            if (!validateAttributeValue(attrValue, digitThreshold)) {
                continue;
            }

            // A set that holds all freebase typeIDs that look something
            // like "http://rdf.freebase.com/rdf/music.release"
            Set<String> types = new HashSet<String>();

            // Get the list of typeIDs based on the text value, and the typeIDs look like the following:
            // "http://rdf.freebase.com/rdf/music.release"
            Set<String> freebaseTypes = freebase.findTypesForResource(attrValue,
                    stringMetric, ontologyMatchingThreshold);

            // Add all typeIDs if freebaseTypes is not null
            if (freebaseTypes != null) {
                types.addAll(freebaseTypes);
            }

            // TODO: Google Spell Checker API does not seem to work at the moment,
            // (URL return 502 error), WILL IMPLEMENT LATER
            // Skip the current iteration if still no typeIDs is found
            if (types.size() == 0) {
                noTypeIDTimes++;
                continue;
            }

            // Count for each typeID, the number of times it has occurred
            for (String type : types) {
                Integer typeCount = commonTypes.get(type);
                if (typeCount == null) {
                    typeCount = 0;
                }
                typeCount++;
                commonTypes.put(type, typeCount);
            }

            // Increment the number of attribute values inspected
            avInspected++;

        }

        // Add typeIDs to types this typeID has occurred enough times (over the threshold)
        Set<String> types = new HashSet<String>();
        for (Map.Entry<String, Integer> entry : commonTypes.entrySet()) {
            if (entry.getValue() / (double) attrValues.size() >= matchThreshold) {
                types.add(entry.getKey());
            }
        }

        return types;

    }

    private boolean validateAttributeValue(String attrValue, double digitThreshold) {

        // The attribute value is invalid if it is empty, longer
        // than 50 characters, or consists entirely of digits
        if (attrValue.trim().length() == 0 || attrValue.length() > 50
                || attrValue.matches("^\\d+$")) {
            return false;
        }

        // Remove all digits from the text value of the value
        String withoutNumbers = attrValue.trim().replaceAll("\\d", "");

        // The attribute value is invalid if the ratio of the length of digits over
        // the total length of the text value is too high
        if ((attrValue.length() - withoutNumbers.length()) / (double) attrValue.length()
                >= digitThreshold) {
            return false;
        }

        return true;

    }

}
