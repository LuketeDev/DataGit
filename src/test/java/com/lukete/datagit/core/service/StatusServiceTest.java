package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;

class StatusServiceTest {
    @Test
    void shouldCompareCurrentExtractedSnapshotAgainstHead() {
        CountingStorage storage = new CountingStorage();
        storage.save(snapshot("head", List.of(
                Map.of("id", 1, "name", "before"),
                Map.of("id", 2, "name", "deleted"))));
        DataSourceAdapter adapter = () -> snapshot("current", List.of(
                Map.of("id", 1, "name", "after"),
                Map.of("id", 3, "name", "inserted")));

        StatusService service = new StatusService(
                adapter,
                new ReferenceResolver(storage),
                new DiffService(new SnapshotNormalizer(), config()),
                new SnapshotNormalizer(),
                config());

        DiffResult result = service.getStatus();

        assertThat(result.oldId()).isEqualTo("head");
        assertThat(result.newId()).isEqualTo("current");
        assertThat(result.tables().get("users").updated()).hasSize(1);
        assertThat(result.tables().get("users").deleted()).hasSize(1);
        assertThat(result.tables().get("users").inserted()).hasSize(1);
    }

    @Test
    void shouldNotSaveNewSnapshot() {
        CountingStorage storage = new CountingStorage();
        storage.save(snapshot("head", List.of(Map.of("id", 1, "name", "same"))));
        int saveCountAfterSetup = storage.saveCount;
        DataSourceAdapter adapter = () -> snapshot("current", List.of(Map.of("id", 1, "name", "same")));

        StatusService service = new StatusService(
                adapter,
                new ReferenceResolver(storage),
                new DiffService(new SnapshotNormalizer(), config()),
                new SnapshotNormalizer(),
                config());

        service.getStatus();

        assertThat(storage.saveCount).isEqualTo(saveCountAfterSetup);
    }

    private static Snapshot snapshot(String id, List<Map<String, Object>> rows) {
        return new Snapshot(
                id,
                Instant.parse("2026-04-08T10:00:00Z"),
                "postgres",
                Map.of("users", rows));
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
}
