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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface MappingGenerator {
  Document generateMapping(Element root, String typePrefix);
  enum MappingStep {
    BASIC, // Should be always in the list.
    DUPLICATE_REMOVAL,
    INTERLINKING,
    INTRALINKING, // Recall enhancement.
    SCHEMA_FLATTENING, // Precision enhancement.
    FIND_KEYS,
  }
}
