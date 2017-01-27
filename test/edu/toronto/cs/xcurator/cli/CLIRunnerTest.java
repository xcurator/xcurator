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
    public void testDrugbankBasicDiscovery() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/drugbank/data/drugbank-sample-sm.xml -m xcurator-data/drugbank/data/mapping-sm.xml -s U";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testDrugbankKI() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/drugbank/data/drugbank-sample-sm.xml -m xcurator-data/drugbank/data/mapping-sm-KI.xml -s KI";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testDrugbankG() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/drugbank/data/drugbank-sample-sm.xml -m xcurator-data/drugbank/data/mapping-sm-G.xml -s G";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testCtd_G() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/ctd/CTD_chemicals.xml -m xcurator-data/ctd/mapping.xml -s G";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testInterpro_G() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/interpro/LARGEFILES/interpro.xml -m xcurator-data/interpro/mapping.xml -s G";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testInterpro_KIG() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/interpro/LARGEFILES/interpro.xml -m xcurator-data/interpro/mapping-KIG.xml -s KIG";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testClinicalTrial_KIG() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/clinicaltrials/data/content.xml -m xcurator-data/clinicaltrials/mapping-KIG_2.xml -s KIG";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testBookstore_KIG() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/bookstore/bookstore.xml -m xcurator-data/bookstore/mapping-KIG.xml -s KIG";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testBookstore() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/bookstore/bookstore.xml -m xcurator-data/bookstore/mapping.xml";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testBookstore_B() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/bookstore/bookstore.xml -m xcurator-data/bookstore/mapping-B.xml -s B";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testBookstore_BI() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/bookstore/bookstore.xml -m xcurator-data/bookstore/mapping-BI.xml -s BI";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }

    public void testBookstore_BKI() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/bookstore/bookstore.xml -m xcurator-data/bookstore/mapping-BKI.xml -s BKI";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }
    
    public void testEvaluation() {
        System.out.println("main");
        String argsStr = "-f xcurator-data/bookstore/bookstore.xml -m xcurator-data/bookstore/mapping-BKI.xml -s BKI";
        String[] args = argsStr.split("\\s");
        CLIRunner.main(args);
    }
    
    

//        BASIC('B'),
//        INTERLIKNING('I'),
//        KEYATTRIBUTE('K'),
//        REMOVE_GROUPING_NODES('G');
}
