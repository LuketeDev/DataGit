package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.AmbiguousReferenceException;
import com.lukete.datagit.core.exception.SnapshotNotFoundException;
import com.lukete.datagit.core.exception.NoSnapshotsFoundException;
import com.lukete.datagit.core.ports.SnapshotStorage;

class ReferenceResolverTest {

    @Test
    void shouldResolveHeadToLatestSnapshot() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(snapshot("id-1", "2026-04-08T10:00:00Z"));
        storage.save(snapshot("id-2", "2026-04-08T11:00:00Z"));

        Snapshot result = new ReferenceResolver(storage).resolve("HEAD");

        assertThat(result.id()).isEqualTo("id-2");
    }

    @Test
    void shouldResolveHeadOneToPreviousSnapshot() {
        InMemorySnapshotStorage storage = storageWithThreeSnapshots();

        Snapshot result = new ReferenceResolver(storage).resolve("HEAD~1");

        assertThat(result.id()).isEqualTo("id-2");
    }

    @Test
    void shouldResolveHeadTwoToOlderSnapshot() {
        InMemorySnapshotStorage storage = storageWithThreeSnapshots();

        Snapshot result = new ReferenceResolver(storage).resolve("HEAD~2");

        assertThat(result.id()).isEqualTo("id-1");
    }

    @Test
    void shouldResolveFullId() {
        InMemorySnapshotStorage storage = storageWithThreeSnapshots();

        Snapshot result = new ReferenceResolver(storage).resolve("id-2");

        assertThat(result.id()).isEqualTo("id-2");
    }

    @Test
    void shouldResolveShortUniqueId() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(snapshot("90e47999-b235", "2026-04-08T10:00:00Z"));
        storage.save(snapshot("abc12345-aaaa", "2026-04-08T11:00:00Z"));

        Snapshot result = new ReferenceResolver(storage).resolve("90e4");

        assertThat(result.id()).isEqualTo("90e47999-b235");
    }

    @Test
    void shouldThrowWhenShortIdIsUnknown() {
        InMemorySnapshotStorage storage = storageWithThreeSnapshots();

        assertThatThrownBy(() -> new ReferenceResolver(storage).resolve("missing"))
                .isInstanceOf(SnapshotNotFoundException.class);
    }

    @Test
    void shouldThrowWhenShortIdIsAmbiguous() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(snapshot("90e47999-b235", "2026-04-08T10:00:00Z"));
        storage.save(snapshot("90e41234-aaaa", "2026-04-08T11:00:00Z"));
        ReferenceResolver resolver = new ReferenceResolver(storage);

        assertThatThrownBy(() -> resolver.resolve("90e4"))
                .isInstanceOf(AmbiguousReferenceException.class);

    }

    @Test
    void shouldThrowWhenNoSnapshotsExist() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        ReferenceResolver resolver = new ReferenceResolver(storage);

        assertThatThrownBy(() -> resolver.resolve("HEAD"))
                .isInstanceOf(NoSnapshotsFoundException.class);
    }

    private static InMemorySnapshotStorage storageWithThreeSnapshots() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(snapshot("id-1", "2026-04-08T10:00:00Z"));
        storage.save(snapshot("id-2", "2026-04-08T11:00:00Z"));
        storage.save(snapshot("id-3", "2026-04-08T12:00:00Z"));
        return storage;
    }

    private static Snapshot snapshot(String id, String timestamp) {
        return new Snapshot(id, Instant.parse(timestamp), "postgres", Map.of());
    }

    private static class InMemorySnapshotStorage implements SnapshotStorage {
        private final List<Snapshot> snapshots = new ArrayList<>();

        @Override
        public void save(Snapshot snapshot) {
            snapshots.add(snapshot);
        }

        @Override
        public Optional<Snapshot> load(String id) {
            return snapshots.stream()
                    .filter(snapshot -> snapshot.id().equals(id))
                    .findFirst();
        }

        @Override
        public List<Snapshot> list() {
            return new ArrayList<>(snapshots);
        }

    }
}
