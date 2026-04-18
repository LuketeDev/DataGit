package com.lukete.datagit.cli.command;

import com.lukete.datagit.cli.output.LogCliRenderer;
import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

/**
 * Lists saved snapshots.
 */
@Command(name = "log", description = "List saved snapshots in reverse chronological order.")
@RequiredArgsConstructor
public class LogCommand implements Runnable {

    private final SnapshotStorage storage;
    private final LogCliRenderer renderer;

    /**
     * Prints the snapshot history ordered by timestamp in descending.
     */
    @Override
    public void run() {
        var snapshots = storage.list();
        renderer.render(snapshots);
    }
}
