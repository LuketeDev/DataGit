package com.lukete.datagit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ProjectLocatorTest {

    @Test
    void should_resolve_config_and_snapshots_inside_datagit_directory() {
        ProjectLocator locator = new ProjectLocator();

        assertThat(locator.getConfigFile()).endsWith(Path.of(".datagit", "config.yml"));
        assertThat(locator.getSnapshotsDirectory()).endsWith(Path.of(".datagit", "snapshots"));
    }
}
