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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UriBuilder {

  private String defaultNamespaceUri;
  private String defaultPrefix;

  public UriBuilder(String defaultNamespaceUri, String defaultPrefix) {
    this.defaultNamespaceUri = defaultNamespaceUri;
    this.defaultPrefix = defaultPrefix;
  }

  public void setDefaultNamespace(String namespaceUri, String prefix) {
    this.defaultNamespaceUri = namespaceUri;
    this.defaultPrefix = prefix;
  }
  
  /**
   * Get the URI of the node, if the node does not have a namespace, use the
   * default namespace setting and update the namespace context.
   * @param node
   * @param defaultBaseUri
   * @param defaultPrefix
   * @param nsContext
   * @return 
   */
  private String getNodeUriOrUseDefault(Node node, NsContext nsContext) {
    String baseUri = node.getNamespaceURI();
    if (baseUri == null) {
      baseUri = defaultNamespaceUri;
      nsContext.addNamespace(defaultPrefix, baseUri);
    }
    return baseUri + "#" + node.getLocalName();
  }
  
  public String getElementUri(Element element, NsContext nsContext) {
    return getNodeUriOrUseDefault(element, nsContext);
  }
  
  public String getAttributeUri(Attr attr, Element parent, NsContext nsContext) {
    return attr.getNamespaceURI() != null ? 
            getNodeUriOrUseDefault(attr, nsContext) : 
            getNodeUriOrUseDefault(parent, nsContext) + "." + attr.getNodeName();
  }
  
  public String getLeafElementUri(Element leaf, Element parent, NsContext nsContext) {
    return leaf.getNamespaceURI() != null ? 
            getNodeUriOrUseDefault(leaf, nsContext) : 
            getNodeUriOrUseDefault(parent, nsContext) + "." + leaf.getNodeName();
  }
  
  public String getValueAttributeUri(Element element, NsContext nsContext) {
    return getElementUri(element, nsContext) + "." + "value";
  }
  
  public String getRelationUri(Element subject, Element object, NsContext nsContext) {
    String objectName = object.getNodeName();
    if (object.getNamespaceURI() == null) {
      objectName = defaultPrefix + ":" + objectName;
    }
    return getElementUri(subject, nsContext) + "." + objectName;
  }

}
