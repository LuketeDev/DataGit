package com.lukete.datagit.config.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Root configuration object for datagit
 */

@Getter
@Setter
public class DataGitConfig {
    private DatabaseConfig databaseConfig;
    private StorageConfig storageConfig;
    private SnapshotConfig snapshotConfig;
}
