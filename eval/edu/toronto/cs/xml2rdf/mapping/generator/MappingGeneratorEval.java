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
package edu.toronto.cs.xml2rdf.mapping.generator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class MappingGeneratorEval extends TestCase{
  
  public class Accuracy{
    private double pr;
    private double re;
    
    public Accuracy(double precision, double recall){
      pr = precision;
      re = recall;
    }
    public double precision(){
      return pr;
    }
    public double recall(){
      return re;
    }
    public double fscore(double beta){
      return (1 + beta*beta) * (  (pr*re) / ( (beta*beta*pr) + re ) );
    }
  }
  
  public Set<String> getEntities(String inputfile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    Set<String> entityList = new HashSet<String>();
    Document doc = XMLUtils.parse(inputfile, -1);
    
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodeList = (NodeList) xpath.evaluate("mapping/entity", doc, XPathConstants.NODESET);
    
    for (int i = 0; i < nodeList.getLength(); i++) {
      entityList.add(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue());
    }
    
    return entityList;
  }
  
  public Accuracy evaluate (Set<String> result, Set<String> ground) {
    
    Set<String> intersection = new HashSet<String>(result);
    intersection.retainAll(ground);
    
    double pr = (double)intersection.size() / result.size();
    double re = (double)intersection.size() / ground.size();
    
    Accuracy ac = new Accuracy(pr,re);
    
    return ac;
  }
  
  
  public void testLoadMapping() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

    int[] max = new int[] { 10, 25, 50, 100, 250, 500 }; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};
    int[] phase = new int[] { 1, 2, 3, 4, 5 }; 
    
    String inputfile = "resources/mapping/linkedct.xml";
    
    Set<String> grEntityList = getEntities(inputfile);
    
    System.out.println("Entities found: " + grEntityList.size());
    for (String entity:grEntityList){
      System.out.println(entity);
    }

    for (int m: max) {
      
      System.out.println("\n\nRunning experiments for sample size: " + m + "\n");
      
      for (int p: phase) {
        inputfile = "output/output.ct." + Integer.toString(p) + "." + m + ".xml";
        Set<String> entityList = getEntities(inputfile);
        Accuracy ac = evaluate(entityList, grEntityList);
        Double x;
        //System.out.print(ac.precision() + "  ");
        //System.out.print(ac.recall() + "  ");
        //System.out.print(ac.fscore(1.0) + "  ");
        System.out.println("Precision: " + ac.precision());
        System.out.println("Recall: " + ac.recall());
        System.out.println("F1: " + ac.fscore(1.0));
      }
      
    }

  }
}
