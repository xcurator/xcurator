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

import edu.toronto.cs.xcurator.common.ElementIdGenerator;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.Attribute;
import edu.toronto.cs.xcurator.mapping.Entity;
import edu.toronto.cs.xcurator.mapping.Reference;
import edu.toronto.cs.xcurator.mapping.Relation;
import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.XPathFinder;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zhuerkan
 */
public class RdfGeneration implements RdfGenerationStep {

  private final String tdbDirPath;
  private final XPathFinder xpath;
  private final ElementIdGenerator elementIdGenerator;

  public RdfGeneration(String tdbDirPath, XPathFinder xpath,
          ElementIdGenerator elementIdGenerator) {
    this.tdbDirPath = tdbDirPath;
    this.xpath = xpath;
    this.elementIdGenerator = elementIdGenerator;
  }

  @Override
  public void process(List<DataDocument> xmlDocuments, Mapping mapping) {
    for (DataDocument dataDoc : xmlDocuments) {
      try {
        // Check if the mapping passed in is initialized
        if (!mapping.isInitialized()) {
          throw new Exception("Mapping was not initialized, missing preprocessing or deserializing?");
        }

        // Create Jena model
        Model model = TDBFactory.createModel(tdbDirPath);

        Iterator<Entity> it = mapping.getEntityIterator();
        while (it.hasNext()) {
          Entity entity = it.next();
          NodeList nl = xpath.getNodesByPath(entity.getPath(), dataDoc.Data,
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
      } catch (XPathExpressionException ex) {
        Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
      } catch (NoSuchAlgorithmException ex) {
        Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
      } catch (Exception ex) {
        Logger.getLogger(RdfGeneration.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private Resource generateRdfs(Entity entity, Mapping mapping, Element dataElement,
          DataDocument dataDoc, Model model)
          throws XPathExpressionException, IOException, NoSuchAlgorithmException {

    // Generate a unique ID for this instance
    String instanceUri = elementIdGenerator.generateUri(dataDoc.resourceUriPattern,
            entity.getNamespaceContext(), dataElement, dataDoc.Data, xpath);

    // Create RDF resources
    Resource typeResource = model.createResource(entity.getTypeUri());
    Resource instanceResource = model.createResource(instanceUri);

    // Return the resource if it has already been created
    // Preventing the relation instance resources to be recreated
    if (model.contains(instanceResource, RDF.type, typeResource)) {
      return instanceResource;
    }

    // Add type to instance
    instanceResource.addProperty(RDF.type, typeResource);

    // Add attribute properties of this instance
    Iterator<Attribute> attrIterator = entity.getAttributeIterator();
    while (attrIterator.hasNext()) {
      Attribute attr = attrIterator.next();
      Property attrProperty = model.createProperty(attr.getTypeUri());
      NodeList nl = xpath.getNodesByPath(attr.getPath(), dataElement,
              entity.getNamespaceContext());
      for (int i = 0; i < nl.getLength(); i++) {
        String value = nl.item(i).getTextContent().trim();
        instanceResource.addProperty(attrProperty, value);
      }
    }

    // Add relation properties of this instance
    Iterator<Relation> relIterator = entity.getRelationIterator();
    while (relIterator.hasNext()) {
      Relation rel = relIterator.next();
      // Get potential instances of target entity of this relation
      NodeList nl = xpath.getNodesByPath(rel.getPath(), dataElement, dataDoc.Data,
              entity.getNamespaceContext());
      // Create a cache map for saving values of the references
      Map<String, String> cache = new HashMap<>();
      for (int i = 0; i < nl.getLength(); i++) {
        Element targetElement = (Element) nl.item(i);
        Entity targetEntity = mapping.getEntity(rel.getTargetEntityUri());
        Iterator<Reference> refIterator = rel.getReferenceIterator();
        // Filter the ones that do not meet the reference
        // Match is automatically true when there is no reference
        boolean match = true; 
        while (refIterator.hasNext()) {
          if (!referenceMatch(dataElement, targetElement, refIterator.next(),
                  entity.getNamespaceContext(), targetEntity.getNamespaceContext(), cache)) {
            // Stop checking when seeing on mis-match
            match = false;
            break;
          }
        }
        if (!match) {
          continue;
        }
        // Recursively create the target resources
        Resource targetResource = generateRdfs(targetEntity, mapping, targetElement, dataDoc, model);
        // Build the relation
        Property relProperty = model.createProperty(rel.getTypeUri());
        instanceResource.addProperty(relProperty, targetResource);
      }
    }

    return instanceResource;
  }

  private boolean referenceMatch(Element subjecElement, Element objectElement, 
          Reference reference, NsContext subjectNsContext, NsContext objectNsContext,
          Map<String, String> cache) throws XPathExpressionException {
    String subjectRefValue;
    String path = reference.getPath();
    if (cache.containsKey(path)) {
      subjectRefValue = cache.get(path);
    } else {
      subjectRefValue = xpath.getStringByPath(reference.getPath(), subjecElement, subjectNsContext);
      cache.put(path, subjectRefValue);
    }
    String objectRefValue = xpath.getStringByPath(reference.getTargetPath(), objectElement, objectNsContext);
    return subjectRefValue.equals(objectRefValue);
  }
}
