package com.lukete.datagit.cli.command;

import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.cli.output.RestorePlanRenderer;
import com.lukete.datagit.core.exception.InvalidCommandOptionsException;

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

    @Option(names = "--dry-run", description = "Show restore plan without changing the database.")
    private boolean dryRun;

    private final DataGitContextProvider contextProvider;
    private final RestorePlanRenderer restorePlanRenderer;
    private final CliPrinter printer;

    @Override
    public void run() {
        if (yes && dryRun) {
            throw new InvalidCommandOptionsException("Options --yes and --dry-run cannot be used together.");
        }

        var context = contextProvider.get();

        var snapshot = context.getReferenceResolver().resolve(ref);
        var plan = context.getRestorePlanner().plan(snapshot);

        restorePlanRenderer.render(plan);

        if (dryRun) {
            printer.blankLine();
            printer.success("Dry run completed. No changes were applied.");
            return;
        }

        if (!yes) {
            printer.blankLine();
            printer.hint("Run again with --yes to confirm:");
            printer.info("datagit checkout " + ref + " --yes");
            return;
        }

        printer.blankLine();
        printer.info("Creating safety snapshot before restore...");
        var safetySnapshot = context.getSnapshotService().createSnapshot();
        printer.success("Safety snapshot created: " + safetySnapshot.id());
        printer.info("HEAD now points to the safety snapshot.");
        printer.hint("Run `datagit diff HEAD~1 HEAD` to see what was undone.");
        context.getRestoreService().restore(snapshot);

        printer.success("Database restored from snapshot: " + snapshot.id());
    }
}
