package com.lukete.datagit.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lukete.datagit.config.exception.ProjectNotInitializedException;

public class ProjectLocator {
    private static final String DATAGIT_DIRECTORY_NAME = ".datagit";
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final String SNAPSHOTS_DIRECTORY_NAME = "snapshots";

    public Path getWorkingDirectory() {
        return Paths.get("").toAbsolutePath().normalize();
    }

    public Path getDataGitDirectory() {
        return getWorkingDirectory().resolve(DATAGIT_DIRECTORY_NAME);
    }

    public Path getConfigFile() {
        return getDataGitDirectory().resolve(CONFIG_FILE_NAME);
    }

    public Path getSnapshotsDirectory() {
        return getDataGitDirectory().resolve(SNAPSHOTS_DIRECTORY_NAME);
    }

    public void validateProjectInitialized() {
        if (!Files.exists(getDataGitDirectory()) || !Files.isDirectory(getDataGitDirectory())) {
            throw new ProjectNotInitializedException();
        }
    }
}
