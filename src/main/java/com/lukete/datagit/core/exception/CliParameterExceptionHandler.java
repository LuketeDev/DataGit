package com.lukete.datagit.core.exception;

import java.io.PrintWriter;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

/**
 * Renders parameter parsing errors and the corresponding command usage help.
 */
public class CliParameterExceptionHandler implements CommandLine.IParameterExceptionHandler {

    /**
     * Handles invalid command-line arguments by printing the error and usage instructions.
     *
     * @param ex the parsing exception raised by Picocli
     * @param args the raw command-line arguments
     * @return the exit code configured for invalid input
     * @throws Exception when Picocli requires propagation of the original failure
     */
    @Override
    public int handleParseException(ParameterException ex, String[] args) throws Exception {
        CommandLine commandLine = ex.getCommandLine();
        PrintWriter err = commandLine.getErr();

        err.println("[!!] " + ex.getMessage());
        err.println();

        commandLine.usage(err);

        return commandLine.getCommandSpec().exitCodeOnInvalidInput();
    }
}
