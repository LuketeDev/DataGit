package com.lukete.datagit.cli.command;

import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.LogCliRenderer;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

/**
 * Lists saved snapshots.
 */
@Command(name = "log", description = "List saved snapshots in reverse chronological order.")
@RequiredArgsConstructor
public class LogCommand implements Runnable {

    private final DataGitContextProvider contextProvider;
    private final LogCliRenderer renderer;

    /**
     * Prints the snapshot history ordered by timestamp in descending.
     */
    @Override
    public void run() {
        var context = contextProvider.get();
        var snapshots = context.getStorage().list();
        renderer.render(snapshots);
    }
}
