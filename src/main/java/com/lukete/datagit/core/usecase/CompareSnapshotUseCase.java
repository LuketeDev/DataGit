package com.lukete.datagit.core.usecase;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.ReferenceResolver;

import lombok.RequiredArgsConstructor;

/**
 * Use case that resolves two snapshot references and compares their contents.
 */
@RequiredArgsConstructor
public class CompareSnapshotUseCase {
    private final ReferenceResolver refResolver;
    private final DiffService diffService;

    /**
     * Resolves the provided references and computes the diff between them.
     *
     * @param oldRef the baseline snapshot reference
     * @param newRef the snapshot reference to compare against the baseline
     * @return the diff produced from the resolved snapshots
     */
    public DiffResult execute(String oldRef, String newRef) {
        var oldSnapshot = refResolver.resolve(oldRef);
        var newSnapshot = refResolver.resolve(newRef);
        return diffService.compare(oldSnapshot, newSnapshot);
    }
}
