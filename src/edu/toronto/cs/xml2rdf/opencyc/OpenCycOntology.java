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
package edu.toronto.cs.xml2rdf.opencyc;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.toronto.cs.xml2rdf.interlink.Interlinker;
import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.string.StringMetric;

public class OpenCycOntology implements Interlinker {

	static boolean debug = true;

	OntModel model;

	private OpenCycOntology() {
		model = JenaUtils.loadOntology(this.getClass().getResourceAsStream("/opencyc/opencyc-latest.owl"));
	}

	private Set<String> getTypesOfSubject(Resource subject) {
		Set<String> ret = new HashSet<String>();

		StmtIterator stiter2 =
				model.listStatements(new SimpleSelector(subject, RDF.type, (RDFNode) null));
		while(stiter2.hasNext()) {
			String uri = stiter2.next().getObject().asResource().getURI();
			if (uri.startsWith("http://sw.opencyc.org")) {
				ret.add(uri);
			}
		}

		return ret;
	}

	public Set<String> findTypesForResourceSPARQL(String str, StringMetric metric, double threshold) {
		str = str.replaceAll("\\s+", "\\\\\\\\s+");

		String queryStr =   "select ?t \n" +
				"where { \n" +
				"?s <" + RDFS.label.getURI() + "> ?l .\n" +
				"FILTER regex(?l, \"^" + str + "$\", \"i\" ) .\n" +
				"?s <" + RDF.type.getURI() + "> ?t ." +
				"}";

		Set<String> types = new HashSet<String>();
		QueryExecution qExec = null;
		try{
			qExec = QueryExecutionFactory.create(queryStr, model);
			ResultSet rs = qExec.execSelect();
			while (rs.hasNext()) {
				QuerySolution solution = rs.next();
				types.add(solution.get("?t").asLiteral().getString());
			}
		}catch(Exception e) {
			if (debug)
				e.printStackTrace();
		} finally {
			if (qExec != null) {
				qExec.close();
			}
		}

		queryStr =   "select ?t \n" +
				"where { \n" +
				"?s <http://sw.opencyc.org/concept/Mx4rwLSVCpwpEbGdrcN5Y29ycA> ?l .\n" +
				"FILTER regex(?l, \"^" + str + "$\", \"i\" ) .\n" +
				"?s <" + RDF.type.getURI() + "> ?t ." +
				"}";

		types = new HashSet<String>();
		qExec = null;
		try{
			qExec = QueryExecutionFactory.create(queryStr, model);
			ResultSet rs = qExec.execSelect();
			while (rs.hasNext()) {
				QuerySolution solution = rs.next();
				String solStr = solution.get("?t").asResource().getURI().toString();
				if (solStr.startsWith("http://sw.opencyc.org")) {
					types.add(solStr);
				}
			}
		} catch (Exception e) {
			if (debug)
				e.printStackTrace();
		} finally {
			if (qExec != null) {
				qExec.close();
			}
		}

		return types;
	}

	@Override
	public Set<String> findTypesForResource(String str, StringMetric metric, double threshold) {
		Set<String> ret = new HashSet<String>();

		StmtIterator iter =
				model.listStatements(new SimpleSelector(null, RDFS.label, (RDFNode) null));

		while(iter.hasNext()) {
			Statement st = iter.next();
			String resourceStr = st.getObject().asLiteral().getString();
			double similarity = metric.getSimilarity(str, resourceStr);

			if (similarity >= threshold) {
				ret.addAll(getTypesOfSubject(st.getSubject()));
			}
		}

		Property prettyString = model.createProperty("http://sw.opencyc.org/concept/Mx4rwLSVCpwpEbGdrcN5Y29ycA");
		iter = model.listStatements(new SimpleSelector(null, prettyString, (RDFNode) null));

		while(iter.hasNext()) {
			Statement st = iter.next();
			String resourceStr = st.getObject().asLiteral().getString();
			double similarity = metric.getSimilarity(str, resourceStr);

			if (similarity >= threshold) {
				ret.addAll(getTypesOfSubject(st.getSubject()));
			}
		}


		return ret;
	}

	@Override
	public Set<String> findSameAsForResource(String str, StringMetric metric, double threshold, Set<String> types) {
		Set<String> ret = new HashSet<String>();

		StmtIterator iter =
				model.listStatements(new SimpleSelector(null, RDFS.label, (RDFNode) null));

		while(iter.hasNext()) {
			Statement st = iter.next();
			String resourceStr = st.getObject().asLiteral().getString();
			double similarity = metric.getSimilarity(str, resourceStr);

			if (similarity >= threshold) {
				Set<String> subjectTypes = getTypesOfSubject(st.getSubject());
				boolean found = false;
				for (String subjectT: subjectTypes) {
					if (types.contains(subjectT)) {
						found = true;
						break;
					}
				}

				if (found) {
					ret.add(st.getSubject().getURI());
				}
			}
		}

		Property prettyString = model.createProperty("http://sw.opencyc.org/concept/Mx4rwLSVCpwpEbGdrcN5Y29ycA");
		iter = model.listStatements(new SimpleSelector(null, prettyString, (RDFNode) null));

		while(iter.hasNext()) {
			Statement st = iter.next();
			String resourceStr = st.getObject().asLiteral().getString();
			double similarity = metric.getSimilarity(str, resourceStr);

			if (similarity >= threshold) {
				Set<String> subjectTypes = getTypesOfSubject(st.getSubject());
				boolean found = false;
				for (String subjectT: subjectTypes) {
					if (types.contains(subjectT)) {
						found = true;
						break;
					}
				}

				if (found) {
					ret.add(st.getSubject().getURI());
				}
			}
		}

		return ret;
	}


	public String getLabelForResource(String uri) {
		Resource subject = model.createResource(uri);
		Statement stmt = subject.getProperty(RDFS.label);
		if (stmt != null) {
			RDFNode object = stmt.getObject();
			return object == null ? "" : object.asLiteral().getString();
		} else {
			return "";
		}
	}

	static OpenCycOntology instance = new OpenCycOntology();
	public static OpenCycOntology getInstance() {
		return instance;
	}

	@Override
	public Map<String, Set<String>> findTypesForResources(List<String> str,
			StringMetric metric, double threshold) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Set<String>> findSameAsForResources(List<String> str,
			StringMetric metric, double threshold, Set<String> types) {
		// TODO Auto-generated method stub
		return null;
	}

}
