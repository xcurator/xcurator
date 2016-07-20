package edu.toronto.cs.xcurator.cli;

import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.rdf.RdfGeneration;
import edu.toronto.cs.xcurator.rdf.RdfGenerator;
import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.cli.config.RunConfig;
import edu.toronto.cs.xcurator.cli.mapping.MappingFactory;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerConfigurationException;
import org.w3c.dom.Document;

public class RdfFactory {

    private final MappingFactory mappingFactory;
    private final RunConfig config;

    public RdfFactory(RunConfig config) {
        this.config = config;
        mappingFactory = new MappingFactory(this.config);
    }

    /**
     * Generate RDFs from multiple XBRL documents, do not serialize the mapping
     *
     * @param xbrlDocuments
     * @param tdbDirectory
     */
    public void createRdfs(List<Document> xbrlDocuments, String tdbDirectory) {
        Mapping mapping = mappingFactory.createInstance(xbrlDocuments);
        generateRdfs(xbrlDocuments, tdbDirectory, mapping);
    }

    /**
     * Generate RDFs from multiple XBRL documents, and the mapping will be
     * serialized to the given file.
     *
     * @param xbrlDocuments
     * @param tdbDirectory
     * @param mappingFile
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.transform.TransformerConfigurationException
     */
    public void createRdfs(List<Document> xbrlDocuments, String tdbDirectory, String mappingFile)
            throws FileNotFoundException, TransformerConfigurationException {
        Mapping mapping = mappingFactory.createInstance(xbrlDocuments, mappingFile);
        generateRdfs(xbrlDocuments, tdbDirectory, mapping);
    }

    /**
     * Generate RDFs from the XBRL document, do not serialize mapping
     *
     * @param xbrlDocument
     * @param tdbDirectory
     */
    public void createRdfs(Document xbrlDocument, String tdbDirectory) {
        List<Document> documents = new ArrayList<>();
        documents.add(xbrlDocument);
        createRdfs(documents, tdbDirectory);
    }

    /**
     * Generate RDFs from the XBRL document, and the mapping will be serialized
     * to the mapping file.
     *
     * @param xbrlDocument
     * @param tdbDirectory
     * @param mappingFile
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws java.io.FileNotFoundException
     */
    public void createRdfs(Document xbrlDocument, String tdbDirectory, String mappingFile)
            throws TransformerConfigurationException, FileNotFoundException {
        List<Document> documents = new ArrayList<>();
        documents.add(xbrlDocument);
        createRdfs(documents, tdbDirectory, mappingFile);
    }

    private void generateRdfs(List<Document> xbrlDocuments, String tdbDirectory, Mapping mapping) {
        RdfGenerator rdfGenerator = new RdfGenerator(mapping);
        for (Document document : xbrlDocuments) {
            rdfGenerator.addDataDocument(new DataDocument(document));
        }
        rdfGenerator.addStep(new RdfGeneration(tdbDirectory, config));
        rdfGenerator.generateRdfs();
    }
}
