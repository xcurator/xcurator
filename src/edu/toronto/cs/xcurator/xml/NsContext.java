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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author ekzhu
 */
public class NsContext implements NamespaceContext {

  private final Map<String, String> prefixMap;

  public NsContext(Element element) {
    prefixMap = new HashMap<>();
    // Currently we only looking for namespace 
    // definitions in root element's attributes
    NamedNodeMap attributeMap = element.getAttributes();
    for (int i = 0; i < attributeMap.getLength(); i++) {
      org.w3c.dom.Attr attr =  (Attr) attributeMap.item(i);
      if (attr.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
        // This is the default namespace
        prefixMap.put(XMLConstants.DEFAULT_NS_PREFIX, attr.getValue());
      }
      else if (attr.getPrefix().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
        // This is a regular namespace definition
        prefixMap.put(attr.getLocalName(), attr.getValue());
      }
    }
    if (!prefixMap.containsKey(XMLConstants.DEFAULT_NS_PREFIX)) {
      // Add the default namespace URI if not added
      prefixMap.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
    }
    // Add other standard mappings
    prefixMap.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    prefixMap.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
  }

  @Override
  public String getNamespaceURI(String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException();
    }
    return prefixMap.get(prefix);
  }

  @Override
  public String getPrefix(String namespaceURI) {
    for (String key : prefixMap.keySet()) {
      if (prefixMap.get(key).equals(namespaceURI)) {
        return key;
      }
    }
    return null;
  }

  @Override
  public Iterator getPrefixes(final String namespaceURI) {
    return new Iterator() {

      private Object[] namespaceMap = prefixMap.entrySet().toArray();
      private int i = 0;
      private int size = prefixMap.size();
      
      @Override
      public boolean hasNext() {
        while (i < size) {
          if (((Map.Entry) namespaceMap[i]).getValue().equals(namespaceURI)) {
            return true;
          }
          i++;
        }
        return false;
      }

      @Override
      public Object next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        i++;
        return ((Map.Entry)namespaceMap[i-1]).getKey();
      }

      @Override
      public void remove() {
        
      }
      
    };
  }

}
