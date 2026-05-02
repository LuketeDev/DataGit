package com.lukete.datagit.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.DatabaseConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.config.domain.StorageConfig;
import com.lukete.datagit.config.exception.InvalidConfigException;
import com.lukete.datagit.config.exception.UnsupportedDatabaseTypeException;
import com.lukete.datagit.config.exception.UnsupportedStorageTypeException;

class ConfigValidatorTest {
    private final ConfigValidator validator = new ConfigValidator();

    @Test
    void shouldPassForValidConfig() {
        assertThatCode(() -> validator.validateConfig(validConfig()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldFailForNullConfig() {
        assertInvalid(null);
    }

    @Test
    void shouldFailForMissingDatabaseSection() {
        DataGitConfig config = validConfig();
        config.setDatabaseConfig(null);

        assertInvalid(config);
    }

    @Test
    void shouldFailForMissingStorageSection() {
        DataGitConfig config = validConfig();
        config.setStorageConfig(null);

        assertInvalid(config);
    }

    @Test
    void shouldFailForBlankDatabaseType() {
        assertInvalidDatabase(database -> database.setType(" "));
    }

    @Test
    void shouldFailForUnsupportedDatabaseType() {
        assertThatThrownBy(() -> validator.validateConfig(configWithDatabase(database -> database.setType("mysql"))))
                .isInstanceOf(UnsupportedDatabaseTypeException.class);
    }

    @Test
    void shouldFailForBlankDatabaseHost() {
        assertInvalidDatabase(database -> database.setHost(""));
    }

    @Test
    void shouldFailForNullDatabasePort() {
        assertInvalidDatabase(database -> database.setPort(null));
    }

    @Test
    void shouldFailForInvalidDatabasePort() {
        assertInvalidDatabase(database -> database.setPort(0));
    }

    @Test
    void shouldFailForBlankDatabaseName() {
        assertInvalidDatabase(database -> database.setName(" "));
    }

    @Test
    void shouldFailForBlankDatabaseUsername() {
        assertInvalidDatabase(database -> database.setUsername(""));
    }

    @Test
    void shouldFailForBlankDatabasePassword() {
        assertInvalidDatabase(database -> database.setPassword(" "));
    }

    @Test
    void shouldFailForBlankStorageType() {
        assertInvalidStorage(storage -> storage.setType(""));
    }

    @Test
    void shouldFailForUnsupportedStorageType() {
        assertThatThrownBy(() -> validator.validateConfig(configWithStorage(storage -> storage.setType("s3"))))
                .isInstanceOf(UnsupportedStorageTypeException.class);
    }

    @Test
    void shouldFailForBlankStoragePath() {
        assertInvalidStorage(storage -> storage.setPath(" "));
    }

    @Test
    void shouldAllowNullIgnoredColumns() {
        DataGitConfig config = validConfig();
        config.getSnapshotConfig().setIgnoredColumns(null);

        assertThatCode(() -> validator.validateConfig(config))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldFailForBlankIgnoredColumnItem() {
        DataGitConfig config = validConfig();
        config.getSnapshotConfig().setIgnoredColumns(new ArrayList<>(List.of("updated_at", "")));

        assertInvalid(config);
    }

    private void assertInvalidDatabase(Consumer<DatabaseConfig> mutation) {
        assertInvalid(configWithDatabase(mutation));
    }

    private void assertInvalidStorage(Consumer<StorageConfig> mutation) {
        assertInvalid(configWithStorage(mutation));
    }

    private void assertInvalid(DataGitConfig config) {
        assertThatThrownBy(() -> validator.validateConfig(config))
                .isInstanceOf(InvalidConfigException.class);
    }

    private static DataGitConfig configWithDatabase(Consumer<DatabaseConfig> mutation) {
        DataGitConfig config = validConfig();
        mutation.accept(config.getDatabaseConfig());
        return config;
    }

    private static DataGitConfig configWithStorage(Consumer<StorageConfig> mutation) {
        DataGitConfig config = validConfig();
        mutation.accept(config.getStorageConfig());
        return config;
    }

    private static DataGitConfig validConfig() {
        DatabaseConfig database = new DatabaseConfig();
        database.setType("postgres");
        database.setHost("localhost");
        database.setPort(5432);
        database.setName("datagit");
        database.setUsername("postgres");
        database.setPassword("postgres");

        StorageConfig storage = new StorageConfig();
        storage.setType("filesystem");
        storage.setPath(".datagit/snapshots");

        SnapshotConfig snapshot = new SnapshotConfig();
        snapshot.setIgnoredColumns(List.of("updated_at"));

        DataGitConfig config = new DataGitConfig();
        config.setDatabaseConfig(database);
        config.setStorageConfig(storage);
        config.setSnapshotConfig(snapshot);
        return config;
    }
}
