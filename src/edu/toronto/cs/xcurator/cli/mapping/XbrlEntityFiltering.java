package edu.toronto.cs.xcurator.cli.mapping;

import edu.toronto.cs.xcurator.common.DataDocument;
import edu.toronto.cs.xcurator.discoverer.MappingDiscoveryStep;
import edu.toronto.cs.xcurator.mapping.Schema;
import edu.toronto.cs.xcurator.mapping.Mapping;
import edu.toronto.cs.xcurator.mapping.Relation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ekzhu
 */
public class XbrlEntityFiltering implements MappingDiscoveryStep {

    @Override
    public void process(List<DataDocument> dataDocuments, Mapping mapping) {
        Set<String> typesToRemove = new HashSet<>();
        Iterator<Schema> iterator = mapping.getEntityIterator();
        while (iterator.hasNext()) {
            Schema entity = iterator.next();
            // Remove entities that were not extracted from the standard namespaces
            // We only keep the standard ones
            String xmlTypeUri = entity.getXmlTypeUri();
            if (!xmlTypeUri.startsWith("http://fasb.org/us-gaap/")
                    && !xmlTypeUri.startsWith("http://xbrl.sec.gov/")
                    && !xmlTypeUri.startsWith("http://www.xbrl.org/")
                    && !xmlTypeUri.startsWith("http://xbrl.org/")
                    && !xmlTypeUri.startsWith("http://www.w3.org/")) {
                typesToRemove.add(xmlTypeUri);
            }
        }

        // Remove the entities from the mapping so no RDFs will be generated
        // for them
        for (String xmlTypeUri : typesToRemove) {
            mapping.removeEntity(xmlTypeUri);
        }

        // Remove the entities that are related to the document
        Schema rootEntity = mapping.getEntity("http://www.xbrl.org/2003/instance/xbrl");
        List<String> relToRemove = new ArrayList<>();
        Iterator<Relation> relIterator = rootEntity.getRelationIterator();
        while (relIterator.hasNext()) {
            Relation relation = relIterator.next();
            if (typesToRemove.contains(relation.getObjectXmlTypeUri())) {
                relToRemove.add(relation.getId());
            }
        }
        for (String relId : relToRemove) {
            rootEntity.removeRelation(relId);
        }

    }

}
