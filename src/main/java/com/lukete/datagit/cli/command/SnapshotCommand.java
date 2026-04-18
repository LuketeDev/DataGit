package com.lukete.datagit.cli.command;

import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.core.service.SnapshotService;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

/**
 * CLI command that captures and stores a new snapshot from the configured data
 * source.
 */
@Command(name = "snapshot", description = "Creates a new snapshot from Database")
@RequiredArgsConstructor
public class SnapshotCommand implements Runnable {
    private final SnapshotService service;
    private final CliPrinter printer;

    /**
     * Creates a new snapshot and prints its generated identifier.
     */
    @Override
    public void run() {
        var snapshot = service.createSnapshot();

        printer.success("Snapshot created: " + snapshot.id());
    }

}
