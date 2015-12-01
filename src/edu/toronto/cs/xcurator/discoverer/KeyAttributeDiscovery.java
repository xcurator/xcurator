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
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Mapping;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ekzhu
 */
public class KeyAttributeDiscovery implements MappingDiscoveryStep {

    @Override
    public void process(List<DataDocument> dataDocuments, Mapping mapping) {
        Iterator<Entity> it = mapping.getEntityIterator();
        while (it.hasNext()) {
      // For each entity, find attribute whose instances are unique
            // That is, the cardinality of the attribute instances should equal
            // to the cardinality of the entity instances

            // The value attribute should not be used as key.
            // Its instance count should be zero
            Entity entity = it.next();
            int instanceCount = entity.getXmlInstanceCount();
            Iterator<Attribute> attrIt = entity.getAttributeIterator();
            while (attrIt.hasNext()) {
                Attribute attr = attrIt.next();
                // This is a hack, the key identification algorithm needs to be 
                // improved.
                if (attr.getInstances().size() == instanceCount
                        && attr.getId().endsWith(".id")) {
                    attr.asKey();
                }
            }
        }
    }
}
