package com.lukete.datagit.core.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lukete.datagit.cli.error.ErrorRenderer;
import com.lukete.datagit.core.exception.ConfigCreationException;

import lombok.RequiredArgsConstructor;

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

    public void setupConfig(boolean isVerbose) {
        File rootDir = new File(".datagit");
        File configFile = new File(".datagit/config.yml");
        File snapshotsDir = new File(".datagit/snapshots");
        if (configFile.exists()) {
            ErrorRenderer.render(new ConfigCreationException("[!!] Config file already exists."), isVerbose);
        }
        if (snapshotsDir.exists()) {
            ErrorRenderer.render(new ConfigCreationException("[!!] Snapshot directory exists."), isVerbose);
            snapshotsDirExists = true;
        }
        if (rootDir.exists()) {
            ErrorRenderer.render(new ConfigCreationException("[!!] DataGit already initialized in this directory."),
                    isVerbose);
        }
        if (!snapshotsDir.mkdirs()) {
            ErrorRenderer.render(new ConfigCreationException("[!!] Failed to create .datagit/snapshots directory"),
                    isVerbose);
            if (!snapshotsDirExists) {
                return;
            }
        }
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write(DEFAULT_CONFIG);
        } catch (IOException e) {
            ErrorRenderer.render(new ConfigCreationException("Failed to write config file: ", e), isVerbose);
        }
    }
}
