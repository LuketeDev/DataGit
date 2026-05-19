package com.lukete.datagit.bootstrap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.config.ConfigLoader;
import com.lukete.datagit.config.ConfigValidator;
import com.lukete.datagit.config.ProjectLocator;
import com.lukete.datagit.config.domain.DataGitConfig;

public class DataGitContextProvider {
    private DataGitContext context;

    public DataGitContext get(CliPrinter printer) {
        if (context == null) {
            context = bootstrap(printer);
        }
        return context;
    }

    private DataGitContext bootstrap(CliPrinter printer) {

        var projectLocator = new ProjectLocator();
        projectLocator.validateProjectInitialized();

        var configLoader = new ConfigLoader(new ObjectMapper(new YAMLFactory()));
        var config = configLoader.load(projectLocator.getConfigFile());

        var configValidator = new ConfigValidator();
        configValidator.validateConfig(config);

        var datasource = createDataSource(config);
        var jdbcTemplate = new JdbcTemplate(datasource);
        var transactionManager = new DataSourceTransactionManager(datasource);

        return new DataGitContext(config, jdbcTemplate, transactionManager, printer);
    }

    private DriverManagerDataSource createDataSource(DataGitConfig config) {
        var database = config.getDatabaseConfig();

        var datasource = new DriverManagerDataSource();
        datasource.setUrl(buildJdbcUrl(config));
        datasource.setUsername(database.getUsername());
        datasource.setPassword(database.getPassword());

        return datasource;
    }

    private String buildJdbcUrl(DataGitConfig config) {
        var db = config.getDatabaseConfig();

        return "jdbc:postgresql://"
                + db.getHost() + ":"
                + db.getPort() + "/"
                + db.getName();
    }
}
