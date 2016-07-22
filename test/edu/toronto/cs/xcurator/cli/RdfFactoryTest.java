/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;

/**
 *
 * @author Amir
 */
public class RdfFactoryTest extends TestCase {
    
    public RdfFactoryTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RdfFactoryTest.class);
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of createRdfs method, of class RdfFactory.
     */
    public void testCreateRdfs_3args_1() {
        System.out.println("createRdfs");
        List<Document> xbrlDocuments = null;
        String tdbDirectory = "";
        String steps = "";
        RdfFactory instance = null;
        instance.createRdfs(xbrlDocuments, tdbDirectory, steps);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createRdfs method, of class RdfFactory.
     */
    public void testCreateRdfs_4args_1() throws Exception {
        System.out.println("createRdfs");
        List<Document> xbrlDocuments = null;
        String tdbDirectory = "";
        String mappingFile = "";
        String steps = "";
        RdfFactory instance = null;
        instance.createRdfs(xbrlDocuments, tdbDirectory, mappingFile, steps);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createRdfs method, of class RdfFactory.
     */
    public void testCreateRdfs_3args_2() {
        System.out.println("createRdfs");
        Document xbrlDocument = null;
        String tdbDirectory = "";
        String steps = "";
        RdfFactory instance = null;
        instance.createRdfs(xbrlDocument, tdbDirectory, steps);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createRdfs method, of class RdfFactory.
     */
    public void testCreateRdfs_4args_2() throws Exception {
        System.out.println("createRdfs");
        Document xbrlDocument = null;
        String tdbDirectory = "";
        String mappingFile = "";
        String steps = "";
        RdfFactory instance = null;
        instance.createRdfs(xbrlDocument, tdbDirectory, mappingFile, steps);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
