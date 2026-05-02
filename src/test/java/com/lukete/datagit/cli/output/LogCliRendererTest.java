package com.lukete.datagit.cli.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.Snapshot;

class LogCliRendererTest {
    @Test
    void shouldRenderHeader() {
        String output = render(List.of(snapshot("abc123456789", "2026-04-08T10:00:00Z")));

        assertThat(output).contains("ID", "TIMESTAMP", "SOURCE", "TABLES", "ROWS");
    }

    @Test
    void shouldSortSnapshotsNewestFirst() {
        String output = render(List.of(
                snapshot("older-123", "2026-04-08T10:00:00Z"),
                snapshot("newer-123", "2026-04-08T12:00:00Z")));

        assertThat(output).containsSubsequence("newer-12", "older-12");
    }

    @Test
    void shouldShowShortId() {
        String output = render(List.of(snapshot("1234567890abcdef", "2026-04-08T10:00:00Z")));

        assertThat(output).contains("12345678");
        assertThat(output).doesNotContain("1234567890abcdef");
    }

    @Test
    void shouldShowSourceTablesCountAndRowCount() {
        String output = render(List.of(snapshot("abc12345", "2026-04-08T10:00:00Z")));

        assertThat(output).contains("postgres");
        assertThat(output).containsPattern("2\\s+3");
    }

    @Test
    void shouldHandleEmptySnapshotListWithHelpfulMessage() {
        String output = render(List.of());

        assertThat(output).contains("No snapshots found.");
        assertThat(output).contains("snapshot");
    }

    private static String render(List<Snapshot> snapshots) {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        CliPrinter printer = new CliPrinter(new PrintWriter(out, true), new PrintWriter(err, true));

        new LogCliRenderer(printer).render(snapshots);

        return out.toString();
    }

    private static Snapshot snapshot(String id, String timestamp) {
        return new Snapshot(
                id,
                Instant.parse(timestamp),
                "postgres",
                Map.of(
                        "users", List.of(Map.of("id", 1), Map.of("id", 2)),
                        "orders", List.of(Map.of("id", 10))));
    }
}
