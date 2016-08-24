/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.discoverer;

import java.util.Arrays;
import junit.framework.TestCase;
import type.detect.TypeDetect;
import type.detect.kb.knowledgegraph.KnowledgeGraphUtil;

/**
 *
 * @author Amir
 */
public class OntologyLinkAdditionTest extends TestCase {

    public void testKnowledgeGraphAPI() {
        final String type = KnowledgeGraphUtil.getType("obama");
        System.out.println(type);
    }

    public void testWikiDATASingleTerm() {
        TypeDetect td = new TypeDetect();
        final String type = td.besttype("obama");
        System.out.println(type);
    }

    public void testWikiDATAMultipleTerms() {
        TypeDetect td = new TypeDetect();
        final String type = td.besttype(Arrays.asList("ITGB3", "ERBB2", "CTLA4"));
        System.out.println(type);
    }

}
