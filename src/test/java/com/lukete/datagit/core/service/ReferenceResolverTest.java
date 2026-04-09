package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.AmbiguousReferenceException;
import com.lukete.datagit.core.exception.NoSnapshotsFoundException;
import com.lukete.datagit.core.ports.SnapshotStorage;

public class ReferenceResolverTest {

    @Test
    void should_resolve_head_to_latest_snapshot() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(new Snapshot("id-1", Instant.parse("2026-04-08T10:00:00Z"), "postgres", Map.of()));
        storage.save(new Snapshot("id-2", Instant.parse("2026-04-08T11:00:00Z"), "postgres", Map.of()));

        ReferenceResolver resolver = new ReferenceResolver(storage);

        Snapshot result = resolver.resolve("HEAD");

        assertThat(result.id()).isEqualTo("id-2");
    }

    @Test
    void should_throw_when_no_snapshots_exist() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        ReferenceResolver resolver = new ReferenceResolver(storage);

        assertThatThrownBy(() -> resolver.resolve("HEAD"))
                .isInstanceOf(NoSnapshotsFoundException.class);
    }

    @Test
    void should_throw_when_short_id_is_ambiguous() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(new Snapshot("90e47999-b235", Instant.now(), "postgres", Map.of()));
        storage.save(new Snapshot("90e41234-aaaa", Instant.now().plusSeconds(1), "postgres", Map.of()));
        ReferenceResolver resolver = new ReferenceResolver(storage);

        assertThatThrownBy(() -> resolver.resolve("90e4"))
                .isInstanceOf(AmbiguousReferenceException.class);

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
