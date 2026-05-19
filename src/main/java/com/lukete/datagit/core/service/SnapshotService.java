package com.lukete.datagit.core.service;

import java.time.Instant;
import java.util.UUID;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.RequiredArgsConstructor;

/**
 * Orchestrates snapshot creation and persistence.
 */
@RequiredArgsConstructor
public class SnapshotService {
    private final DataSourceAdapter adapter;
    private final SnapshotStorage storage;
    private final SnapshotNormalizer normalizer;
    private final DataGitConfig config;
    private final CliPrinter printer;

    /**
     * Creates a snapshot from the configured data source and persists it.
     *
     * @return the persisted snapshot enriched with its generated identifier and
     *         timestamp
     */
    public Snapshot createSnapshot() {
        Stopwatch total = Stopwatch.start();

        Stopwatch extractWatch = Stopwatch.start();
        Snapshot rawSnapshot = adapter.extract();
        printer.performance("Snapshot extracted in " + extractWatch.elapsedMillis() + "ms");

        Stopwatch normalizeWatch = Stopwatch.start();
        Snapshot normalizedSnapshot = normalizer.normalize(
                rawSnapshot,
                config.getSnapshotConfig().getIgnoredColumns());

        printer.performance("Snapshot normalized in " + normalizeWatch.elapsedMillis() + "ms");

        Stopwatch saveWatch = Stopwatch.start();
        Snapshot snapshot = enrichSnapshot(normalizedSnapshot);

        storage.save(snapshot);

        printer.performance("Snapshot saved in " + saveWatch.elapsedMillis() + "ms");
        printer.performance("Snapshot completed in " + total.elapsedMillis() + "ms");

        return snapshot;
    }

    private Snapshot enrichSnapshot(Snapshot snapshot) {
        return new Snapshot(
                snapshot.id() == null || snapshot.id().isBlank() ? UUID.randomUUID().toString() : snapshot.id(),
                snapshot.timestamp() == null ? Instant.now() : snapshot.timestamp(),
                snapshot.source(),
                snapshot.tables(),
                snapshot.schema());
    }
}
