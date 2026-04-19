package com.lukete.datagit.cli.output;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageStyle {
    /**
     * Extra white spaces in label for text padding normalization
     * Consider '.' as white spaces, including spaces from CliPrinter
     * OK......TEXT
     * INFO....TEXT
     * WARN....TEXT
     * ERROR...TEXT
     * TIP.....TEXT
     */
    SUCCESS("OK    "),
    INFO("INFO  "),
    WARNING("WARN  "),
    ERROR("ERROR "),
    HINT("TIP   ");

    private final String label;

    public String label() {
        return label;
    }
}