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

import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class XmlBasedMappingDeserialization implements RdfGenerationStep {

    private final String mappingFilePath;
    private final XmlParser parser;
    
    public XmlBasedMappingDeserialization(String mappingFilePath, XmlParser parser) {
        this.mappingFilePath = mappingFilePath;
        this.parser = parser;
    }
    
    @Override
    public void process(Mapping mapping) {
        try {
            if (!(mapping instanceof XmlBasedMapping)) {
                throw new IllegalArgumentException("The mapping needs to be XML based.");
            }
            Document mapDoc = parser.parse(mappingFilePath, -1);
            Element root = mapDoc.getDocumentElement();
            String namespaceUri = root.getNamespaceURI();
            NodeList nl = mapDoc.getElementsByTagNameNS(namespaceUri, Entity.tagName);
            for (int i=0; i < nl.getLength(); i++) {
                Element entityElement = (Element) nl.item(i);
                Entity entity = createEntity(entityElement);
                mapping.addEntity(entity);
            }
            
            
        } catch (SAXException ex) {
            Logger.getLogger(XmlBasedMappingDeserialization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlBasedMappingDeserialization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlBasedMappingDeserialization.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Entity createEntity(Element entityElement) {
        String type = entityElement.getAttribute(Entity.typeAttrName);
        String path = entityElement.getAttribute(Entity.pathAttrName);
        Entity entity = new Entity(type, path);
        return entity;
    }
    
//    private Attribute createAttribtue(Entity entity, Element attrElement) {
//        
//    }
    
}
