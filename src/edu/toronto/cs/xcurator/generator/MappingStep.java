package edu.toronto.cs.xcurator.generator;

import java.util.Map;

import org.w3c.dom.Element;

import edu.toronto.cs.xcurator.model.Schema;

public interface MappingStep {
  /**
   * Processes the map of schemas.
   *
   * @param root The XML root element of the source document.
   * @param schemas The map of processed schemas. Note that the step should only
   *    modify the schemas map.
   */
  void process(Element root, Map<String, Schema> schemas);
}
