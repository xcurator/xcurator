/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Amir
 */
public class CLIRunnerTest extends TestCase {

    public CLIRunnerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(CLIRunnerTest.class);
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
     * Test of main method, of class CLIRunner.
     */

    public void testMain() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/drugbank/data/drugbank-sample-sm.xml -m xcurator-data/drugbank/data/mapping-sm.xml -s U";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

}
