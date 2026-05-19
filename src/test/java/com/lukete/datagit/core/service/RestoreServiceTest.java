package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.lukete.datagit.support.TestSnapshots.schemaFor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.exception.RestoreFailedException;
import com.lukete.datagit.core.ports.DataSourceAdapter;

class RestoreServiceTest {

    @Test
    void shouldRestoreSnapshotThroughAdapter() {
        FakeAdapter adapter = new FakeAdapter();
        Snapshot snapshot = snapshot();
        CliPrinter printer = mock(CliPrinter.class);

        new RestoreService(adapter, printer).restore(snapshot);

        assertThat(adapter.restoredSnapshot).isSameAs(snapshot);
    }

    @Test
    void shouldPropagateAdapterExceptions() {
        FakeAdapter adapter = new FakeAdapter();
        adapter.exception = new RestoreFailedException("failed", new RuntimeException("boom"));
        CliPrinter printer = mock(CliPrinter.class);

        assertThatThrownBy(() -> new RestoreService(adapter, printer).restore(snapshot()))
                .isSameAs(adapter.exception);

        verify(printer, never()).performance(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldNotMutateSnapshot() {
        FakeAdapter adapter = new FakeAdapter();
        Snapshot snapshot = snapshot();
        Map<String, List<Map<String, Object>>> originalTables = deepCopy(snapshot.tables());
        CliPrinter printer = mock(CliPrinter.class);

        new RestoreService(adapter, printer).restore(snapshot);

        assertThat(snapshot.tables()).isEqualTo(originalTables);
    }

    @Test
    void shouldEmitPerformanceLogWhenSnapshotIsRestored() {
        FakeAdapter adapter = new FakeAdapter();
        CliPrinter printer = mock(CliPrinter.class);

        new RestoreService(adapter, printer).restore(snapshot());

        verify(printer).performance(org.mockito.ArgumentMatchers.contains("Snapshot restored in"));
    }

    private static Snapshot snapshot() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "Lucas");

        return new Snapshot(
                "snap-1",
                Instant.parse("2026-04-08T10:00:00Z"),
                "postgres",
                Map.of("users", List.of(row)),
                schemaFor(Map.of("users", List.of(row))));
    }

    private static Map<String, List<Map<String, Object>>> deepCopy(Map<String, List<Map<String, Object>>> tables) {
        Map<String, List<Map<String, Object>>> copy = new LinkedHashMap<>();
        tables.forEach((tableName, rows) -> copy.put(tableName, rows.stream()
                .map(row -> Map.copyOf(row))
                .toList()));
        return copy;
    }

    private static class FakeAdapter implements DataSourceAdapter {
        private Snapshot restoredSnapshot;
        private RuntimeException exception;

        @Override
        public Snapshot extract() {
            return null;
        }

        @Override
        public SchemaSnapshot extractSchema() {
            return null;
        }

        @Override
        public void restore(Snapshot snapshot) {
            if (exception != null) {
                throw exception;
            }
            this.restoredSnapshot = snapshot;
        }
    }
}
