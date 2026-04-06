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
     * Creates snapshot from datasource and stores it
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