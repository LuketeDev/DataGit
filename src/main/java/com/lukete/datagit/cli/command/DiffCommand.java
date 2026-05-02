package com.lukete.datagit.cli.command;

import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.DiffRenderer;
import com.lukete.datagit.cli.output.OutputFormat;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * CLI command that compares two snapshots and renders the resulting diff.
 * old is left, new is right
 */
@Command(name = "diff", description = "Compare two snapshots.")
@RequiredArgsConstructor
public class DiffCommand implements Runnable {
    @Parameters(index = "0", description = "ID of the old snapshot (defaults to HEAD~1)", defaultValue = "HEAD~1")
    private String oldId = "HEAD~1";

    @Parameters(index = "1", description = "ID of the new snapshot (defaults to HEAD)", defaultValue = "HEAD")
    private String newId = "HEAD";

    @Option(names = {
            "--format" }, defaultValue = "TEXT", description = "Output format: ${COMPLETION-CANDIDATES}")
    private OutputFormat outputFormat;

    private final DataGitContextProvider contextProvider;
    private final DiffRenderer jsonRenderer;
    private final DiffRenderer textRenderer;

    /**
     * Resolves the requested snapshots, computes the diff, and prints it in the
     * selected format.
     */
    @Override
    public void run() {
        var context = contextProvider.get();

        var oldSnap = context.getReferenceResolver().resolve(oldId);
        var newSnap = context.getReferenceResolver().resolve(newId);

        var diff = context.getDiffService().compare(oldSnap, newSnap);

        DiffRenderer renderer = outputFormat == OutputFormat.TEXT ? textRenderer : jsonRenderer;

        renderer.render(oldId, newId, diff);
    }
}
