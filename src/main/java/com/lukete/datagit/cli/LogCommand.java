package com.lukete.datagit.cli;

import com.lukete.datagit.core.ports.SnapshotStorage;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Command(name = "log", description = "")
@Slf4j
public class LogCommand implements Runnable {

    private final SnapshotStorage storage;

    public LogCommand(SnapshotStorage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        storage.list().stream()
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .forEach(s -> log.info(
                        s.id() + " - " + s.timestamp()));
    }
}