package com.lukete.datagit.core.exception;

import java.io.PrintWriter;

import com.lukete.datagit.cli.command.DataGitCommand;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;

/**
 * Centralizes CLI exception rendering for command execution failures.
 */
@RequiredArgsConstructor
public class CliExecutionExceptionHandler implements IExecutionExceptionHandler {
    private final DataGitCommand rootCommand;
    private static final String ERROR_LABEL = "[!!] ";

    /**
     * Maps known exceptions to user-friendly CLI messages and preserves verbose
     * output when requested.
     *
     * @param ex          the exception raised during command execution
     * @param commandLine the active Picocli command line
     * @param parseResult the parse result for the executed command
     * @return the exit code configured for execution exceptions
     * @throws Exception when Picocli requires propagation of the original failure
     */
    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult)
            throws Exception {
        PrintWriter err = commandLine.getErr();

        if (ex instanceof SnapshotNotFoundException) {
            err.println(ERROR_LABEL + ex.getMessage());
            err.println(ERROR_LABEL + "Try: datagit log");

            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
        if (ex instanceof AmbiguousReferenceException) {
            err.println(ERROR_LABEL + ex.getMessage());
            err.println(ERROR_LABEL + "Try: insert a longer ID or use: datagit log");

            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
        if (ex instanceof NoSnapshotsFoundException) {
            err.println(ERROR_LABEL + ex.getMessage());
            err.println(ERROR_LABEL + "Try: create first with: datagit snapshot");

            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
        if (ex instanceof DataGitException) {
            err.println(ERROR_LABEL + ex.getMessage());

            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
        if (ex instanceof ConfigCreationException) {
            err.println(ERROR_LABEL + ex.getMessage());
        }

        if (rootCommand.isVerbose()) {
            ex.printStackTrace();
        } else {
            err.println(ERROR_LABEL + "Unexpected internal error.");
            err.println(ERROR_LABEL + "Re-run with --verbose for more information.");
        }
        return commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
}
