package com.lukete.datagit.cli.command;

import com.lukete.datagit.core.service.SnapshotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

/**
 * CLI command that captures and stores a new snapshot from the configured data
 * source.
 */
@Command(name = "snapshot", description = "Creates a new snapshot from Database")
@RequiredArgsConstructor
@Slf4j
public class SnapshotCommand implements Runnable {
    private final SnapshotService service;

    /**
     * Creates a new snapshot and prints its generated identifier.
     */
    @Override
    public void run() {
        var snapshot = service.createSnapshot();

        log.info("Snapshot created: " + snapshot.id());
    }

}
