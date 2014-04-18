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

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Reference;
import edu.toronto.cs.xcurator.mapping.Relation;
import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.RdfUriConfig;
import edu.toronto.cs.xcurator.common.XmlDocumentBuilder;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
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
  private final XmlDocumentBuilder builder;
  private final Transformer transformer;
  private final NsContext rdfNsContext;
  
  public SerializeMapping(XmlDocumentBuilder builder, OutputStream output,
          Transformer transformer, RdfUriConfig rdfUriConfig) {
    this.output = output;
    this.builder = builder;
    this.transformer = transformer;
    rdfNsContext = new NsContext();
    rdfNsContext.addNamespace(rdfUriConfig.getPropertyResourcePrefix(), 
            rdfUriConfig.getPropertyResourceUriBase());
    rdfNsContext.addNamespace(rdfUriConfig.getTypeResourcePrefix(), 
            rdfUriConfig.getTypeResourceUriBase());
  }
  
  @Override
  public void process(List<DataDocument> dataDocuments, Mapping mapping) {
    try {
      if (!(mapping instanceof XmlBasedMapping) ||
              !mapping.isInitialized()) {
        return;
      }
      XmlBasedMapping xmlMap = (XmlBasedMapping) mapping;
      Document mapDoc = builder.createDocument();
      Element root = builder.addRootElement(mapDoc, xmlMap.getMappingNamespaceUri(), 
              xmlMap.getMappingNodeName());
      builder.addNsContextToEntityElement(root, xmlMap.getBaseNamespaceContext());
      builder.addNsContextToEntityElement(root, rdfNsContext);
      
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
    builder.addUriBasedAttrToElement(XmlBasedMapping.typeAttrName, 
            entity.getRdfTypeUri(), rdfNsContext, entityElement);
    builder.addUriBasedAttrToElement(XmlBasedMapping.xmlTypeAttrName, 
            entity.getXmlTypeUri(), nsContext, entityElement);
    builder.addNsContextToEntityElement(entityElement, nsContext);
    builder.addNsContextToEntityElement(entityElement, rdfNsContext);
    root.appendChild(entityElement);
    
    // Create attribute children
    Iterator<Attribute> attrIterator = entity.getAttributeIterator();
    while (attrIterator.hasNext()) {
      Attribute attribute = attrIterator.next();
      
      Element attrElement = doc.createElementNS(mappingNsUri,
              mapping.getAttributeNodeName());
      attrElement.setAttribute(XmlBasedMapping.pathAttrName, attribute.getPath());
      builder.addUriBasedAttrToElement(XmlBasedMapping.nameAttrName,
              attribute.getRdfTypeUri(), rdfNsContext, attrElement);
      entityElement.appendChild(attrElement);
    }
    
    // Create relation children
    Iterator<Relation> relIterator = entity.getRelationIterator();
    while (relIterator.hasNext()) {
      Relation relation = relIterator.next();
      
      Element relElement = doc.createElementNS(mappingNsUri, mapping.getRelationNodeName());
      relElement.setAttribute(XmlBasedMapping.pathAttrName, relation.getPath());
      builder.addUriBasedAttrToElement(XmlBasedMapping.nameAttrName, relation.getRdfUri(), rdfNsContext, relElement);
      builder.addUriBasedAttrToElement(XmlBasedMapping.targetEntityXmlTypeAttrName,
              relation.getTargetEntityXmlTypeUri(), nsContext, relElement);
      
      // Create references of this releation
      Iterator<Reference> refIterator = relation.getReferenceIterator();
      while (refIterator.hasNext()) {
        Reference reference = refIterator.next();
        
        Element refElement = doc.createElementNS(mappingNsUri, mapping.getReferenceNodeName());
        refElement.setAttribute(XmlBasedMapping.referencePathAttrName, reference.getPath());
        refElement.setAttribute(XmlBasedMapping.referenceTargetPathAttrName, reference.getTargetPath());
        relElement.appendChild(refElement);
      }
      
      entityElement.appendChild(relElement);
    }
  }
}
