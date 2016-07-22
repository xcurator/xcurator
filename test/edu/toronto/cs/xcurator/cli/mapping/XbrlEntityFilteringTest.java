/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli.mapping;

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.mapping.Mapping;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Amir
 */
public class XbrlEntityFilteringTest extends TestCase {
    
    public XbrlEntityFilteringTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(XbrlEntityFilteringTest.class);
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
     * Test of process method, of class XbrlEntityFiltering.
     */
    public void testProcess() {
        System.out.println("process");
        List<DataDocument> dataDocuments = null;
        Mapping mapping = null;
        XbrlEntityFiltering instance = new XbrlEntityFiltering();
        instance.process(dataDocuments, mapping);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
