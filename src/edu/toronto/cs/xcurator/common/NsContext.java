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
package edu.toronto.cs.xcurator.common;

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
public final class NsContext implements NamespaceContext {

    private final Map<String, String> prefixMap;
    private static final Map<String, String> defaultMap = new HashMap<>();

    static {
        defaultMap.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        defaultMap.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        defaultMap.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
    }

    ;

  public NsContext() {
        prefixMap = new HashMap<>();
    }

    public NsContext(Element element) {
        this();
        discover(element);
    }

    public NsContext(NsContext nsContext) {
        this.prefixMap = new HashMap<>(nsContext.getNamespaces());
    }

    private NsContext(Map<String, String> prefixMap) {
        this.prefixMap = new HashMap<>(prefixMap);
    }

    public void discover(Element element) {
        discover(element, true);
    }

    public void discover(Element element, boolean override) {
        // Currently we only looking for namespace 
        // definitions in root element's attributes
        NamedNodeMap attributeMap = element.getAttributes();
        for (int i = 0; i < attributeMap.getLength(); i++) {
            org.w3c.dom.Attr attr = (Attr) attributeMap.item(i);
            String prefix = attr.getPrefix();
            if (attr.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                // This is the default namespace
                addNamespace(XMLConstants.DEFAULT_NS_PREFIX, attr.getValue(), override);
            } else if (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                // This is a regular namespace definition
                addNamespace(attr.getLocalName(), attr.getValue(), override);
            }
        }
    }

    /**
     * Add a prefix to namespace URI mapping to the context; if override is
     * false, there is no guarantee the given prefix name will be used in the
     * context. When a prefix with the same name already exist in our context:
     * if override is true, then it will replace the existing prefix to URI
     * mapping with the new one; if override is false, then a new prefix name
     * will be chosen and added to the context.
     *
     * @param prefix
     * @param namespaceURI
     * @param override
     */
    public void addNamespace(String prefix, String namespaceURI, boolean override) {
        // If we choose to override the existing prefix definition, or if the prefix
        // definition does not already exist, we can go ahead and put the new prefix
        // definition in our map.
        if (override || !prefixMap.containsKey(prefix)) {
            prefixMap.put(prefix, namespaceURI);
            return;
        }
        // If the existing prefix definition is the same as the new one, we do nothing.
        if (prefixMap.get(prefix).equals(namespaceURI)) {
            return;
        }
        // If we choose to not override and a different prefix definition already exist,
        // we need to find a new prefix for it.
        int count = 1;
        String altPrefixName = prefix + "_" + Integer.toString(count);
        while (prefixMap.containsKey(altPrefixName)) {
            count++;
            altPrefixName = prefix + "_" + Integer.toString(count);
        }
        prefixMap.put(altPrefixName, namespaceURI);
    }

    public void addNamespace(String prefix, String namespaceURI) {
        addNamespace(prefix, namespaceURI, true);
    }

    public NsContext merge(NsContext other) {
        return merge(other, true);
    }

    /**
     * Merge other namespace with this one, allowing cascading calls.
     *
     * @param other
     * @param override
     * @return
     */
    public NsContext merge(NsContext other, boolean override) {
        Map<String, String> otherPrefixMap = other.getNamespaces();
        for (Map.Entry<String, String> entry : otherPrefixMap.entrySet()) {
            addNamespace(entry.getKey(), entry.getValue(), override);
        }
        return this;
    }

    public boolean hasNamespace(String prefix, String namespaceUri) {
        return prefixMap.containsKey(prefix)
                && prefixMap.get(prefix).equals(namespaceUri);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        if (prefixMap.containsKey(prefix)) {
            return prefixMap.get(prefix);
        }
        return defaultMap.get(prefix);
    }

    public Map<String, String> getNamespaces() {
        return new HashMap<>(prefixMap);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (String key : prefixMap.keySet()) {
            if (prefixMap.get(key).equals(namespaceURI)) {
                return key;
            }
        }
        for (String key : defaultMap.keySet()) {
            if (defaultMap.get(key).equals(namespaceURI)) {
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
                return ((Map.Entry) namespaceMap[i - 1]).getKey();
            }

            @Override
            public void remove() {

            }

        };
    }

}
