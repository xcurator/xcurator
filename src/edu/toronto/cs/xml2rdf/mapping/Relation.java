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

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class Relation {

  static boolean debug = true;
  
  private String name;
  private Entity entity;
  private String targetEntity;
  private ForeignLookupKey foreignLookupKey;
  private String path;
  private Element element;

  public Relation(String name, Entity entity, String targetEntity,
      ForeignLookupKey foreignLookupKey, String path,
      Element element) {
    this.name = name;
    this.entity = entity;
    this.targetEntity = targetEntity;
    this.foreignLookupKey = foreignLookupKey;
    this.path = path;
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

  public ForeignLookupKey getForeignLookupKey() {
    return foreignLookupKey;
  }

  public void setForeignLookupKey(ForeignLookupKey foreignLookupKey) {
    this.foreignLookupKey = foreignLookupKey;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
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

  public void createRDFRelation(Model model, Resource instanceResource, Element item, Document dataDoc, String typePrefix) 
  throws XPathExpressionException {

    NodeList relationNodeList = XMLUtils.getNodesByPath(path, item, dataDoc);
    for (int i = 0; i < relationNodeList.getLength(); i++) {
      Element relationElement = (Element) relationNodeList.item(i);
      findAndAddLinkedResouces(model, relationElement, dataDoc, instanceResource, typePrefix);
    }
  }

  private void findAndAddLinkedResouces(Model model,
      Element item, Document dataDoc, Resource parentResouce, String typePrefix) throws XPathExpressionException {

    //TODO: Fix it!
    //    createSPARQL();

    // name="x.y.z.y"
    // x

    // ?x y ?y .
    // ?y z ?z .
    // ?z y ?y .

    String whereClause = "WHERE {\n";
    int j = 0;
    whereClause += "?x0 rdf:type <" + getTargetEntity() + "> . \n";

    for (Property lookupProperty: foreignLookupKey.getProperties()) {
      String localValue = XMLUtils.getStringByPath(lookupProperty.getPath(), item, dataDoc).trim();

      if (localValue.length() == 0) {
        continue;
      }
      //      localValue = localValue.replaceAll("\\s+", "\\\\\\\\s+");
      localValue = JenaUtils.querify(localValue);

      String[] splittedRelation = lookupProperty.getElement().getAttribute("name")
      .replace(typePrefix, "")
      .split("\\.");

      int i;
      for (i = 1; i < splittedRelation.length; i++) {
        whereClause += "?x" + (i - 1) + "" + (i != 1 ? j : "") + " t:" + splittedRelation[i] + " ?x" + (i ) + "" +  j + ".\n";
      }

      whereClause += "FILTER (?x" + (i - 1) + (i != 1 ? j : "") + " = \"" + localValue + "\").\n";

      j++;
    }

    whereClause += "} ";

    String queryStr = 
      "PREFIX t: <" + typePrefix + ">\n" +
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
      "select ?x0 \n" + whereClause;

    LogUtils.debug(this.getClass(), queryStr);

    Query query = QueryFactory.create(queryStr);
    QueryExecution qExec = null;
    try{
      qExec = QueryExecutionFactory.create(query, model);
      ResultSet rs = qExec.execSelect();
      while (rs.hasNext()) {
        QuerySolution solution = rs.next();
        RDFNode subject = solution.get("?x0");
        parentResouce.addProperty(getJenaProperty(model), subject);

      }
    }catch(Exception e){
      if (debug)
        e.printStackTrace();
    }finally{
      if (qExec != null) {
        qExec.close();
      }
    }

    //    for (Property lookupProperty: foreignLookupKey.getProperties()) {
    //      String localValue = XMLUtils.getStringByPath(lookupProperty.getPath(), item, dataDoc);
    //      Selector selector = new SimpleSelector(null, lookupProperty.getJenaProperty(model), localValue);
    //      StmtIterator iter = model.listStatements(selector);
    //
    //      while (iter.hasNext()) {
    //        Statement stmt = iter.next();
    //        if (targetEntity.equals(stmt.getSubject().getProperty(RDF.type).getObject().toString())) {
    //          parentResouce.addProperty(getJenaProperty(model), stmt.getSubject());
    //        }
    //      }
    //    }
    //
    //
    //    for (Property lookupProperty: foreignLookupKey.getProperties()) {
    //      String localValue = XMLUtils.getStringByPath(lookupProperty.getPath(), item, dataDoc);
    //      Selector selector = new SimpleSelector(null, lookupProperty.getJenaProperty(model), localValue);
    //      StmtIterator iter = model.listStatements(selector);
    //
    //      while (iter.hasNext()) {
    //        Statement stmt = iter.next();
    //        if (targetEntity.equals(stmt.getSubject().getProperty(RDF.type).getObject().toString())) {
    //          parentResouce.addProperty(getJenaProperty(model), stmt.getSubject());
    //        }
    //      }
    //    }

  }

  public String getSPARQLEqualPhrase(String parentVarName, String typePrefix, Model model, Element item, Document dataDoc) throws XPathExpressionException {
    String clause = "";
    NodeList relationNodeList = XMLUtils.getNodesByPath(path, item, dataDoc);
    for (int i = 0; i < relationNodeList.getLength(); i++) {
      Element relationElement = (Element) relationNodeList.item(i);
      String relVarName = JenaUtils.getNextSparqlVarName();

      clause += parentVarName + " <" + this.getName() + "> " + relVarName + " . \n";

      for (Property lookupProperty: foreignLookupKey.getProperties()) {
        String localValue = XMLUtils.getStringByPath(lookupProperty.getPath(), relationElement, dataDoc).trim();

        if (localValue.length() == 0) {
          continue;
        }

        //        localValue = localValue.replaceAll("\\s+", "\\\\\\\\s+");
        localValue = JenaUtils.querify(localValue);

        String[] splittedRelation = lookupProperty.getElement().getAttribute("name")
        .replace(typePrefix, "")
        .split("\\.");

        String currentVarName = relVarName;
        String nextVarName = JenaUtils.getNextSparqlVarName();
        int k;
        for (k = 1; k < splittedRelation.length; k++) {
          clause += currentVarName + " t:" + splittedRelation[k] + " " + nextVarName + ".\n";
          currentVarName = nextVarName;
          nextVarName = JenaUtils.getNextSparqlVarName();
        }

        clause += "FILTER ("+ currentVarName + " = \"" + localValue + "\") . \n";
      }
    }

    return clause;
  }

}
