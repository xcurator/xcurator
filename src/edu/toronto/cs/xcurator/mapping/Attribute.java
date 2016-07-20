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

public class Attribute implements MappingModel {

    Entity entity;

    SearchPath paths;

    String rdfUri;

    String xmlTypeUri;

    Set<String> instances;

    boolean isKey;

    public Attribute(Entity entity, String rdfUri, String xmlTypeUri) {
        this.entity = entity;
        this.rdfUri = rdfUri;
        this.xmlTypeUri = xmlTypeUri;
        this.paths = new SearchPath();
        this.instances = new HashSet<>();
        this.isKey = false;
    }

    @Override
    public String getId() {
        return entity.xmlTypeUri + "." + xmlTypeUri;
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
        this.instances.add(value);
    }

    public void addInstances(Set<String> others) {
        this.instances.addAll(others);
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

    public Entity getEntity() {
        return this.entity;
    }
}
