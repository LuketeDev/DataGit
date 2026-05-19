package com.lukete.datagit.core.service;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.core.domain.diff.DiffResult;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusService {
    private final DataSourceAdapter adapter;
    private final ReferenceResolver resolver;
    private final DiffService diffService;
    private final SnapshotNormalizer snapshotNormalizer;
    private final DataGitConfig config;
    private final CliPrinter printer;;

    public DiffResult getStatus() {
        Stopwatch totalStopwatch = Stopwatch.start();
        var current = snapshotNormalizer.normalize(adapter.extract(), config.getSnapshotConfig().getIgnoredColumns());
        var head = resolver.resolve("HEAD");
        printer.performance("Status generated in " + totalStopwatch.elapsedMillis() + "ms");
        return diffService.compare(head, current);
    }
}
