package com.lukete.datagit.config.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Root configuration object for datagit
 */

@Getter
@Setter
public class DataGitConfig {
    @JsonProperty("database")
    private DatabaseConfig databaseConfig;
    @JsonProperty("storage")
    private StorageConfig storageConfig;
    @JsonProperty("snapshot")
    private SnapshotConfig snapshotConfig;
}
