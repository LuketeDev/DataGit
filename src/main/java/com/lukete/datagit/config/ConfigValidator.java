package com.lukete.datagit.config;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.DatabaseConfig;
import com.lukete.datagit.config.domain.DatabaseType;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.config.domain.StorageConfig;
import com.lukete.datagit.config.domain.StorageType;
import com.lukete.datagit.config.exception.InvalidConfigException;

/**
 * Validates DataGit configuration before application startup
 */

public class ConfigValidator {
    public void validateConfig(DataGitConfig config) {
        if (config == null) {
            throw new InvalidConfigException("Config file is empty");
        }

        validateDatabase(config.getDatabaseConfig());
        validateStorage(config.getStorageConfig());
        validateSnapshot(config.getSnapshotConfig());
    }

    private void validateDatabase(DatabaseConfig databaseConfig) {
        if (databaseConfig == null) {
            throw new InvalidConfigException("Missing required section: database");
        }

        requireNotBlank(databaseConfig.getType(), "database.type");

        // Just for the checks
        DatabaseType.from(databaseConfig.getType());

        requireNotBlank(databaseConfig.getHost(), "database.host");

        if (databaseConfig.getPort() == null || databaseConfig.getPort() <= 0) {
            throw new InvalidConfigException("database.port must be greater than 0");
        }
        requireNotBlank(databaseConfig.getName(), "database.name");
        requireNotBlank(databaseConfig.getUsername(), "database.username");
        requireNotBlank(databaseConfig.getPassword(), "database.password");
    }

    private void validateStorage(StorageConfig storageConfig) {
        if (storageConfig == null) {
            throw new InvalidConfigException("Missing required section: storage");
        }

        requireNotBlank(storageConfig.getType(), "storage.type");

        // Just for the checks
        StorageType.from(storageConfig.getType());

        requireNotBlank(storageConfig.getPath(), "storage.path");
    }

    private void validateSnapshot(SnapshotConfig snapshotConfig) {
        if (snapshotConfig == null) {
            return;
        }
        if (snapshotConfig.getIgnoredColumns() == null) {
            return;
        }

        for (String column : snapshotConfig.getIgnoredColumns()) {
            requireNotBlank(column, "snapshot.ignoredColumns contains a blank value.");
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidConfigException(fieldName + " must not be blank");
        }
    }
}
