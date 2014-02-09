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
package edu.toronto.cs.xcurator.discoverer;

import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.xml.NsContext;
import edu.toronto.cs.xcurator.xml.XmlDocumentSerializer;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author zhuerkan
 */
public class SerializeMapping implements MappingDiscoveryStep {

  private final OutputStream output;
  private final XmlDocumentSerializer serializer;
  private final Transformer transformer;
  
  public SerializeMapping(XmlDocumentSerializer serializer, OutputStream output,
          Transformer transformer) {
    this.output = output;
    this.serializer = serializer;
    this.transformer = transformer;
  }
  
  @Override
  public void process(Document dataDoc, Mapping mapping) {
    try {
      if (!(mapping instanceof XmlBasedMapping) ||
              !mapping.isInitialized()) {
        return;
      }
      XmlBasedMapping xmlMap = (XmlBasedMapping) mapping;
      Document mapDoc = serializer.createDocument();
      Element root = serializer.addRootElement(mapDoc, xmlMap.getMappingNamespaceUri(), 
              xmlMap.getMappingNodeName());
      serializer.addNsContextToEntityElement(root, xmlMap.getBaseNamespaceContext());
      
      Iterator<Entity> entityIterator = xmlMap.getEntityIterator();
      while (entityIterator.hasNext()) {
        Entity entity = entityIterator.next();
        serializeEntity(entity, mapDoc, root, xmlMap);
      }
      
      // Write to output stream
      transformer.transform(new DOMSource(mapDoc), new StreamResult(output));
    } catch (ParserConfigurationException ex) {
      Logger.getLogger(SerializeMapping.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TransformerConfigurationException ex) {
      Logger.getLogger(SerializeMapping.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TransformerException ex) {
      Logger.getLogger(SerializeMapping.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private void serializeEntity(Entity entity, Document doc, Element root, 
          XmlBasedMapping mapping) {
    
    String mappingNsUri = mapping.getMappingNamespaceUri();
    NsContext nsContext = entity.getNamespaceContext();
    
    // Create a new entity element in the mapping document
    Element entityElement = doc.createElementNS(mappingNsUri, 
            mapping.getEntityNodeName());
    entityElement.setAttribute(XmlBasedMapping.pathAttrName, entity.getPath());
    serializer.addUriBasedAttrToElement(XmlBasedMapping.typeAttrName, 
            entity.getTypeUri(), nsContext, entityElement);
    serializer.addNsContextToEntityElement(entityElement, nsContext);
    root.appendChild(entityElement);
    
    // Create ID child element
    Element idElement = doc.createElementNS(mappingNsUri, mapping.getIdNodeName());
    idElement.setTextContent(entity.getInstanceIdPattern());
    entityElement.appendChild(idElement);
    
    // Create attribute children
    Iterator<Attribute> attrIterator = entity.getAttributeIterator();
    while (attrIterator.hasNext()) {
      Attribute attribute = attrIterator.next();
      
      Element attrElement = doc.createElementNS(mappingNsUri,
              mapping.getAttributeNodeName());
      attrElement.setAttribute(XmlBasedMapping.pathAttrName, attribute.getPath());
      serializer.addUriBasedAttrToElement(XmlBasedMapping.nameAttrName,
              attribute.getTypeUri(), nsContext, attrElement);
      entityElement.appendChild(attrElement);
    }
    
    // Create relation children
    Iterator<Relation> relIterator = entity.getRelationIterator();
    while (relIterator.hasNext()) {
      Relation relation = relIterator.next();
      
      Element relElement = doc.createElementNS(mappingNsUri, mapping.getRelationNodeName());
      relElement.setAttribute(XmlBasedMapping.pathAttrName, relation.getPath());
      serializer.addUriBasedAttrToElement(XmlBasedMapping.nameAttrName, relation.getTypeUri(), nsContext, relElement);
      serializer.addUriBasedAttrToElement(XmlBasedMapping.targetEntityAttrName,
              relation.getTargetEntityUri(), nsContext, relElement);
      entityElement.appendChild(relElement);
    }
  }
}
