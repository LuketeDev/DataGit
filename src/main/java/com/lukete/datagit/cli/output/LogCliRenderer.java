package com.lukete.datagit.cli.output;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import com.lukete.datagit.core.domain.Snapshot;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogCliRenderer {
    private static final String HEADER_FORMAT = "%-10s %-19s %-10s %-8s %-8s";
    private static final String ROW_FORMAT = "%-10s %-19s %-10s %-8d %-8d";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final CliPrinter printer;

    public void render(List<Snapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            printer.info("No snapshots found.");
            printer.hint("Run `datagit.snapshot` to create your first snapshot.");
            return;
        }

        // Sort snapshots
        List<Snapshot> sortedSnapshots = snapshots.stream()
                .sorted(Comparator.comparing(Snapshot::timestamp).reversed())
                .toList();

        // Render header
        printer.info(String.format(HEADER_FORMAT, "ID", "TIMESTAMP", "SOURCE", "TABLES", "ROWS"));

        for (Snapshot snapshot : sortedSnapshots) {
            printer.info(String.format(ROW_FORMAT,
                    shortId(snapshot),
                    formatTimeStamp(snapshot),
                    safeSource(snapshot),
                    countTables(snapshot),
                    countRows(snapshot)));
        }

    }

    private String shortId(Snapshot snapshot) {
        if (snapshot.id() == null || snapshot.id().isBlank()) {
            return "-";
        }

        return snapshot.id().length() <= 8 ? snapshot.id() : snapshot.id().substring(0, 8);
    }

    private String formatTimeStamp(Snapshot snapshot) {
        if (snapshot.timestamp() == null) {
            return "-";
        }

        return TIMESTAMP_FORMATTER.format(snapshot.timestamp());
    }

    private int countTables(Snapshot snapshot) {
        if (snapshot.tables() == null) {
            return 0;
        }

        return snapshot.tables().size();
    }

    private int countRows(Snapshot snapshot) {
        if (snapshot.tables() == null || snapshot.tables().isEmpty()) {
            return 0;
        }

        return snapshot.tables()
                .values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }

    private String safeSource(Snapshot snapshot) {
        return snapshot.source() == null || snapshot.source().isBlank() ? "-" : snapshot.source();
    }
}