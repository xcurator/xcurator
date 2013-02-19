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

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.toronto.cs.xml2rdf.string.StringMetric;

public class JenaUtils {

	public static Model getTDBModel(String path) {
		Model m = TDBFactory.createModel(path);
		return m;
	}

	//  public static Model getANewModel() {
	//    Model model = ModelFactory.createDefaultModel();
	//    return model;
	//  }

	public static OntModel loadOntology(InputStream is) {
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
		m.read(is, null);
		return m;
	}

	public static List<Statement> getBestMatchingStatements(OntModel ontology,
			StringMetric metric, String term) {
		StmtIterator iter =
				ontology.listStatements(new SimpleSelector(null, RDFS.label, (RDFNode) null));

		double maxSimilarity = Double.MIN_VALUE;
		List<Statement> bestChoices = new LinkedList<Statement>();

		while(iter.hasNext()) {
			Statement st = iter.next();
			String objectStr = st.getObject().asLiteral().getString();

			double similarity = metric.getSimilarity(term, objectStr);

			if (similarity <= 0) {
				continue;
			}

			if (similarity > maxSimilarity) {
				maxSimilarity = similarity;
				bestChoices.clear();
			} else if (similarity == maxSimilarity) {
				bestChoices.add(st);
			}
		}

		return bestChoices;
	}

	private static long varCounter = 1;
	public static synchronized String getNextSparqlVarName() {
		return "?x" + varCounter++; 
	}

	public static String querify(String clause) { 
		return clause.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"");
	}

	public static void main(String[] args) {
		System.out.println(querify("efficacy of platelets rich plasma (PRP \\ PRGF)"));  
	}
}
