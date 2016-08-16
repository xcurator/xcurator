/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 *
 * @author Amir
 */
public class SparqlConsumer {

    public SparqlConsumer() {

    }

    public static void main(String[] args) {
        String q = "PREFIX  g:    <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
                + "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX  onto: <http://dbpedia.org/ontology/>\n"
                + "\n"
                + "SELECT  ?subject ?stadium ?lat ?long\n"
                + "WHERE\n"
                + "  { ?subject g:lat ?lat .\n"
                + "    ?subject g:long ?long .\n"
                + "    ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> onto:Stadium .\n"
                + "    ?subject rdfs:label ?stadium\n"
                + "    FILTER ( ( ( ( ( ?lat >= 52.4814 ) && ( ?lat <= 57.4814 ) ) && ( ?long >= -1.89358 ) ) && ( ?long <= 3.10642 ) ) && ( lang(?stadium) = \"en\" ) )\n"
                + "  }\n"
                + "LIMIT   5\n"
                + "";
        String endpoint = "http://dbpedia.org/sparql";
        query(endpoint, q);

    }

    public static void query(String endpoint, String query) {
        Query queryObj = QueryFactory.create(query); //s2 = the query above
        QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, queryObj);
        ResultSet results = qExe.execSelect();
        ResultSetFormatter.out(System.out, results, queryObj);

    }
}
