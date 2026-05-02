package com.lukete.datagit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lukete.datagit.config.exception.ConfigFileNotFoundException;
import com.lukete.datagit.config.exception.InvalidConfigException;

class ConfigLoaderTest {

  @TempDir
  Path tempDir;

  @Test
  void should_load_datagit_yaml_sections_into_config_fields() throws Exception {
    Path configFile = tempDir.resolve("config.yml");
    Files.writeString(configFile, """
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
    Files.writeString(configFile, """
        snapshot:
          ignoredColmns:
            - created_at
        """);

    var config = new ConfigLoader(new ObjectMapper(new YAMLFactory())).load(configFile);

    assertThat(config.getSnapshotConfig().getIgnoredColumns()).containsExactly("created_at");
  }

  @Test
  void shouldThrowConfigFileNotFoundWhenConfigFileDoesNotExist() {
    Path missing = tempDir.resolve("missing-config.yml");

    assertThatThrownBy(() -> loader().load(missing))
        .isInstanceOf(ConfigFileNotFoundException.class);
  }

  @Test
  void shouldThrowInvalidConfigWhenYamlIsInvalid() throws Exception {
    Path configFile = tempDir.resolve("config.yml");
    Files.writeString(configFile, "database: [");

    assertThatThrownBy(() -> loader().load(configFile))
        .isInstanceOf(InvalidConfigException.class);
  }

  private static ConfigLoader loader() {
    return new ConfigLoader(new ObjectMapper(new YAMLFactory()));
  }
}
