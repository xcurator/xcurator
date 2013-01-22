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
package edu.toronto.cs.xml2rdf.jena;

import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

class CaseInsensitiveTerm {
  private String term;

  public CaseInsensitiveTerm(String term) {
    this.term = term;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof String) {
      return ((String)obj).toLowerCase().contains(term);
    } else if (obj instanceof CaseInsensitiveTerm) {
      return ((CaseInsensitiveTerm)obj).term.equalsIgnoreCase(term);
    }
    return super.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}

public class JenaSimpleTest extends TestCase{
  public void testCreateRDF() {
    
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
    m.read("file:///home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/resources/opencyc/opencyc-latest.owl");
    Property label = m.createProperty( "http://sw.cyc.com/CycAnnotations_v1#label" );
    StmtIterator stiter =
        m.listStatements(new SimpleSelector(null, RDFS.label, (RDFNode) null));
    
    while(stiter.hasNext()) {
      Statement st = stiter.next();
      if (st.getObject().asLiteral().getString().equals("Subcollection Of With Relation From Type Fn geological basin Covers-Paintlike asphalt that is not an artifact")) {
        System.out.println(st);
        
        System.out.println(st.getSubject().getProperty(RDF.type));
        
        StmtIterator stiter2 =
            m.listStatements(new SimpleSelector(st.getSubject(), RDF.type, (RDFNode) null));
        while(stiter2.hasNext()) {
          System.out.println(stiter2.next());
        }
        
      }
    }
    
    // create an empty Model
    Model model = ModelFactory.createDefaultModel();

    // set a namespace prefix
    model.setNsPrefix("foaf", FOAF.NS);

    Resource Person = model.createResource( "http://www.test.org/2001/vcard-rdf2/3.0#Person" );
    // create a contributor
    model.setNsPrefix("soheil", "http://www.test.org/2001/vcard-rdf2/3.0#");
    Resource contributor2 = model.createResource("http://drthorweasel3.com");
    model.setNsPrefix("soheil", "http://www.test.org/2001/vcard-rdf2/3.0#");
    
    contributor2.addProperty(RDF.type, Person);
    contributor2.addProperty(FOAF.title, "Dr");
    contributor2.addProperty(FOAF.name, "ThorWeasel");

//      Resource contributor = model.createResource("http://drthorweasel.com");
//    contributor.addProperty(RDF.type, Person);
//    contributor.addProperty(FOAF.title, "Dr");
//    contributor.addProperty(FOAF.name, "ThorWeasel");
//    contributor.addProperty(FOAF.knows,
//                  model.createResource("http://drthorweasel3.com"));
    

    
    // write the RDF model to the console as RDF/XML
    model.write(System.out, "RDF/XML-ABBREV");
    ResIterator iter = model.listSubjects();
    while(iter.hasNext()) {
      Resource subj = iter.next();
      System.out.println(subj.getLocalName());
    }
//    // some definitions
//    String personURI    = "http://somewhere/JohnSmith";
//    String fullName     = "John Smith";
//
//    // create an empty Model
//    model = ModelFactory.createDefaultModel();
//
//    // create the resource
//    Resource johnSmith = model.createResource(personURI);
//    model.setNsPrefix("soheil", "http://www.test.org/2001/vcard-rdf2/3.0#");
//
//    johnSmith.addProperty(RDF.type, model.createResource("http://www.test.org/2001/vcard-rdf2/3.0#Person"));
//    
//    // add the property
//    johnSmith.addProperty(VCARD.FN, fullName);
//    johnSmith.addProperty(model.createProperty("http://www.test.org/2001/vcard-rdf2/3.0#test"), "test");
//    model.write(System.out);
//    model.write(System.out, "RDF/XML-ABBREV");
//    
//    
//    // list the statements in the Model
//    StmtIterator iter = model.listStatements();
//
//    // print out the predicate, subject and object of each statement
//    while (iter.hasNext()) {
//        Statement stmt      = iter.nextStatement();  // get next statement
//        Resource  subject   = stmt.getSubject();     // get the subject
//        Property  predicate = stmt.getPredicate();   // get the predicate
//        RDFNode   object    = stmt.getObject();      // get the object
//
//        System.out.print(subject.toString());
//        System.out.print(" " + predicate.toString() + " ");
//        
//        if (object instanceof Resource) {
//           System.out.print(object.toString());
//        } else {
//            // object is a literal
//            System.out.print(" \"" + object.toString() + "\"");
//        }
//
//        System.out.println(" .");
//    } 
//    
//    
//     Model m = ModelFactory.createDefaultModel();
//     String nsA = "http://somewhere/else#";
//     String nsB = "http://nowhere/else#";
//     Resource root = m.createResource( nsA + "root" );
//     Property P = m.createProperty( nsA + "P" );
//     Property Q = m.createProperty( nsB + "Q" );
//     Resource x = m.createResource( nsA + "x" );
//     Resource y = m.createResource( nsA + "y" );
//     Resource z = m.createResource( nsA + "z" );
//     m.add( root, P, x ).add( root, P, y ).add( y, Q, z );
//     System.out.println( "# -- no special prefixes defined" );
//     m.write( System.out );
//     System.out.println( "# -- nsA defined" );
//     m.setNsPrefix( "nsA", nsA );
//     m.write( System.out );
//     System.out.println( "# -- nsA and cat defined" );
//     m.setNsPrefix( "cat", nsB );
//     m.write( System.out );


  }
}
