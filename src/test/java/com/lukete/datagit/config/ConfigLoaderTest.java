package com.lukete.datagit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class ConfigLoaderTest {

  @TempDir
  Path tempDir;

  @Test
  void should_load_datagit_yaml_sections_into_config_fields() throws Exception {
    Path configFile = tempDir.resolve("config.yml");
    java.nio.file.Files.writeString(configFile, """
        database:
          type: postgres
          host: localhost
          port: 5432
          name: datagit
          username: postgres
          password: postgres

        storage:
          type: filesystem
          path: .datagit/snapshots

        snapshot:
          ignoredColumns:
            - updated_at
        """);

    var config = new ConfigLoader(new ObjectMapper(new YAMLFactory())).load(configFile);

    assertThat(config.getDatabaseConfig().getName()).isEqualTo("datagit");
    assertThat(config.getStorageConfig().getPath()).isEqualTo(".datagit/snapshots");
    assertThat(config.getSnapshotConfig().getIgnoredColumns()).containsExactly("updated_at");
  }

  @Test
  void should_load_legacy_misspelled_ignored_columns_key() throws Exception {
    Path configFile = tempDir.resolve("config.yml");
    java.nio.file.Files.writeString(configFile, """
        snapshot:
          ignoredColmns:
            - created_at
        """);

    var config = new ConfigLoader(new ObjectMapper(new YAMLFactory())).load(configFile);

    assertThat(config.getSnapshotConfig().getIgnoredColumns()).containsExactly("created_at");
  }
}
