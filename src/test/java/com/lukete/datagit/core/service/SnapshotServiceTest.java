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
    void should_create_and_store_snapshot() {
        FakeAdapter fakeAdapter = new FakeAdapter();
        FakeStorage fakeStorage = new FakeStorage();

        SnapshotService service = new SnapshotService(fakeAdapter, fakeStorage, new SnapshotNormalizer(),
                createConfig());
        Snapshot created = service.createSnapshot();

        assertThat(created.id()).isNotBlank();
        assertThat(created.timestamp()).isNotNull();
        assertThat(created.source()).isEqualTo("postgres");
        assertThat(fakeStorage.savedSnapshot).isNotNull();
        assertThat(fakeStorage.savedSnapshot.id()).isEqualTo(created.id());
    }

    private static class FakeAdapter implements DataSourceAdapter {
        @Override
        public Snapshot extract() {
            return new Snapshot(
                    null,
                    null,
                    "postgres",
                    Map.of("users", List.of(Map.of("id", 1, "name", "Lucas"))));
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
