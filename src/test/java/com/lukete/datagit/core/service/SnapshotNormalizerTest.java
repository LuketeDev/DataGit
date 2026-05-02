package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.Snapshot;

class SnapshotNormalizerTest {
    private final SnapshotNormalizer normalizer = new SnapshotNormalizer();

    @Test
    void shouldReturnSameSnapshotWhenIgnoredColumnsIsNull() {
        Snapshot snapshot = snapshot();

        Snapshot normalized = normalizer.normalize(snapshot, null);

        assertThat(normalized).isSameAs(snapshot);
    }

    @Test
    void shouldReturnSameSnapshotWhenIgnoredColumnsIsEmpty() {
        Snapshot snapshot = snapshot();

        Snapshot normalized = normalizer.normalize(snapshot, List.of());

        assertThat(normalized).isSameAs(snapshot);
    }

    @Test
    void shouldRemoveIgnoredColumnsFromAllRows() {
        Snapshot normalized = normalizer.normalize(snapshot(), List.of("updated_at"));

        assertThat(normalized.tables().get("users"))
                .allSatisfy(row -> assertThat(row).doesNotContainKey("updated_at"));
    }

    @Test
    void shouldNotMutateOriginalSnapshot() {
        Snapshot snapshot = snapshot();

        normalizer.normalize(snapshot, List.of("updated_at"));

        assertThat(snapshot.tables().get("users"))
                .allSatisfy(row -> assertThat(row).containsKey("updated_at"));
    }

    @Test
    void shouldHandleMultipleTables() {
        Snapshot normalized = normalizer.normalize(snapshot(), List.of("updated_at"));

        assertThat(normalized.tables().get("users").getFirst()).doesNotContainKey("updated_at");
        assertThat(normalized.tables().get("orders").getFirst()).doesNotContainKey("updated_at");
        assertThat(normalized.tables().get("orders").getFirst()).containsEntry("total", 99);
    }

    @Test
    void shouldIgnoreBlankAndNullIgnoredColumnEntriesSafely() {
        Snapshot normalized = normalizer.normalize(snapshot(), new ArrayList<>(Arrays.asList("updated_at", "", null, "   ")));

        assertThat(normalized.tables().get("users").getFirst()).doesNotContainKey("updated_at");
        assertThat(normalized.tables().get("users").getFirst()).containsEntry("name", "Lucas");
    }

    @Test
    void shouldPreserveSnapshotMetadata() {
        Snapshot snapshot = snapshot();

        Snapshot normalized = normalizer.normalize(snapshot, List.of("updated_at"));

        assertThat(normalized.id()).isEqualTo(snapshot.id());
        assertThat(normalized.timestamp()).isEqualTo(snapshot.timestamp());
        assertThat(normalized.source()).isEqualTo(snapshot.source());
    }

    private static Snapshot snapshot() {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 1);
        user.put("name", "Lucas");
        user.put("updated_at", "now");

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("id", 10);
        order.put("total", 99);
        order.put("updated_at", "later");

        return new Snapshot(
                "snapshot-1",
                Instant.parse("2026-04-08T10:00:00Z"),
                "postgres",
                Map.of(
                        "users", List.of(user),
                        "orders", List.of(order)));
    }
}
