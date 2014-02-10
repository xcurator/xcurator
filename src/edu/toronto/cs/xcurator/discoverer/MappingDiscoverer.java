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

import edu.toronto.cs.xcurator.mapping.Mapping;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;

/**
 *
 * @author zhuerkan
 */
public class MappingDiscoverer {
  
  private List<MappingDiscoveryStep> pipeline;
  private Mapping mapping;
  private List<DataDocument> dataDocuments;
  
  // Initialize the discoverer for only one data document
  public MappingDiscoverer(Document dataDocument, String entityIdPattern, Mapping mapping) {
    pipeline = new ArrayList<>();
    dataDocuments = new ArrayList<>();
    dataDocuments.add(new DataDocument(dataDocument, entityIdPattern));
    this.mapping = mapping;
  }
  
  public MappingDiscoverer addStep(MappingDiscoveryStep step) {
    pipeline.add(step);
    return this;
  }
  
  public void discoverMapping() {
    for (MappingDiscoveryStep step : pipeline) {
      step.process(dataDocuments, mapping);
    }
  }
  
}
