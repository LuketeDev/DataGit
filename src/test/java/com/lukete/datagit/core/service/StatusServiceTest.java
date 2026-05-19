package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static com.lukete.datagit.support.TestSnapshots.schemaFor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.diff.DiffResult;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;

class StatusServiceTest {
    @Test
    void shouldCompareCurrentExtractedSnapshotAgainstHead() {
        CountingStorage storage = new CountingStorage();
        storage.save(snapshot("head", List.of(
                Map.of("id", 1, "name", "before"),
                Map.of("id", 2, "name", "deleted"))));
        DataSourceAdapter adapter = new ExtractingAdapter(snapshot("current", List.of(
                Map.of("id", 1, "name", "after"),
                Map.of("id", 3, "name", "inserted"))));
        CliPrinter statusPrinter = mock(CliPrinter.class);

        StatusService service = new StatusService(
                adapter,
                new ReferenceResolver(storage),
                new DiffService(new SnapshotNormalizer(), config(), mock(CliPrinter.class)),
                new SnapshotNormalizer(),
                config(),
                statusPrinter);

        DiffResult result = service.getStatus();

        assertThat(result.oldId()).isEqualTo("head");
        assertThat(result.newId()).isEqualTo("current");
        assertThat(result.tables().get("users").updated()).hasSize(1);
        assertThat(result.tables().get("users").deleted()).hasSize(1);
        assertThat(result.tables().get("users").inserted()).hasSize(1);
        verify(statusPrinter).performance(org.mockito.ArgumentMatchers.contains("Status generated in"));
    }

    @Test
    void shouldNotSaveNewSnapshot() {
        CountingStorage storage = new CountingStorage();
        storage.save(snapshot("head", List.of(Map.of("id", 1, "name", "same"))));
        int saveCountAfterSetup = storage.saveCount;
        DataSourceAdapter adapter = new ExtractingAdapter(
                snapshot("current", List.of(Map.of("id", 1, "name", "same"))));
        CliPrinter statusPrinter = mock(CliPrinter.class);

        StatusService service = new StatusService(
                adapter,
                new ReferenceResolver(storage),
                new DiffService(new SnapshotNormalizer(), config(), mock(CliPrinter.class)),
                new SnapshotNormalizer(),
                config(),
                statusPrinter);

        service.getStatus();

        assertThat(storage.saveCount).isEqualTo(saveCountAfterSetup);
    }

    @Test
    void shouldEmitPerformanceLogWhenGeneratingStatus() {
        CountingStorage storage = new CountingStorage();
        storage.save(snapshot("head", List.of(Map.of("id", 1, "name", "same"))));
        DataSourceAdapter adapter = new ExtractingAdapter(
                snapshot("current", List.of(Map.of("id", 1, "name", "same"))));
        CliPrinter statusPrinter = mock(CliPrinter.class);

        StatusService service = new StatusService(
                adapter,
                new ReferenceResolver(storage),
                new DiffService(new SnapshotNormalizer(), config(), mock(CliPrinter.class)),
                new SnapshotNormalizer(),
                config(),
                statusPrinter);

        service.getStatus();

        verify(statusPrinter).performance(org.mockito.ArgumentMatchers.contains("Status generated in"));
    }

    private static Snapshot snapshot(String id, List<Map<String, Object>> rows) {
        return new Snapshot(
                id,
                Instant.parse("2026-04-08T10:00:00Z"),
                "postgres",
                Map.of("users", rows),
                schemaFor(Map.of("users", rows)));
    }

    private static DataGitConfig config() {
        SnapshotConfig snapshot = new SnapshotConfig();
        snapshot.setIgnoredColumns(List.of());
        DataGitConfig config = new DataGitConfig();
        config.setSnapshotConfig(snapshot);
        return config;
    }

    private static class CountingStorage implements SnapshotStorage {
        private final List<Snapshot> snapshots = new ArrayList<>();
        private int saveCount;

        @Override
        public void save(Snapshot snapshot) {
            saveCount++;
            snapshots.add(snapshot);
        }

        @Override
        public Optional<Snapshot> load(String id) {
            return snapshots.stream().filter(snapshot -> snapshot.id().equals(id)).findFirst();
        }

        @Override
        public List<Snapshot> list() {
            return List.copyOf(snapshots);
        }
    }

    private static class ExtractingAdapter implements DataSourceAdapter {
        private final Snapshot snapshot;

        private ExtractingAdapter(Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Snapshot extract() {
            return snapshot;
        }

        @Override
        public SchemaSnapshot extractSchema() {
            return snapshot.schema();
        }

        @Override
        public void restore(Snapshot snapshot) {
        }
    }
}
