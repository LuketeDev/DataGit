package com.lukete.datagit.cli.output;

import java.io.PrintWriter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CliPrinter {
    private final PrintWriter out;
    private final PrintWriter err;

    public void print(MessageStyle style, String message) {
        String line = style.label() + "  " + message;

        if (style == MessageStyle.ERROR || style == MessageStyle.WARNING) {
            err.println(line);
            return;
        }

        out.println(line);
    }

    public void success(String message) {
        print(MessageStyle.SUCCESS, message);
    }

    public void info(String message) {
        print(MessageStyle.INFO, message);
    }

    public void warn(String message) {
        print(MessageStyle.WARNING, message);
    }

    public void error(String message) {
        print(MessageStyle.ERROR, message);
    }

    public void hint(String message) {
        print(MessageStyle.HINT, message);
    }

    public void blankLine() {
        out.println();
    }
}