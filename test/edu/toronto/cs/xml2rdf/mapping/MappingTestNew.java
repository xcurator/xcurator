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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.toronto.cs.xml2rdf.string.NoWSCaseInsensitiveStringMetric;
import edu.toronto.cs.xml2rdf.utils.LogUtils;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

public class MappingTestNew extends TestCase {

    public void testLoadMapping() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        String[] blacklist = {
            "http://rdf.freebase.com/rdf/m.04mp1fp",
            "http://rdf.freebase.com/rdf/location.dated_location",
            "http://rdf.freebase.com/rdf/location.statistical_region",
            "http://rdf.freebase.com/rdf/location.administrative_division",
            "http://rdf.freebase.com/rdf/music.artist",
            "http://rdf.freebase.com/rdf/base.whoami.answer",
            "http://rdf.freebase.com/rdf/base.legislation.vote_value",
            "http://rdf.freebase.com/rdf/m.04lqt84",
            "http://rdf.freebase.com/rdf/base.umltools.design_pattern",
            "http://rdf.freebase.com/rdf/base.braziliangovt.brazilian_governmental_vote_type",
            "http://sw.opencyc.org/concept/Mx4rJ3ZbguI8QdeGDNhCi9LL3Q",
            "http://rdf.freebase.com/rdf/base.whoami.answer",
            "http://sw.opencyc.org/concept/Mx4rvUCoPtoTQdaZVdw2OtjsAg",
            "http://sw.opencyc.org/concept/Mx4rveI9NpwpEbGdrcN5Y29ycA",
            "http://rdf.freebase.com/rdf/m.04mp1fp",
            "http://sw.opencyc.org/concept/Mx4rIGTaIPAIQdaffLzGWDo0Zw",
            "http://rdf.freebase.com/rdf/m.04lqt84",
            "http://sw.opencyc.org/concept/Mx4rvVj8VZwpEbGdrcN5Y29ycA",
            "http://rdf.freebase.com/rdf/military.military_combatant",
            "http://rdf.freebase.com/rdf/book.book_subject",
            "http://rdf.freebase.com/rdf/sports.sports_team_location",
            "http://rdf.freebase.com/rdf/user.tsegaran.random.taxonomy_subject",
            "http://rdf.freebase.com/rdf/food.beer_country_region",
            "http://rdf.freebase.com/rdf/user.skud.flags.flag_having_thing",
            "http://rdf.freebase.com/rdf/m.04l1354",
            "http://rdf.freebase.com/rdf/olympics.olympic_participating_country",
            "http://rdf.freebase.com/rdf/organization.organization_member",
            "http://rdf.freebase.com/rdf/biology.breed_origin",
            "http://rdf.freebase.com/rdf/user.robert.military.military_power",
            "http://rdf.freebase.com/rdf/base.ontologies.ontology_instance",
            "http://rdf.freebase.com/rdf/government.governmental_jurisdiction"
        };

        Mapping mapping = new Mapping("/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/output.500.xml", new HashSet<String>(Arrays.asList(blacklist)));
        //"/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/resources/clinicaltrials/mapping/linkedct-mapping.xml");
        BufferedReader br = new BufferedReader(new FileReader("/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/resources/clinicaltrials/data/filelist.txt"));
        String line = null;
        while ((line = br.readLine()) != null) {
            String url = "http://clinicaltrials.gov" + line.trim().replace("/ct2", "") + "?displayxml=true";
            Document dataDoc = XMLUtils.parse((InputStream) new URL(url).getContent(), -1);
            dataDoc = XMLUtils.addRoot(dataDoc, "clinical_studies");
//      OutputFormat format = new OutputFormat(dataDoc);
//      format.setLineWidth(65);
//      format.setIndenting(true);
//      format.setIndent(2);
//      XMLSerializer serializer = new XMLSerializer (
//          System.out, format);
//      serializer.asDOMSerializer();
//      serializer.serialize(dataDoc);
            Date from = new Date();
            String typePrefix = "http://www.linkedct.org/0.1#";
            mapping.generateRDFSchema("/home/soheil/Archive/finaltdb", dataDoc, typePrefix, null, "RDF/XML-ABBREV",
                    new NoWSCaseInsensitiveStringMetric(), 1);
            mapping.generateRDFs("/home/soheil/Archive/finaltdb", dataDoc, typePrefix, null, "RDF/XML-ABBREV",
                    new NoWSCaseInsensitiveStringMetric(), 1);
            LogUtils.info(MappingTestNew.class, "Imported : " + line + " @ " + (new Date().getTime() - from.getTime()));
            //"/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/resources/clinicaltrials/data/content.xml", -1);
        }
        //    "/home/soheil/workspaces/workspace-xml2rdf/xml2rdf-java/resources/clinicaltrials/data/NCT00000219.xml");
    }
}
