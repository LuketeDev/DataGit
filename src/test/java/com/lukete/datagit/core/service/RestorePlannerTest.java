package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.Snapshot;

class RestorePlannerTest {
    private final RestorePlanner planner = new RestorePlanner();

    @Test
    void shouldCreatePlanWithCorrectTableAndRowCounts() {
        Snapshot snapshot = snapshot(tables(
                table("users", row("id", 1), row("id", 2)),
                table("orders", row("id", 10))));

        var plan = planner.plan(snapshot);

        assertThat(plan.snapshotId()).isEqualTo("snap-1");
        assertThat(plan.source()).isEqualTo("postgres");
        assertThat(plan.tableCount()).isEqualTo(2);
        assertThat(plan.rowCount()).isEqualTo(3);
        assertThat(plan.tables())
                .extracting("tableName", "rowCount")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("users", 2),
                        org.assertj.core.groups.Tuple.tuple("orders", 1));
    }

    @Test
    void shouldHandleEmptySnapshot() {
        Snapshot snapshot = snapshot(Map.of());

        var plan = planner.plan(snapshot);

        assertThat(plan.tableCount()).isZero();
        assertThat(plan.rowCount()).isZero();
        assertThat(plan.tables()).isEmpty();
    }

    @Test
    void shouldHandleNullRowsInTables() {
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        tables.put("users", null);

        var plan = planner.plan(snapshot(tables));

        assertThat(plan.tableCount()).isEqualTo(1);
        assertThat(plan.rowCount()).isZero();
        assertThat(plan.tables())
                .extracting("tableName", "rowCount")
                .containsExactly(org.assertj.core.groups.Tuple.tuple("users", 0));
    }

    @Test
    void shouldSumTotalRowCountCorrectly() {
        Snapshot snapshot = snapshot(tables(
                table("users", row("id", 1), row("id", 2), row("id", 3)),
                table("orders", row("id", 10)),
                table("audit_log")));

        var plan = planner.plan(snapshot);

        assertThat(plan.rowCount()).isEqualTo(4);
    }

    private static Snapshot snapshot(Map<String, List<Map<String, Object>>> tables) {
        return new Snapshot("snap-1", Instant.parse("2026-04-08T10:00:00Z"), "postgres", tables);
    }

    @SafeVarargs
    private static Map<String, List<Map<String, Object>>> tables(TableRows... tables) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (TableRows table : tables) {
            result.put(table.name(), table.rows());
        }
        return result;
    }

    @SafeVarargs
    private static TableRows table(String name, Map<String, Object>... rows) {
        return new TableRows(name, List.of(rows));
    }

    private static Map<String, Object> row(String key, Object value) {
        return Map.of(key, value);
    }

    private record TableRows(String name, List<Map<String, Object>> rows) {
    }
}
