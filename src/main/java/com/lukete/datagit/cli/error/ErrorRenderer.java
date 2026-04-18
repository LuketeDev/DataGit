package com.lukete.datagit.cli.error;

import com.lukete.datagit.cli.output.CliPrinter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorRenderer {
    private final CliPrinter printer;

    /**
     * Renders an exception either as a full stack trace or as a concise message.
     *
     * @param e       the exception to render
     * @param verbose whether the full stack trace should be printed
     */
    public void render(Throwable e, boolean verbose) {
        if (verbose) {
            e.printStackTrace();
        } else {
            printer.error(e.getMessage());
        }
    }
}
