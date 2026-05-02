package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;

class SnapshotServiceTest {

    private static DataGitConfig createConfig() {
        DataGitConfig config = new DataGitConfig();
        SnapshotConfig snapshotConfig = new SnapshotConfig();
        snapshotConfig.setIgnoredColumns(List.of());
        config.setSnapshotConfig(snapshotConfig);
        return config;
    }

    @Test
    void shouldCreateSnapshotWithGeneratedIdAndTimestamp() {
        FakeAdapter fakeAdapter = new FakeAdapter(List.of(Map.of("id", 1, "name", "Lucas")));
        FakeStorage fakeStorage = new FakeStorage();

        SnapshotService service = new SnapshotService(fakeAdapter, fakeStorage, new SnapshotNormalizer(),
                createConfig());
        Snapshot created = service.createSnapshot();

        assertThat(created.id()).isNotBlank();
        assertThat(created.timestamp()).isNotNull();
    }

    @Test
    void shouldStoreCreatedSnapshot() {
        FakeAdapter fakeAdapter = new FakeAdapter(List.of(Map.of("id", 1, "name", "Lucas")));
        FakeStorage fakeStorage = new FakeStorage();

        SnapshotService service = new SnapshotService(fakeAdapter, fakeStorage, new SnapshotNormalizer(),
                createConfig());
        Snapshot created = service.createSnapshot();

        assertThat(created.source()).isEqualTo("postgres");
        assertThat(fakeStorage.savedSnapshot).isNotNull();
        assertThat(fakeStorage.savedSnapshot.id()).isEqualTo(created.id());
    }

    @Test
    void shouldPreserveSourceAndTables() {
        FakeAdapter fakeAdapter = new FakeAdapter(List.of(Map.of("id", 1, "name", "Lucas")));
        FakeStorage fakeStorage = new FakeStorage();

        Snapshot created = new SnapshotService(fakeAdapter, fakeStorage, new SnapshotNormalizer(), createConfig())
                .createSnapshot();

        assertThat(created.source()).isEqualTo("postgres");
        assertThat(created.tables()).containsKey("users");
        assertThat(created.tables().get("users")).hasSize(1);
    }

    @Test
    void shouldApplyIgnoredColumnsWhenCreatingSnapshot() {
        DataGitConfig config = createConfig();
        config.getSnapshotConfig().setIgnoredColumns(List.of("updated_at"));
        FakeAdapter fakeAdapter = new FakeAdapter(List.of(Map.of("id", 1, "name", "Lucas", "updated_at", "now")));
        FakeStorage fakeStorage = new FakeStorage();

        Snapshot created = new SnapshotService(fakeAdapter, fakeStorage, new SnapshotNormalizer(), config)
                .createSnapshot();

        assertThat(created.tables().get("users").getFirst()).doesNotContainKey("updated_at");
        assertThat(fakeStorage.savedSnapshot.tables().get("users").getFirst()).doesNotContainKey("updated_at");
    }

    private static class FakeAdapter implements DataSourceAdapter {
        private final List<Map<String, Object>> rows;

        private FakeAdapter(List<Map<String, Object>> rows) {
            this.rows = rows;
        }

        @Override
        public Snapshot extract() {
            return new Snapshot(
                    null,
                    null,
                    "postgres",
                    Map.of("users", rows));
        }
    }

    private static class FakeStorage implements SnapshotStorage {
        private Snapshot savedSnapshot;

        @Override
        public void save(Snapshot snapshot) {
            this.savedSnapshot = snapshot;
        }

        @Override
        public Optional<Snapshot> load(String id) {
            return Optional.ofNullable(savedSnapshot);
        }

        @Override
        public List<Snapshot> list() {
            return savedSnapshot == null ? List.of() : List.of(savedSnapshot);
        }
    }
}
