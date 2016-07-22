/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli;

import edu.toronto.cs.xcurator.cli.config.ConfigSuite;
import edu.toronto.cs.xcurator.cli.mapping.MappingSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Amir
 */
public class CliSuite extends TestCase {
    
    public CliSuite(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("CliSuite");
        suite.addTest(CLIRunnerTest.suite());
        suite.addTest(MappingSuite.suite());
        suite.addTest(UtilTest.suite());
        suite.addTest(RdfFactoryTest.suite());
        suite.addTest(ConfigSuite.suite());
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
    
}
