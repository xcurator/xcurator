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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UriBuilder {
  
  private final String typeUriBase;
  private final String propertyUriBase;
  
  private final String typePrefix;
  private final String propertyPrefix;

  public UriBuilder(String typeUriBase, String propertyUriBase,
          String typePrefix, String propertyPrefix) {
    this.typeUriBase = typeUriBase.endsWith("/") ? 
            typeUriBase.substring(0, typeUriBase.lastIndexOf("/")) : typeUriBase;
    this.propertyUriBase = propertyUriBase.endsWith("/") ? 
            propertyUriBase.substring(0, propertyUriBase.lastIndexOf("/")) : propertyUriBase;
    this.typePrefix = typePrefix;
    this.propertyPrefix = propertyPrefix;
  }
   
  /**
   * Set the user defined namespaces to a namespace context
   * This can be useful when the root namespace context carries all namespace
   * definitions in the document.
   * @param nsContext 
   */
  public void setNamespace(NsContext nsContext) {
    nsContext.addNamespace(typePrefix, typeUriBase);
    nsContext.addNamespace(propertyPrefix, propertyUriBase);
  }
  
  private String getUri(Node node, NsContext nsContext, String uriBase, String prefix) {
    if (!nsContext.hasNamespace(prefix, uriBase)) {
      nsContext.addNamespace(prefix, uriBase);
    }
    return uriBase + "/" + node.getLocalName();
  }
  
  public String getRdfTypeUri(Element element, NsContext nsContext) {
    return getUri(element, nsContext, typeUriBase, typePrefix);
  }
  
  public String getRdfPropertyUri(Node node, Element parent, NsContext nsContext) {
    return getUri(node, nsContext, propertyUriBase, propertyPrefix);
  }
  
  public String getRdfPropertyUriForValue(Element element, NsContext nsContext) {
    if (!nsContext.hasNamespace(propertyPrefix, propertyUriBase)) {
      nsContext.addNamespace(propertyPrefix, propertyUriBase);
    }
    return propertyUriBase + "/value";
  }
  
  public String getRdfRelationPropertyUri(Element subject, Element object, NsContext nsContext) {
    return getUri(object, nsContext, propertyUriBase, propertyPrefix);
  }
  
  public String getXmlTypeUri(Node node) {
    String nsuri = node.getNamespaceURI();
    if (nsuri == null) {
      return node.getLocalName();
    }
    if (nsuri.endsWith("/") || nsuri.endsWith("#")) {
      nsuri = nsuri.substring(0, nsuri.length()-1);
    }
    return nsuri + "/" + node.getLocalName();
  }

}
