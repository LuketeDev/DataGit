package com.lukete.datagit.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lukete.datagit.config.ConfigLoader;
import com.lukete.datagit.config.ConfigValidator;
import com.lukete.datagit.config.ProjectLocator;

public class DataGitContextProvider {
    private DataGitContext context;

    public DataGitContext get() {
        if (context == null) {
            context = bootstrap();
        }
        return context;
    }

    private DataGitContext bootstrap() {
        var projectLocator = new ProjectLocator();
        projectLocator.validateProjectInitialized();

        var configLoader = new ConfigLoader(new ObjectMapper(new YAMLFactory()));
        var config = configLoader.load(projectLocator.getConfigFile());

        var configValidator = new ConfigValidator();
        configValidator.validateConfig(config);

        return new DataGitContext(config);
    }

}
