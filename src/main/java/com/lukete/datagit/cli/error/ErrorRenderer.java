package com.lukete.datagit.cli.error;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorRenderer {
    public static void render(Throwable e, boolean verbose) {
        if (verbose) {
            e.printStackTrace();
        } else {
            log.error(e.getMessage());
        }
    }
}
