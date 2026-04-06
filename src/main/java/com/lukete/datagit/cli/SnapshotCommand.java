package com.lukete.datagit.cli;

import com.lukete.datagit.core.service.SnapshotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Command(name = "snapshot", description = "Creates a new snapshot from Database")
@RequiredArgsConstructor
@Slf4j
public class SnapshotCommand implements Runnable {
    private final SnapshotService service;

    @Override
    public void run() {
        var snapshot = service.createSnapshot();

        log.info("Snapshot created: " + snapshot.id());
    }

}
