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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.model.Attribute;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.model.Relation;
import edu.toronto.cs.xcurator.xml.ElementIdGenerator;
import edu.toronto.cs.xcurator.xml.XPathFinder;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class RdfGeneration implements RdfGenerationStep {

  private String tdbDirPath;
  private InputStream xmlDataStream;
  private XmlParser parser;
  private XPathFinder xpath;
  private ElementIdGenerator elementIdGenerator;

  public RdfGeneration(String tdbDirPath, InputStream xmlDataStream, XmlParser parser,
          XPathFinder xpath, ElementIdGenerator elementIdGenerator) {
    this.tdbDirPath = tdbDirPath;
    this.xmlDataStream = xmlDataStream;
    this.parser = parser;
    this.xpath = xpath;
    this.elementIdGenerator = elementIdGenerator;
  }

  @Override
  public void process(Mapping mapping) {
    try {
      // Check if the mapping passed in is initialized
      if (!mapping.isInitialized()) {
        throw new Exception("Mapping was not initialized, missing preprocessing or deserializing?");
      }

      // Get data document
      Document dataDoc = parser.parse(xmlDataStream, -1);

      // Create Jena model
      Model model = TDBFactory.createModel(tdbDirPath);

      Iterator<Entity> it = mapping.getEntityIterator();
      while (it.hasNext()) {
        Entity entity = it.next();
        NodeList nl = xpath.getNodesByPath(entity.getPath(), null, dataDoc,
                entity.getNamespaceContext());
        for (int i = 0; i < nl.getLength(); i++) {
          // Create RDFs
          // The URI of the subject should be the XBRL link + UUID
          // But a resolvable link should be used in the future
          Element dataElement = (Element) nl.item(i);
          generateRdfs(entity, mapping, dataElement, dataDoc, model);
          
        }

      }
      // Finish writing to the TDB
      model.commit();
      model.close();
    } catch (SAXException ex) {
      Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParserConfigurationException ex) {
      Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  
  private Resource generateRdfs(Entity entity, Mapping mapping, Element dataElement, 
          Document dataDoc, Model model) 
          throws XPathExpressionException, IOException, NoSuchAlgorithmException {
    // Maybe we should check duplicate here?
    
    // Generate a unique ID for this instance
    String instanceUri = elementIdGenerator.generateId(entity.getInstanceIdPattern(),
            entity.getNamespaceContext(), dataElement, dataDoc, xpath);
    
    // Return the resource if it has already been created
    // Preventing the related resources to be recursively created
    Resource instanceResource = model.getResource(instanceUri);
    if (instanceResource != null) {
      return instanceResource;
    }
    
    // Create RDF resources
    Resource typeResource = model.createResource(entity.getTypeUri());
    instanceResource = model.createResource(instanceUri);
    
    // Add type to instance
    instanceResource.addProperty(RDF.type, typeResource);
    
    // Add attribute properties of this instance
    Iterator<Attribute> attrIterator = entity.getAttributeIterator();
    while (attrIterator.hasNext()) {
      Attribute attr = attrIterator.next();
      Property attrProperty = model.createProperty(attr.getTypeUri());
      NodeList nl = xpath.getNodesByPath(attr.getPath(), dataElement, dataDoc, 
              entity.getNamespaceContext());
      for (int i = 0; i < nl.getLength(); i++) {
        String value = nl.item(i).getTextContent();
        instanceResource.addProperty(attrProperty, value);
      }
    }
    
    // Add relation properties of this instance
    Iterator<Relation> relIterator = entity.getRelationIterator();
    while (relIterator.hasNext()) {
      Relation rel = relIterator.next();
      Property relProperty = model.createProperty(rel.getTypeUri());
      NodeList nl = xpath.getNodesByPath(rel.getPath(), dataElement, dataDoc,
              entity.getNamespaceContext());
      for (int i = 0; i < nl.getLength(); i++) {
        Element targetElement = (Element) nl.item(i);
        Entity targetEntity = mapping.getEntity(rel.getTargetEntityUri());
        // Recursively create the target RDFs
        Resource targetResource = generateRdfs(targetEntity, mapping, targetElement, dataDoc, model);
        instanceResource.addProperty(relProperty, targetResource);
      }
    }
    
    return instanceResource;
  }

//  private Object getSameResource(Model model, String typePrefix,
//      Element item, Document dataDoc) throws XPathExpressionException {
//    QueryExecution qExec = null;
//    try{
//      String query = getEqualsQuery(model, typePrefix, item, dataDoc);
//      LogUtils.debug(this.getClass(), query);
//      qExec = QueryExecutionFactory.create(query, model);
//      ResultSet rs = qExec.execSelect();
//      while (rs.hasNext()) {
//        QuerySolution solution = rs.next();
//        return solution.get("?x0");
//      }
//    } catch(Exception e){
//      if (debug)
//        e.printStackTrace();
//    } finally {
//      if (qExec != null) {
//        qExec.close();
//      }
//    }
//    return null;
//  }
//  
//  public String getEqualsQuery(Model model, String typePrefix, Element item, Document dataDoc) throws XPathExpressionException {
//
//    String whereClause = "WHERE {\n";
//
//    whereClause += "?x0 rdf:type <" + type + "> . \n";
//    boolean hasKey = false;
//    for (Property property : getProperties()) {
//      if (property.isKey()) {
//        hasKey = true;
//      }
//    }
//    for (Property property : getProperties()) {
//      if (property.isKey() || !hasKey) {
//        whereClause += property.getSPARQLEqualPhrase("?x0", item, dataDoc);
//      }
//    }
//
//    if (!hasKey) {
//      for (Relation rel: getRelations()) {
//        whereClause += rel.getSPARQLEqualPhrase("?x0", typePrefix, model, item, dataDoc);
//      }
//    }
//
//    whereClause += "}\n";
//
//    String prefixes = "PREFIX t: <" + typePrefix + "> \n" +
//    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"; 
//    String select = "select ?x0 ";
//
//    return prefixes + select + whereClause;
//  }
}
