package com.lukete.datagit.core;

import static org.assertj.core.api.Assertions.assertThat;
import static com.lukete.datagit.support.TestSnapshots.schemaFor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.snapshot.Snapshot;

class SnapshotTests {

    @Test
    void should_hold_snapshot_tables() {
        Snapshot snapshot = new Snapshot(
                "snapshot-1",
                Instant.parse("2026-04-08T10:00:00Z"),
                "postgres",
                Map.of("users", List.of(Map.of("id", 1, "name", "Lucas"))),
                schemaFor(Map.of("users", List.of(Map.of("id", 1, "name", "Lucas")))));

        assertThat(snapshot.tables()).isNotEmpty();
    }
}
