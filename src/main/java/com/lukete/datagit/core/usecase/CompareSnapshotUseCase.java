package com.lukete.datagit.core.usecase;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.ReferenceResolver;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompareSnapshotUseCase {
    private final ReferenceResolver refResolver;
    private final DiffService diffService;

    public DiffResult execute(String oldRef, String newRef) {
        var oldSnapshot = refResolver.resolve(oldRef);
        var newSnapshot = refResolver.resolve(newRef);
        return diffService.compare(oldSnapshot, newSnapshot);
    }
}
