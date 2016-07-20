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
package edu.toronto.cs.xml2rdf.jena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class SKOS {

    /**
     * <
     * p>
     * The RDF model that holds the vocabulary terms</p>
     */
    private static Model m_model = ModelFactory.createDefaultModel();

    /**
     * <
     * p>
     * The namespace of the vocabulary as a string ({@value})</p>
     */
    public static final String NS = "http://www.w3.org/2004/02/skos/core#";

    /**
     * <
     * p>
     * The namespace of the vocabulary as a string</p>
     *
     * @see #NS
     */
    public static String getURI() {
        return NS;
    }

    /**
     * <
     * p>
     * The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = m_model.createResource(NS);

    /**
     * A resource that denotes the OWL-full sublanguage of OWL
     */
    public static final Resource FULL_LANG = m_model.getResource(getURI());

    public static final Property closeMatch = m_model.createProperty("http://www.w3.org/2004/02/skos/core#closeMatch");

    public static final Property exactMatch = m_model.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch");
}
