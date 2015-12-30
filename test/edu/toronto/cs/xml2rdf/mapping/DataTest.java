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
package edu.toronto.cs.xml2rdf.mapping;

import java.util.Set;

import junit.framework.TestCase;

import com.hp.hpl.jena.rdf.model.Model;

import edu.toronto.cs.xml2rdf.jena.JenaUtils;

public class DataTest extends TestCase {

    public void testData() {
        String typePrefix = "http://www.linkedct.org/0.1#";
        Model model = JenaUtils.getTDBModel("finaltdb");
        Set<String> entityTypes = Mapping.getAllTypes(model, typePrefix);
        System.out.println("EntityTypes: >>>>");
        System.out.println(entityTypes);
        model.close();
    }
}
