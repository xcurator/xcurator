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

public class RdfUriBuilder {
  
  private final String typeUriBase;
  private final String propertyUriBase;
  
  private final String typePrefix;
  private final String propertyPrefix;

  public RdfUriBuilder(RdfUriConfig config) {
    this.typeUriBase = config.getTypeResourceUriBase();
    this.propertyUriBase = config.getPropertyResourceUriBase();
    this.typePrefix = config.getTypeResourcePrefix();
    this.propertyPrefix = config.getPropertyResourcePrefix();
  }
  
  private String getUri(Node node, String uriBase) {
    return uriBase + "/" + node.getLocalName();
  }
  
  public String getRdfTypeUri(Element element) {
    return getUri(element, typeUriBase);
  }
  
  public String getRdfPropertyUri(Node node) {
    return getUri(node, propertyUriBase);
  }
  
  public String getRdfPropertyUriForValue(Element element) {
    return propertyUriBase + "/value";
  }
  
  public String getRdfRelationPropertyUri(Element subject, Element object) {
    return getUri(object, propertyUriBase);
  }

}
