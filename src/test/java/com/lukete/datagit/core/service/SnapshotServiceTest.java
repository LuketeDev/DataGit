package com.lukete.datagit.core.service;

import static com.lukete.datagit.support.TestSnapshots.schemaFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private DataSourceAdapter adapter;

    @Mock
    private SnapshotStorage storage;

    @Mock
    private SnapshotNormalizer normalizer;

    @Mock
    private CliPrinter printer;

    @Mock
    private DataGitConfig config;

    private SnapshotService service;

    @BeforeEach
    void setUp() {
        service = new SnapshotService(adapter, storage, normalizer, config, printer);
    }

    @Test
    void shouldCreateAndPersistSnapshot() {
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        Snapshot normalizedSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(List.of());
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, List.of())).thenReturn(normalizedSnapshot);

        Snapshot created = service.createSnapshot();

        assertThat(created.id()).isNotBlank();
        assertThat(created.timestamp()).isNotNull();
        assertThat(created.source()).isEqualTo(normalizedSnapshot.source());
        assertThat(created.tables()).isEqualTo(normalizedSnapshot.tables());
        assertThat(created.schema()).isEqualTo(normalizedSnapshot.schema());

        verify(storage, times(1)).save(created);

        InOrder order = inOrder(adapter, normalizer, storage);
        order.verify(adapter).extract();
        order.verify(normalizer).normalize(rawSnapshot, List.of());
        order.verify(storage).save(created);
    }

    @Test
    void shouldNormalizeSnapshotUsingIgnoredColumns() {
        List<String> ignoredColumns = List.of("updated_at", "created_at");
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas", "updated_at", "now"))));
        Snapshot normalizedSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(ignoredColumns);
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, ignoredColumns)).thenReturn(normalizedSnapshot);

        service.createSnapshot();

        verify(normalizer).normalize(rawSnapshot, ignoredColumns);
    }

    @Test
    void shouldEmitPerformanceLogsForAllStages() {
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        Snapshot normalizedSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(List.of());
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, List.of())).thenReturn(normalizedSnapshot);

        service.createSnapshot();

        verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot extracted in"));
        verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot normalized in"));
        verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot saved in"));
        verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot completed in"));
        verify(printer, times(4)).performance(anyString());
    }

    @Test
    void shouldEmitPerformanceLogsInCorrectOrder() {
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        Snapshot normalizedSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(List.of());
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, List.of())).thenReturn(normalizedSnapshot);

        service.createSnapshot();

        InOrder order = inOrder(printer);
        order.verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot extracted in"));
        order.verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot normalized in"));
        order.verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot saved in"));
        order.verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot completed in"));
    }

    @Test
    void shouldSaveEnrichedSnapshotInsteadOfRawSnapshot() {
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        Snapshot normalizedSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(List.of());
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, List.of())).thenReturn(normalizedSnapshot);
        ArgumentCaptor<Snapshot> snapshotCaptor = ArgumentCaptor.forClass(Snapshot.class);

        service.createSnapshot();

        verify(storage).save(snapshotCaptor.capture());
        Snapshot savedSnapshot = snapshotCaptor.getValue();

        assertThat(savedSnapshot).isNotSameAs(normalizedSnapshot);
        assertThat(savedSnapshot.id()).isNotBlank();
        assertThat(savedSnapshot.timestamp()).isNotNull();
        assertThat(savedSnapshot.source()).isEqualTo(normalizedSnapshot.source());
        assertThat(savedSnapshot.tables()).isEqualTo(normalizedSnapshot.tables());
        assertThat(savedSnapshot.schema()).isEqualTo(normalizedSnapshot.schema());
    }

    @Test
    void shouldPropagateExceptionWhenExtractFails() {
        RuntimeException failure = new RuntimeException("extract failed");
        when(adapter.extract()).thenThrow(failure);

        assertThatThrownBy(service::createSnapshot)
                .isSameAs(failure);

        verify(normalizer, never()).normalize(any(), any());
        verify(storage, never()).save(any());
    }

    @Test
    void shouldPropagateExceptionWhenNormalizeFails() {
        RuntimeException failure = new RuntimeException("normalize failed");
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(List.of("updated_at"));
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, List.of("updated_at"))).thenThrow(failure);

        assertThatThrownBy(service::createSnapshot)
                .isSameAs(failure);

        verify(storage, never()).save(any());
    }

    @Test
    void shouldPropagateExceptionWhenSaveFails() {
        RuntimeException failure = new RuntimeException("save failed");
        Snapshot rawSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        Snapshot normalizedSnapshot = snapshot("postgres", Map.of(
                "users", List.of(Map.of("id", 1, "name", "Lucas"))));
        givenIgnoredColumns(List.of());
        when(adapter.extract()).thenReturn(rawSnapshot);
        when(normalizer.normalize(rawSnapshot, List.of())).thenReturn(normalizedSnapshot);
        doThrow(failure).when(storage).save(any(Snapshot.class));

        assertThatThrownBy(service::createSnapshot)
                .isSameAs(failure);
    }

    private void givenIgnoredColumns(List<String> ignoredColumns) {
        SnapshotConfig snapshotConfig = new SnapshotConfig();
        snapshotConfig.setIgnoredColumns(ignoredColumns);
        when(config.getSnapshotConfig()).thenReturn(snapshotConfig);
    }

    private static Snapshot snapshot(String source, Map<String, List<Map<String, Object>>> tables) {
        return new Snapshot(null, null, source, tables, schemaFor(tables));
    }
}
