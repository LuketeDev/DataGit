package com.lukete.datagit.core.ports;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.domain.SnapshotMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Defines how snapshots are persisted and retrieved.
 */
public interface SnapshotStorage {

    /**
     * Saves a snapshot.
     */
    void save(Snapshot snapshot);

    /**
     * Loads a snapshot by its ID.
     */
    Optional<Snapshot> load(String id);

    /**
     * Lists all available snapshots (metadata only in future).
     */
    List<SnapshotMetadata> list();
}