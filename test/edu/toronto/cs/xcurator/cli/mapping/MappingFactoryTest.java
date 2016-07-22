/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli.mapping;

import edu.toronto.cs.xcurator.mapping.Mapping;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;

/**
 *
 * @author Amir
 */
public class MappingFactoryTest extends TestCase {
    
    public MappingFactoryTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(MappingFactoryTest.class);
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
     * Test of createInstance method, of class MappingFactory.
     */
    public void testCreateInstance_Document_String() {
        System.out.println("createInstance");
        Document xmlDocument = null;
        String steps = "";
        MappingFactory instance = null;
        Mapping expResult = null;
        Mapping result = instance.createInstance(xmlDocument, steps);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createInstance method, of class MappingFactory.
     */
    public void testCreateInstance_3args_1() throws Exception {
        System.out.println("createInstance");
        Document xmlDocument = null;
        String mappingFile = "";
        String steps = "";
        MappingFactory instance = null;
        Mapping expResult = null;
        Mapping result = instance.createInstance(xmlDocument, mappingFile, steps);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createInstance method, of class MappingFactory.
     */
    public void testCreateInstance_List_String() {
        System.out.println("createInstance");
        List<Document> xmlDocuments = null;
        String steps = "";
        MappingFactory instance = null;
        Mapping expResult = null;
        Mapping result = instance.createInstance(xmlDocuments, steps);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createInstance method, of class MappingFactory.
     */
    public void testCreateInstance_3args_2() throws Exception {
        System.out.println("createInstance");
        List<Document> xmlDocuments = null;
        String fileName = "";
        String steps = "";
        MappingFactory instance = null;
        Mapping expResult = null;
        Mapping result = instance.createInstance(xmlDocuments, fileName, steps);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
