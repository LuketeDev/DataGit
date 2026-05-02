package com.lukete.datagit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lukete.datagit.config.exception.ProjectNotInitializedException;

class ProjectLocatorTest {
    @TempDir
    Path tempDir;

    @Test
    void should_resolve_config_and_snapshots_inside_datagit_directory() {
        ProjectLocator locator = new ProjectLocator(tempDir);

        assertThat(locator.getConfigFile()).isEqualTo(tempDir.resolve(".datagit/config.yml"));
        assertThat(locator.getSnapshotsDirectory()).isEqualTo(tempDir.resolve(".datagit/snapshots"));
    }

    @Test
    void shouldDetectInitializedProjectWhenDatagitDirectoryExists() throws Exception {
        Files.createDirectory(tempDir.resolve(".datagit"));
        ProjectLocator locator = new ProjectLocator(tempDir);

        assertThatCode(locator::validateProjectInitialized)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenDatagitDirectoryDoesNotExist() {
        ProjectLocator locator = new ProjectLocator(tempDir);

        assertThatThrownBy(locator::validateProjectInitialized)
                .isInstanceOf(ProjectNotInitializedException.class);
    }

    @Test
    void shouldResolveConfigFilePath() {
        ProjectLocator locator = new ProjectLocator(tempDir);

        assertThat(locator.getConfigFile()).isEqualTo(tempDir.resolve(".datagit").resolve("config.yml"));
    }

    @Test
    void shouldResolveSnapshotsDirectoryPath() {
        ProjectLocator locator = new ProjectLocator(tempDir);

        assertThat(locator.getSnapshotsDirectory()).isEqualTo(tempDir.resolve(".datagit").resolve("snapshots"));
    }
}
