package com.lukete.datagit.cli.error;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorRenderer {
    private ErrorRenderer() {
        /* This utility class should not be instantiated */
    }

    /**
     * Renders an exception either as a full stack trace or as a concise message.
     *
     * @param e       the exception to render
     * @param verbose whether the full stack trace should be printed
     */
    public static void render(Throwable e, boolean verbose) {
        if (verbose) {
            e.printStackTrace();
        } else {
            log.error(e.getMessage());
        }
    }
}
