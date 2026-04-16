package com.lukete.datagit.cli.command;

import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

/**
 * CLI command that lists the snapshots currently available in storage.
 */
@Command(name = "log", description = "")
@Slf4j
public class LogCommand implements Runnable {
    private static final String FORMAT = "%-10s | %-40s | %-10s";
    private static final String DIVISOR = "--------------------------------------------------------------------------------------";
    private static final String[] COLUMNS = { "SHORT UUID", "COMPLETE UUID", "TIMESTAMP" };
    private final SnapshotStorage storage;

    public LogCommand(SnapshotStorage storage) {
        this.storage = storage;
    }

    /**
     * Prints the snapshot history ordered by timestamp in descending.
     */
    @Override
    public void run() {
        log.info(String.format(FORMAT, (Object[]) COLUMNS));
        log.info(DIVISOR);
        storage.list().stream()
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .forEach(s -> log.info(String.format(FORMAT, s.id().substring(0, 7), s.id(), s.timestamp())));
    }
}
