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
import com.hp.hpl.jena.tdb.TDBFactory;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.model.Entity;
import edu.toronto.cs.xcurator.xml.XmlXPathFinder;
import edu.toronto.cs.xcurator.xml.XmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
public class RdfGeneration implements RdfGenerationStep {

  private String tdbDirPath;
  private InputStream xmlDataStream;
  private XmlParser parser;
  private XmlXPathFinder xpath;

  public RdfGeneration(String tdbDirPath, InputStream xmlDataStream, XmlParser parser, XmlXPathFinder xpath) {
    this.tdbDirPath = tdbDirPath;
    this.xmlDataStream = xmlDataStream;
    this.parser = parser;
    this.xpath = xpath;
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

}
