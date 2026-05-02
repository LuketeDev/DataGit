package com.lukete.datagit.cli.command;

import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.CliPrinter;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "checkout", description = "Restore the database state from a snapshot.")
@RequiredArgsConstructor
public class RestoreCommand implements Runnable {
    @Parameters(index = "0", description = "Snapshot reference to restore, e.g. HEAD, HEAD~1 or short ID.")
    private String ref;

    @Option(names = "--yes", description = "Confirm restore operation")
    private boolean yes;

    private final DataGitContextProvider contextProvider;
    private final CliPrinter printer;

    @Override
    public void run() {
        var context = contextProvider.get();
        var snapshot = context.getReferenceResolver().resolve(ref);

        printer.warn("This operation will overwrite current table data.");
        printer.info("Target snapshot: " + snapshot.id());
        printer.info("Source: " + snapshot.source());
        printer.info("Tables: " + snapshot.tables().size());

        if (!yes) {
            printer.blankLine();
            printer.hint("Run again with --yes to confirm:");
            printer.info("datagit checkout " + ref + " --yes");
            return;
        }

        context.getRestoreService().restore(snapshot);

        printer.success("Database restored from snapshot: " + snapshot.id());
    }
}
