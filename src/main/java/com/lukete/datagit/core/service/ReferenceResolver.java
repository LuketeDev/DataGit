package com.lukete.datagit.core.service;

import java.util.Comparator;
import java.util.List;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.InvalidReferenceException;
import com.lukete.datagit.core.exception.NoSnapshotsFoundException;
import com.lukete.datagit.core.exception.SnapshotNotFoundException;
import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.RequiredArgsConstructor;

/**
 * Resolves human-friendly references like HEAD, HEAD~1, etc.
 */
@RequiredArgsConstructor
public class ReferenceResolver {
    private final SnapshotStorage storage;

    public Snapshot resolve(String ref) {
        List<Snapshot> snapshots = storage.list()
                .stream()
                // Sort by timestamp and reversed (latest first)
                .sorted(Comparator.comparing(Snapshot::timestamp).reversed())
                .toList();

        if (snapshots.isEmpty()) {
            throw new NoSnapshotsFoundException();
        }

        // HEAD is latest
        if (ref.equalsIgnoreCase("HEAD")) {
            return snapshots.get(0);
        }

        if (ref.startsWith("HEAD~")) {
            int index = Integer.parseInt(ref.substring(5));

            if (index >= snapshots.size()) {
                throw new InvalidReferenceException(ref);
            }

            return snapshots.get(index);
        }

        // fallback: assume it's an ID
        return storage.load(ref).orElseThrow(() -> new SnapshotNotFoundException(ref));
    }
}
