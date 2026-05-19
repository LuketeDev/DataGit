package com.lukete.datagit.core.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.core.exception.ConfigCreationException;

/**
 * Creates the initial DataGit configuration and snapshot storage directories.
 */
public class InitService {
    private static final String DEFAULT_CONFIG = """
            database:
              type: postgres
              host: localhost
              port: 5432
              name: my_database
              username: postgres
              password: postgres

            storage:
              type: filesystem
              path: .datagit/snapshots

            snapshot:
              ignoredColumns:
                - updated_at
                - created_at
            """;

    private final CliPrinter printer;
    private final File baseDir;

    public InitService(CliPrinter printer) {
        this(printer, new File("."));
    }

    InitService(CliPrinter printer, File baseDir) {
        this.printer = printer;
        this.baseDir = baseDir;
    }

    /**
     * Creates the {@code .datagit} directory structure and writes the default
     * configuration file.
     */
    public void setupConfig() {
        Stopwatch totalStopwatch = Stopwatch.start();
        File rootDir = new File(baseDir, ".datagit");
        File configFile = new File(rootDir, "config.yml");
        File snapshotsDir = new File(rootDir, "snapshots");

        if (rootDir.exists()) {
            throw new ConfigCreationException("DataGit already initialized in this directory.");
        }

        if (!snapshotsDir.mkdirs()) {
            throw new ConfigCreationException("Failed to create .datagit/snapshots directory.");
        }

        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write(DEFAULT_CONFIG);
        } catch (IOException e) {
            throw new ConfigCreationException("Failed to write config file.", e);
        }
        printer.performance("Configuration generated in " + totalStopwatch.elapsedMillis() + "ms");
    }
}
