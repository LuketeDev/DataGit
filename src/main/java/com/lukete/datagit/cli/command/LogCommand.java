package com.lukete.datagit.cli.command;

import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.cli.render.renderer.LogCliRenderer;

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
    private final CliPrinter printer;

    /**
     * Prints the snapshot history ordered by timestamp in descending.
     */
    @Override
    public void run() {
        var context = contextProvider.get(printer);
        var snapshots = context.getStorage().list();
        renderer.render(snapshots);
    }
}
