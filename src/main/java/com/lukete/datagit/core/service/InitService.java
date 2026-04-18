package com.lukete.datagit.core.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.lukete.datagit.cli.error.ErrorRenderer;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.core.exception.ConfigCreationException;

import lombok.RequiredArgsConstructor;

/**
 * Creates the initial DataGit configuration and snapshot storage directories.
 */
@RequiredArgsConstructor
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
              ignoredColmns:
                - updated_at
                - created_at
            """;

    private boolean snapshotsDirExists;
    private final CliPrinter printer;

    /**
     * Creates the {@code .datagit} directory structure and writes the default
     * configuration file.
     *
     * @param isVerbose whether initialization errors should include verbose details
     */
    public void setupConfig(boolean isVerbose) {
        File rootDir = new File(".datagit");
        File configFile = new File(".datagit/config.yml");
        File snapshotsDir = new File(".datagit/snapshots");
        ErrorRenderer renderer = new ErrorRenderer(printer);
        if (configFile.exists()) {
            renderer.render(new ConfigCreationException("[!!] Config file already exists."), isVerbose);
        }
        if (snapshotsDir.exists()) {
            renderer.render(new ConfigCreationException("[!!] Snapshot directory exists."), isVerbose);
            snapshotsDirExists = true;
        }
        if (rootDir.exists()) {
            renderer.render(new ConfigCreationException("[!!] DataGit already initialized in this directory."),
                    isVerbose);
        }
        if (!snapshotsDir.mkdirs()) {
            renderer.render(new ConfigCreationException("[!!] Failed to create .datagit/snapshots directory"),
                    isVerbose);
            if (!snapshotsDirExists) {
                return;
            }
        }
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write(DEFAULT_CONFIG);
        } catch (IOException e) {
            renderer.render(new ConfigCreationException("Failed to write config file: ", e), isVerbose);
        }
    }
}
