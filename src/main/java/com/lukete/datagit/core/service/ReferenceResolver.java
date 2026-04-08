package com.lukete.datagit.core.service;

import java.util.Comparator;
import java.util.List;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.AmbiguousReferenceException;
import com.lukete.datagit.core.exception.InvalidReferenceException;
import com.lukete.datagit.core.exception.NoSnapshotsFoundException;
import com.lukete.datagit.core.exception.SnapshotNotFoundException;
import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves human-friendly references like HEAD, HEAD~1, etc.
 */
@Slf4j
@RequiredArgsConstructor
public class ReferenceResolver {
    private final SnapshotStorage storage;

    public Snapshot resolve(String ref) {
        List<Snapshot> snapshots = storage.list();

        if (snapshots.isEmpty()) {
            throw new NoSnapshotsFoundException();
        }

        // HEAD is latest
        return loadFromRefOrId(ref, snapshots);
    }

    private Snapshot resolveShortId(String ref, List<Snapshot> snapshots) {
        var matches = snapshots.stream()
                .filter(s -> s.id().startsWith(ref))
                .toList();

        if (matches.isEmpty()) {
            throw new SnapshotNotFoundException(ref);
        }

        if (matches.size() > 1) {
            throw new AmbiguousReferenceException(ref);
        }

        return matches.get(0);
    }

    private Snapshot getLatest(List<Snapshot> snapshots) {
        return snapshots.stream().sorted(Comparator.comparing(Snapshot::timestamp).reversed()).toList().get(0);
    }

    private Snapshot loadFromRefOrId(String ref, List<Snapshot> snapshots) {
        if (ref.equalsIgnoreCase("HEAD")) {
            return getLatest(snapshots);
        }

        if (ref.startsWith("HEAD~")) {
            int index = Integer.parseInt(ref.substring(5));

            if (index >= snapshots.size()) {
                throw new InvalidReferenceException(ref);
            }

            return snapshots.get(index);
        }

        // fallback: assume it's an ID
        return resolveShortId(ref, snapshots);
    }
}
