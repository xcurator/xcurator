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
package edu.toronto.cs.xml2rdf.mapping;

import java.util.List;

import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;

public class ForeignLookupKey {

	private String name;
	private Entity entity;
	private String targetEntity;
	private List<Property> properties;
	private Element element;

	public ForeignLookupKey(String name, Entity entity, String targetEntity,
			List<Property> properties, Element element) {
		this.name = name;
		this.entity = entity;
		this.targetEntity = targetEntity;
		this.properties = properties;
		this.element = element;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public String getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(String targetEntity) {
		this.targetEntity = targetEntity;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public com.hp.hpl.jena.rdf.model.Property getJenaProperty(Model model) {
		return model.createProperty(getName());
	}

}
