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

import edu.toronto.cs.xcurator.model.AttributeOld;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.RelationOld;
import edu.toronto.cs.xcurator.xml.NsContext;
import java.util.Map;

/**
 *
 * @author zhuerkan
 */
public class XmlBasedMapping implements Mapping {
    
    private String namespaceUri;
    
    private NsContext rootNamespaceContext;
    
    private Map<String, Entity> entities;
    
    private Map<String, AttributeOld> attributes;
    
    private Map<String, RelationOld> relations;
    
    public XmlBasedMapping() {
        
    }

    @Override
    public void addEntity(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity getEntity(String typeUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addRelation(RelationOld relation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RelationOld getRelation(String typeUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addAttribute(String entityTypeUri, AttributeOld attribute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AttributeOld getAttribute(String typeUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static final String entityTagName = "entity";
    public static final String attributeTagName = "property";
    public static final String relationTagName = "relation";
    public static final String keyAttrName = "key";
    public static final String nameAttrName = "name";
    public static final String typeAttrName = "type";
    public static final String pathAttrName = "path";
}
