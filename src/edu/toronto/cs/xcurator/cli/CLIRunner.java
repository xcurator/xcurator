/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli;

import edu.toronto.cs.xcurator.cli.config.RunConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Amirhossein Aleyasen <aleyase2@illinois.edu>
 * created on Apr 2, 2016, 1:14:53 PM
 */
public class CLIRunner {

    private static boolean serializeMapping = false;
    private static String mappingFilename;
    private static String tdbDirectory;
    private static String domain;
    private static String[] fileLocations;
    private static List<InputStream> inputStreams;
    private static DocumentBuilder builder;

    public static void main(String[] args) {
        Options options = setupOptions();
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('t')) {
                tdbDirectory = line.getOptionValue('t');
                File d = new File(tdbDirectory);
                if (!d.exists() || !d.isDirectory()) {
                    throw new Exception("TDB directory does not exist, please create.");
                }
            } else {
                printHelpAndExit(options);
            }
            if (line.hasOption('h')) {
                domain = line.getOptionValue('h');
                try {
                    URL url = new URL(domain);
                } catch (MalformedURLException ex) {
                    throw new Exception("The domain name is ill-formed");
                }
            } else {
                printHelpAndExit(options);
            }
            if (line.hasOption('m')) {
                serializeMapping = true;
                mappingFilename = line.getOptionValue('m');
            }
            if (line.hasOption('d')) {
                fileLocations = line.getOptionValues('d');
                inputStreams = new ArrayList<>();
                for (String inputfile : fileLocations) {
                    File f = new File(inputfile);
                    if (f.isFile() && f.exists()) {
                        System.out.println("Adding document to mapping discoverer: " + inputfile);
                        inputStreams.add(new FileInputStream(f));
                    } // If it is a URL download link for the document from SEC
                    else if (inputfile.startsWith("http") && inputfile.contains("://")) {
                        // Download
                        System.out.println("Adding remote document to mapping discoverer: " + inputfile);
                        try {
                            URL url = new URL(inputfile);
                            InputStream remoteDocumentStream = url.openStream();
                            inputStreams.add(remoteDocumentStream);
                        } catch (MalformedURLException ex) {
                            throw new Exception("The document URL is ill-formed: " + inputfile);
                        } catch (IOException ex) {
                            throw new Exception("Error in downloading remote document: " + inputfile);
                        }
                    } else {
                        throw new Exception("Cannot open XBRL document: " + f.getName());
                    }
                }
            } else {
                printHelpAndExit(options);
            }
            setupDocumentBuilder();
            RdfFactory rdfFactory = new RdfFactory(new RunConfig(domain));
            List<Document> documents = new ArrayList<>();
            for (InputStream inputStream : inputStreams) {
                Document dataDocument = createDocument(inputStream);
                documents.add(dataDocument);
            }
            if (serializeMapping) {
                System.out.println("Mapping file will be saved to: " + new File(mappingFilename).getAbsolutePath());
                rdfFactory.createRdfs(documents, tdbDirectory, mappingFilename);
            } else {
                rdfFactory.createRdfs(documents, tdbDirectory);
            }
        } catch (Exception ex) {
            System.err.println("Unexpected exception: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption("m", "mapping-file", true, "Optional. File name of the "
                + "mapping file to output. If none then there will be no mapping file output.");
        options.addOption("t", "tdb", true, "Directory of the TDB used for "
                + "storing the RDFs converted");
        options.addOption(OptionBuilder.withLongOpt("documents")
                .withDescription("XBRL documents to be converted to RDFs")
                .hasArg().hasArgs()
                .create('d'));
        options.addOption("h", "domain", true, "The generated RDFs will have this domain name in their URIs.");
        return options;
    }

    private static Document createDocument(InputStream inputStream)
            throws SAXException, IOException {
        return builder.parse(inputStream);
    }

    private static void setupDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builder = builderFactory.newDocumentBuilder();
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("xbrl2rdf.jar", options, true);
        System.exit(1);
    }

}
