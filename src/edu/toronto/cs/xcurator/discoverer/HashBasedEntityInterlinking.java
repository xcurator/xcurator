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
package edu.toronto.cs.xcurator.discoverer;

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.common.RdfUriBuilder;
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.Reference;
import edu.toronto.cs.xcurator.mapping.Relation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ekzhu
 */
public class HashBasedEntityInterlinking implements MappingDiscoveryStep {

    private final RdfUriBuilder rdfUriBuilder;

    public HashBasedEntityInterlinking(RdfUriBuilder rdfUriBuilder) {
        this.rdfUriBuilder = rdfUriBuilder;
    }

    @Override
    public void process(List<DataDocument> dataDocuments, Mapping mapping) {
        Map<String, Set<Attribute>> attrHash = new HashMap<>();

        // Hash all attributes into the hash table by their instances
        hashAllAttributeValues(mapping, attrHash);

        for (Map.Entry<String, Set<Attribute>> bucket : attrHash.entrySet()) {
            String hashValue = bucket.getKey();
            Set<Attribute> hashSet = bucket.getValue();
//      System.out.println("At hash bucket " + hashValue);

            // Identity the attributes that are keys
            Set<Attribute> keyAttrs = new HashSet<>();
            for (Attribute hashedAttr : hashSet) {
                if (hashedAttr.isKey()) {
                    keyAttrs.add(hashedAttr);
                }
            }

            // Create relations between the entities that has their keys hashsed
            // to this bucket and other entities that has their normal attributes
            // hased to the same bucket
            for (Attribute keyAttr : keyAttrs) {
                for (Attribute hashedAttr : hashSet) {
                    // Skip the attribtues that are keys (including itself)
                    if (hashedAttr.isKey()) {
                        continue;
                    }

                    // Create new relation
                    Entity subject = hashedAttr.getEntity();
                    Entity object = keyAttr.getEntity();
                    String rdfUri = rdfUriBuilder.getRdfRelationUriFromEntities(subject, object);
                    Relation relation = new Relation(subject, object, rdfUri);

                    // Use the absolute path of the object entity as the relation path
                    relation.addPath(object.getPath());

                    // Create reference for this relation
                    Reference reference = new Reference(hashedAttr.getPath(), keyAttr.getPath());
                    relation.addReference(reference);

                    // Add the relation to the subject entity
                    subject.addRelation(relation);
                }
            }

        }
    }

    private void hashAllAttributeValues(Mapping mapping,
            Map<String, Set<Attribute>> attrHash) {
        Iterator<Entity> it = mapping.getEntityIterator();
        while (it.hasNext()) {
            Entity entity = it.next();
            Iterator<Attribute> attrIt = entity.getAttributeIterator();
            while (attrIt.hasNext()) {
                Attribute attr = attrIt.next();
                Set<String> attrValues = attr.getInstances();
                for (String val : attrValues) {
                    if (attrHash.containsKey(val)) {
                        attrHash.get(val).add(attr);
                    } else {
                        Set<Attribute> hashedSet = new HashSet<>();
                        hashedSet.add(attr);
                        attrHash.put(val, hashedSet);
                    }
                }
            }
        }
    }

}
