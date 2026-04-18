package com.lukete.datagit.cli.command;

import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

/**
 * CLI command that lists the snapshots currently available in storage.
 */
@Command(name = "log", description = "")
@RequiredArgsConstructor
public class LogCommand implements Runnable {
    private static final String FORMAT = "%-10s | %-40s | %-10s";
    private static final String DIVISOR = "--------------------------------------------------------------------------------------";
    private static final String[] COLUMNS = { "SHORT UUID", "COMPLETE UUID", "TIMESTAMP" };
    private final SnapshotStorage storage;
    private final CliPrinter printer;

    /**
     * Prints the snapshot history ordered by timestamp in descending.
     */
    @Override
    public void run() {
        printer.info(String.format(FORMAT, (Object[]) COLUMNS));
        printer.info(DIVISOR);
        storage.list().stream()
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .forEach(s -> printer.info(String.format(FORMAT, s.id().substring(0, 7), s.id(), s.timestamp())));
    }
}
