package com.lukete.datagit.core.service;

import java.time.Instant;
import java.util.UUID;

import com.lukete.datagit.core.domain.Snapshot;
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

    /**
     * Creates a snapshot from the configured data source and persists it.
     *
     * @return the persisted snapshot enriched with its generated identifier and timestamp
     */
    public Snapshot createSnapshot() {
        Snapshot rawSnapshot = adapter.extract();

        Snapshot snapshot = new Snapshot(
                UUID.randomUUID().toString(),
                Instant.now(),
                rawSnapshot.source(),
                rawSnapshot.tables());

        storage.save(snapshot);
        return snapshot;
    }
}
