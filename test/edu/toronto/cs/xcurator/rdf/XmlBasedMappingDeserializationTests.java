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
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.common.XmlParser;
import edu.toronto.cs.xcurator.mapping.Entity;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XmlBasedMappingDeserializationTests {

    private XmlBasedMappingDeserialization step;
    private final String exampleEntityTypeUri = "http://example.org/resource/class/us-gaap-NonoperatingIncomeExpense";

    @Before
    public void setup() {

    }

    @Test
    public void test_process_fb() throws FileNotFoundException {
        Mapping mapping = new XmlBasedMapping();
        step = new XmlBasedMappingDeserialization(
                new FileInputStream("output/fb-20121231-mapping.xml"),
                new XmlParser());
        step.process(new ArrayList<DataDocument>(), mapping);

        Assert.assertTrue(mapping.isInitialized());
        Assert.assertNotNull(mapping.getEntity(exampleEntityTypeUri));
        Entity e = mapping.getEntity("http://example.org/resource/class/xbrli-unitNumerator");
        e.hasAttribute("http://example.org/resource/property/xbrli-measure");
    }

    @Test
    public void test_process_msft() throws FileNotFoundException {
        Mapping mapping = new XmlBasedMapping();
        step = new XmlBasedMappingDeserialization(
                new FileInputStream("output/msft-20130630-mapping.xml"),
                new XmlParser());
        step.process(new ArrayList<DataDocument>(), mapping);

        Assert.assertTrue(mapping.isInitialized());
        Assert.assertNotNull(mapping.getEntity(exampleEntityTypeUri));
        Entity e = mapping.getEntity("http://example.org/resource/class/unitNumerator");
        e.hasAttribute("http://example.org/resource/property/measure");
    }
}
