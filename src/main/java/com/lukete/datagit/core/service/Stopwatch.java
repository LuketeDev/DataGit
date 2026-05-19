package com.lukete.datagit.core.service;

import java.time.Duration;

public final class Stopwatch {
    private final long startedAt;

    private Stopwatch() {
        this.startedAt = System.nanoTime();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public long elapsedMillis() {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }

}