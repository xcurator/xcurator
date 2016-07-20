package edu.toronto.cs.xcurator.cli.mapping;

import edu.toronto.cs.xcurator.discoverer.BasicEntityDiscovery;
import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.discoverer.MappingDiscoverer;
import edu.toronto.cs.xcurator.discoverer.SerializeMapping;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.XmlBasedMapping;
import edu.toronto.cs.xcurator.common.RdfUriBuilder;
import edu.toronto.cs.xcurator.common.XmlDocumentBuilder;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import edu.toronto.cs.xcurator.common.XmlUriBuilder;
import edu.toronto.cs.xcurator.discoverer.HashBasedEntityInterlinking;
import edu.toronto.cs.xcurator.discoverer.KeyAttributeDiscovery;
import edu.toronto.cs.xcurator.cli.config.RunConfig;
import org.w3c.dom.Document;

public class MappingFactory {

    private final RunConfig config;

    public MappingFactory(RunConfig config) {
        this.config = config;
    }

    /**
     * Create a mapping instance by discovering entities in the XBRL document,
     * do not serialize the mapping.
     *
     * @param xbrlDocument
     * @return
     */
    public Mapping createInstance(Document xbrlDocument) {
        List<Document> docList = new ArrayList<>();
        docList.add(xbrlDocument);
        return createInstance(docList);
    }

    /**
     * Create a mapping instance by discovering entities in the XBRL document,
     * and the mapping will be serialized to the mapping file
     *
     * @param xbrlDocument
     * @param mappingFile
     * @return
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     */
    public Mapping createInstance(Document xbrlDocument, String mappingFile)
            throws TransformerConfigurationException, FileNotFoundException {
        List<Document> docList = new ArrayList<>();
        docList.add(xbrlDocument);
        return createInstance(docList, mappingFile);
    }

    /**
     * Create a mapping instance by discovering entities in the multiple XBRL
     * documents, do not serialize the mapping.
     *
     * @param xbrlDocuments
     * @return
     */
    public Mapping createInstance(List<Document> xbrlDocuments) {
        Mapping mapping = buildXmlBasedMapping();
        MappingDiscoverer discoverer = buildBasicDiscoverer(xbrlDocuments, mapping);
        discoverer.discoverMapping();
        return mapping;
    }

    /**
     * Create a mapping instance by discovering entities in the multiple XBRL
     * documents, and the mapping will be serialized to the mapping file.
     *
     * @param xbrlDocuments
     * @param fileName
     * @return
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     */
    public Mapping createInstance(List<Document> xbrlDocuments, String fileName)
            throws TransformerConfigurationException, FileNotFoundException {
        Mapping mapping = buildXmlBasedMapping();
        MappingDiscoverer discoverer = buildBasicDiscoverer(xbrlDocuments, mapping);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        discoverer.addStep(new SerializeMapping(new XmlDocumentBuilder(),
                new FileOutputStream(fileName),
                transformer, config));

        discoverer.discoverMapping();
        return mapping;
    }

    private MappingDiscoverer buildBasicDiscoverer(List<Document> xbrlDocuments,
            Mapping mapping) {
        MappingDiscoverer discoverer = new MappingDiscoverer(mapping);

        String resourceUriPattern = config.getResourceUriBase() + "${UUID}";
        for (Document document : xbrlDocuments) {
            discoverer.addDataDocument(new DataDocument(document, resourceUriPattern));
        }

        discoverer.addStep(new BasicEntityDiscovery(
                new XmlParser(),
                new RdfUriBuilder(config), new XmlUriBuilder(), true));
        discoverer.addStep(new KeyAttributeDiscovery());
        discoverer.addStep(new HashBasedEntityInterlinking(new RdfUriBuilder(config)));
//        discoverer.addStep(new XbrlEntityFiltering());

        return discoverer;

    }

    private Mapping buildXmlBasedMapping() {
        return new XmlBasedMapping();
    }
}
