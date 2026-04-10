package com.lukete.datagit.core.exception;

import java.io.PrintWriter;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class CliParameterExceptionHandler implements CommandLine.IParameterExceptionHandler {

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
