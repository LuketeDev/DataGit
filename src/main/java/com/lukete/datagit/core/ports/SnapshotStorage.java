package com.lukete.datagit.core.ports;

import com.lukete.datagit.core.domain.Snapshot;

import java.util.List;
import java.util.Optional;

/**
 * Defines how snapshots are persisted and retrieved.
 */
public interface SnapshotStorage {

    /**
     * Saves a snapshot.
     *
     * @param snapshot the snapshot to persist
     */
    void save(Snapshot snapshot);

    /**
     * Loads a snapshot by its identifier.
     *
     * @param id the snapshot identifier
     * @return the stored snapshot when it exists
     */
    Optional<Snapshot> load(String id);

    /**
     * Lists all available snapshots.
     *
     * @return the stored snapshots
     */
    List<Snapshot> list();
}
