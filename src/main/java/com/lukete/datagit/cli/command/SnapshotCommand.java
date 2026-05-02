package com.lukete.datagit.cli.command;

import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.CliPrinter;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

/**
 * CLI command that captures and stores a new snapshot from the configured data
 * source.
 */
@Command(name = "snapshot", description = "Creates a new snapshot from Database")
@RequiredArgsConstructor
public class SnapshotCommand implements Runnable {
    private final DataGitContextProvider contextProvider;
    private final CliPrinter printer;

    /**
     * Creates a new snapshot and prints its generated identifier.
     */
    @Override
    public void run() {
        var context = contextProvider.get();
        var snapshot = context.getSnapshotService().createSnapshot();

        printer.success("Snapshot created: " + snapshot.id());
    }

}
