/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli.config;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Amir
 */
public class RunConfigTest extends TestCase {
    
    public RunConfigTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RunConfigTest.class);
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
     * Test of getResourceUriBase method, of class RunConfig.
     */
    public void testGetResourceUriBase() {
        System.out.println("getResourceUriBase");
        RunConfig instance = null;
        String expResult = "";
        String result = instance.getResourceUriBase();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTypeResourceUriBase method, of class RunConfig.
     */
    public void testGetTypeResourceUriBase() {
        System.out.println("getTypeResourceUriBase");
        RunConfig instance = null;
        String expResult = "";
        String result = instance.getTypeResourceUriBase();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPropertyResourceUriBase method, of class RunConfig.
     */
    public void testGetPropertyResourceUriBase() {
        System.out.println("getPropertyResourceUriBase");
        RunConfig instance = null;
        String expResult = "";
        String result = instance.getPropertyResourceUriBase();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTypeResourcePrefix method, of class RunConfig.
     */
    public void testGetTypeResourcePrefix() {
        System.out.println("getTypeResourcePrefix");
        RunConfig instance = null;
        String expResult = "";
        String result = instance.getTypeResourcePrefix();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPropertyResourcePrefix method, of class RunConfig.
     */
    public void testGetPropertyResourcePrefix() {
        System.out.println("getPropertyResourcePrefix");
        RunConfig instance = null;
        String expResult = "";
        String result = instance.getPropertyResourcePrefix();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
