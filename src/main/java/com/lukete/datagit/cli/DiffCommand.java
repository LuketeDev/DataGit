package com.lukete.datagit.cli;

import com.lukete.datagit.core.ports.SnapshotStorage;
import com.lukete.datagit.core.service.DiffService;
import static com.lukete.datagit.core.util.JsonUtils.toJson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

// TODO Defaults to latest and previous.
@Command(name = "diff", description = "Compare two snapshots.")
@RequiredArgsConstructor
@Slf4j
public class DiffCommand implements Runnable {
    @Parameters(index = "0")
    private String oldId;

    @Parameters(index = "1")
    private String newId;

    private final SnapshotStorage storage;
    private final DiffService service;

    @Override
    public void run() {
        var oldSnap = storage.load(oldId)
                .orElseThrow(() -> new RuntimeException("Could not find snapshot: " + oldId));
        var newSnap = storage.load(newId)
                .orElseThrow(() -> new RuntimeException("Could not find snapshot: " + newId));

        var diff = service.compare(oldSnap, newSnap);

        log.info(toJson(diff));
    }
}
