package edu.toronto.cs.xcurator.cli.config;

import edu.toronto.cs.xcurator.common.RdfUriConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class RunConfig implements RdfUriConfig {

    private Configuration config;

    // From setting file
    final String resourceUriBase;
    final String typeResourceUriBase;
    final String typeResourcePrefix;
    final String propertyResourceUriBase;
    final String propertyResourcePrefix;

    // From run time setting
    String domain;
    String tdbDirectory;

    public RunConfig(String domain) throws Exception {

        config = new PropertiesConfiguration("setting.properties");

        // We have to check if this domain is a valid uri before initializing
        // the config
        this.domain = domain.endsWith("/")
                ? domain.substring(0, domain.length() - 1)
                : domain;

        resourceUriBase = this.domain + config.getString("rdf.uribase", "/resource");

        String typeUriBase = config.getString("rdf.type.uribase",
                "/resource/class");
        String propertyUriBase = config.getString("rdf.property.uribase",
                "/resource/property");

        if (typeUriBase.endsWith("/") || propertyUriBase.endsWith("/")) {
            throw new Exception("Do not add \"/\" at the end of URI base.");
        }

        typeResourceUriBase = this.domain + typeUriBase;

        typeResourcePrefix = config.getString("rdf.type.prefix",
                "class");

        propertyResourceUriBase = this.domain + propertyUriBase;

        propertyResourcePrefix = config.getString("rdf.property.prefix",
                "property");
    }

    @Override
    public String getResourceUriBase() {
        return resourceUriBase;
    }

    @Override
    public String getTypeResourceUriBase() {
        return typeResourceUriBase;
    }

    @Override
    public String getPropertyResourceUriBase() {
        return propertyResourceUriBase;
    }

    @Override
    public String getTypeResourcePrefix() {
        return typeResourcePrefix;
    }

    @Override
    public String getPropertyResourcePrefix() {
        return propertyResourcePrefix;
    }

}
