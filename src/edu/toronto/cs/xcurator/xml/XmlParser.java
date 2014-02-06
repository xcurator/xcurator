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
package edu.toronto.cs.xcurator.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlParser {
    
    private final boolean debug;
    
    public XmlParser() {
        this.debug = false;
    }
    
    public XmlParser(boolean debug) {
        this.debug = debug;
    }

    public Document parse(String path, int maxElement) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
        Document doc = builder.parse(path);
        doc = pruneDocument(doc, maxElement);
        return doc;
    }

    public Document parse(InputStream is, int maxElement) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
        Document doc = builder.parse(is);
        doc = pruneDocument(doc, maxElement);
        return doc;
    }

    public Document parse(Reader reader, int maxElement) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = XMLUtils.createNsAwareDocumentBuilder();
        Document doc = builder.parse(new InputSource(reader));
        doc = pruneDocument(doc, maxElement);
        return doc;
    }

    private Document pruneDocument(Document doc, int maxElement) throws ParserConfigurationException {
        if (maxElement == -1) {
            return doc;
        }
        
        Document newDoc = (Document) doc.cloneNode(false);
        Element newRoot = (Element) doc.getDocumentElement().cloneNode(false);
        newDoc.adoptNode(newRoot);
        newDoc.appendChild(newRoot);

        NodeList nl = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < maxElement && i < nl.getLength(); i++) {
            if (!(nl.item(i) instanceof Element)) {
                maxElement++;
                continue;
            }

            Node item = nl.item(i).cloneNode(true);
            newDoc.adoptNode(item);
            newDoc.getDocumentElement().appendChild(item);
        }

        if (debug) {
            System.out.println("Creating document of " + newDoc.getDocumentElement().getChildNodes().getLength());
        }
        return newDoc;
    }
    
    public String getUriFromPrefixedName(String prefixedName, NsContext nsContext) {
      // Apply the split only 1 time to get the first prefix
      // There may be more prefix in the name but we choose to ignore them
      // for now. Need to change the way it was serialized first.
      String[] segs = prefixedName.split(":", 2);
      assert(segs.length == 2); // This is temporary.
      // We need more elaborate way of parsing to make sure the result is 
      // correct.
      String baseUri = nsContext.getNamespaceURI(segs[0]);
      return baseUri + segs[1];
    }
}
