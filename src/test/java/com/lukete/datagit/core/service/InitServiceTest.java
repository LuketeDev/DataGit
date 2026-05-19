package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.core.exception.ConfigCreationException;

class InitServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldCreateDefaultConfigAndEmitPerformanceLog() {
        CliPrinter printer = mock(CliPrinter.class);

        new InitService(printer, tempDir.toFile()).setupConfig();

        assertThat(tempDir.resolve(".datagit/snapshots")).isDirectory();
        assertThat(tempDir.resolve(".datagit/config.yml"))
                .exists()
                .content()
                .contains("database:", "storage:", "snapshot:", "ignoredColumns:");
        verify(printer).performance(org.mockito.ArgumentMatchers.contains("Configuration generated in"));
    }

    @Test
    void shouldPropagateConfigCreationExceptionWhenAlreadyInitialized() throws Exception {
        java.nio.file.Files.createDirectories(tempDir.resolve(".datagit"));
        CliPrinter printer = mock(CliPrinter.class);

        assertThatThrownBy(() -> new InitService(printer, tempDir.toFile()).setupConfig())
                .isInstanceOf(ConfigCreationException.class)
                .hasMessageContaining("already initialized");

        verify(printer, never()).performance(org.mockito.ArgumentMatchers.anyString());
    }
}
