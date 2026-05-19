package com.lukete.datagit.core.service;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestoreService {
    private final DataSourceAdapter adapter;
    private final CliPrinter printer;

    public void restore(Snapshot snapshot) {
        Stopwatch totalStopwatch = Stopwatch.start();
        adapter.restore(snapshot);
        printer.performance("Snapshot restored in " + totalStopwatch.elapsedMillis() + "ms");
    }
}
