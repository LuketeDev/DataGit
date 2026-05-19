package com.lukete.datagit.cli.render.formatter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageStyle {
    SUCCESS("OK"),
    INFO("INFO"),
    WARNING("WARN"),
    ERROR("ERROR"),
    HINT("TIP"),
    PERFORMANCE("PERF");

    private final String label;

    public String label() {
        return label;
    }
}