package com.lukete.datagit.cli.output;

import java.io.PrintWriter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CliPrinter {
    private final PrintWriter out;
    private final PrintWriter err;

    public void success(String message) {
        out.println(message);
    }

    public void info(String message) {
        out.println("INFO " + message);
    }

    public void warn(String message) {
        err.println("WARN " + message);
    }

    public void error(String message) {
        err.println("ERROR " + message);
    }

    public void hint(String message) {
        out.println("TIP " + message);
    }

    public void blankLine() {
        out.println();
    }
}