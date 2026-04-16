package com.lukete.datagit.cli.command;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.usecase.CompareSnapshotUseCase;
import com.lukete.datagit.core.util.DiffJsonFormatter;
import com.lukete.datagit.core.util.DiffTextFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * CLI command that compares two snapshots and renders the resulting diff.
 */
@Command(name = "diff", description = "Compare two snapshots.")
@RequiredArgsConstructor
@Slf4j
public class DiffCommand implements Runnable {
    @Parameters(index = "0", description = "ID of the old snapshot (defaults to HEAD~1)", defaultValue = "HEAD~1")
    private String oldId = "HEAD~1";

    @Parameters(index = "1", description = "ID of the new snapshot (defaults to HEAD)", defaultValue = "HEAD")
    private String newId = "HEAD";

    @Option(names = {
            "--complete" }, defaultValue = "false", fallbackValue = "true", description = "Display as object. If false, display as list.")
    private boolean complete = false;

    private final CompareSnapshotUseCase compareSnapshotUseCase;
    private final DiffTextFormatter diffTextFormatter;
    private final DiffJsonFormatter diffJsonFormatter;

    /**
     * Resolves the requested snapshots, computes the diff, and prints it in the selected format.
     */
    @Override
    public void run() {
        DiffResult diffResult = compareSnapshotUseCase.execute(oldId, newId);

        String output = complete
                ? diffJsonFormatter.format(diffResult)
                : diffTextFormatter.format(diffResult);

        log.info(output);
    }
}
