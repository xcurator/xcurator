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

import edu.toronto.cs.xcurator.eval.EvalUtil;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

public class MappingGeneratorEval extends TestCase {

//    @Ignore("we will use entity with attribute accuracy instead.")
//    @Test
//    public void testAccuracyforEntities() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
//
////        int[] max = new int[]{10, 25, 50, 100, 250, 500, 1000}; //20, 40, 50, 100, 125, 250, 500, 1000, 2000 }; // 5, 10, 20, 40, 50, 100, 125, 250, 500, 1000, 2000};
//        // 10, 25, 50, 100, 250, 500, 1000
////        int[] phase = new int[]{1, 2, 3, 4, 5};
//        String inputfile = "xcurator-data\\drugbank\\data\\mapping-sm.xml";
//
//        Set<String> entitySet = getEntities(inputfile);
//
////        System.out.println("Entities found: " + grEntityList.size());
////        for (String entity : grEntityList) {
////            System.out.println(entity);
////        }
//        String gtInputfile = "xcurator-data\\drugbank\\ground-truth\\classes.csv";
//        final String content = FileUtils.readFileToString(new File(gtInputfile));
//        List<String> grEntityList = Arrays.asList(content.split("\\r?\\n"));
//
//        Set<String> grEntitySet = new HashSet<>(grEntityList);
////                inputfile = "output/output.ct." + Integer.toString(p) + "." + m + ".xml";
//        printAccuracyStats(entitySet, grEntitySet);
//    }
    @Test
    public void testAccuracyforEntitiesAndAtrributes() {
        String root = "xcurator-data\\clinicaltrials\\ground-truth\\";
        String inputfile = root + "mapping-KIG_2.xml";
        String gtEntityInputfile = root + "ents.txt";
        String gtAttributeInputfile = root + "attrs.txt";
        try {
            EvalUtil.genAccuracyforEntitiesAndAtrributes(inputfile, gtEntityInputfile, gtAttributeInputfile, false);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            Logger.getLogger(MappingGeneratorEval.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
