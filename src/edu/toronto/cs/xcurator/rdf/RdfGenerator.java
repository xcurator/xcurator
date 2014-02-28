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
package edu.toronto.cs.xcurator.rdf;

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;

/**
 * The pipeline executor for RDF generation.
 */
public class RdfGenerator {

  private List<RdfGenerationStep> pipeline;
  private List<DataDocument> dataDocuments;
  private Mapping mapping;

  public RdfGenerator(DataDocument doc, Mapping mapping) {
    pipeline = new ArrayList<>();
    dataDocuments = new ArrayList<>();
    dataDocuments.add(doc);
    this.mapping = mapping;
  }

  public RdfGenerator(Mapping mapping) {
    pipeline = new ArrayList<>();
    dataDocuments = new ArrayList<>();
    this.mapping = mapping;
  }

  public RdfGenerator addStep(RdfGenerationStep step) {
    pipeline.add(step);
    return this;
  }
  
  public RdfGenerator addDataDocument(DataDocument doc) {
    dataDocuments.add(doc);
    return this;
  }

  public void generateRdfs() {
    for (RdfGenerationStep step : pipeline) {
      step.process(dataDocuments, mapping);
    }
  }
}
