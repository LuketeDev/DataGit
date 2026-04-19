package com.lukete.datagit.core.service;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusService {
    private final DataSourceAdapter adapter;
    private final ReferenceResolver resolver;
    private final DiffService diffService;
    private final SnapshotNormalizer snapshotNormalizer;
    private final DataGitConfig config;

    public DiffResult getStatus() {
        var current = snapshotNormalizer.normalize(adapter.extract(), config.getSnapshotConfig().getIgnoredColumns());
        var head = resolver.resolve("HEAD");
        return diffService.compare(head, current);
    }
}
