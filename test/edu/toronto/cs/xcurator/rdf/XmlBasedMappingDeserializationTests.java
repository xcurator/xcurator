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

import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class XmlBasedMappingDeserializationTests {

  private XmlBasedMappingDeserialization step;
  private final String exampleEntityTypeUri = "http://fasb.org/us-gaap/2012-01-31#NonoperatingIncomeExpense";

  @Before
  public void setup() throws FileNotFoundException {
    step = new XmlBasedMappingDeserialization(
            new FileInputStream("output/fb-20121231-mapping.xml"),
            new XmlParser());
  }

  @Test
  public void test_process() {
    Mapping mapping = new XmlBasedMapping();
    step.process(new ArrayList<Document>(), mapping);
    
    Assert.assertTrue(mapping.isInitialized());
    Assert.assertNotNull(mapping.getEntity(exampleEntityTypeUri));
  }
}
