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
package edu.toronto.cs.xcurator.mapping;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Element;

public class Attribute implements MappingModel {

    Schema schema;

    SearchPath paths;

    String rdfUri;

    String xmlTypeUri;

    private Set<String> instances;

    boolean isKey;

    public Attribute(Schema schema, String rdfUri, String xmlTypeUri) {
        this.schema = schema;
        this.rdfUri = rdfUri;
        this.xmlTypeUri = xmlTypeUri;
        this.paths = new SearchPath();
        this.instances = new HashSet<>();
        this.isKey = false;
    }

    @Override
    public String getId() {
        return schema.xmlTypeUri + "." + xmlTypeUri;
    }

    @Override
    public void addPath(String path) {
        paths.addPath(path);
    }

    @Override
    public String getPath() {
        return paths.getPath();
    }

    public boolean isKey() {
        return this.isKey;
    }

    public void asKey() {
        isKey = true;
    }

    public void addInstance(String value) {
        value = value.trim();
        value = value.replaceAll("[\\t\\n\\r]+", " ");
        this.instances.add(value);
    }

    public void addInstances(Set<String> others) {
        for (String val : others) {
            addInstance(val);
        }
    }

    public Set<String> getInstances() {
        return instances;
    }

    public String getRdfUri() {
        return rdfUri;
    }

    public void resetRdfUri(String rdfUri) {
        this.rdfUri = rdfUri;
    }

    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public String toString() {
        StringBuilder instanceSb = new StringBuilder();
        instanceSb.append("[");
        if (!instances.isEmpty()) {
            for (String str : instances) {
//                str = str.replace("\"", "\\\"");
                str = StringEscapeUtils.escapeJava(str);
                if (str.length() > 30) {
                    str = str.substring(0, 30) + "...";
                }
                instanceSb.append("\"").append(str).append("\"").append(", ");
            }
            instanceSb.deleteCharAt(instanceSb.length() - 1);
            instanceSb.deleteCharAt(instanceSb.length() - 1);
        }
        instanceSb.append("]");
        return "{"
                + "\"Attribute\": {"
                + "\"rdfUri\":" + "\"" + rdfUri + "\""
                + ", \"xmlTypeUri\":" + "\"" + xmlTypeUri + "\""
                + ", \"instances\":" + instanceSb
                + '}'
                + '}';
    }
}
