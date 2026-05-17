package com.lukete.datagit.support;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.schema.TableSchema;
import com.lukete.datagit.core.domain.snapshot.Snapshot;

public final class TestSnapshots {
    private static final Instant DEFAULT_TIMESTAMP = Instant.parse("2026-04-08T10:00:00Z");
    private static final String DEFAULT_SOURCE = "postgres";

    private TestSnapshots() {
    }

    public static Snapshot snapshot(String id, Map<String, List<Map<String, Object>>> tables) {
        return new Snapshot(id, DEFAULT_TIMESTAMP, DEFAULT_SOURCE, tables, schemaFor(tables));
    }

    public static Snapshot snapshot(
            String id,
            Instant timestamp,
            Map<String, List<Map<String, Object>>> tables) {
        return new Snapshot(id, timestamp, DEFAULT_SOURCE, tables, schemaFor(tables));
    }

    public static SchemaSnapshot schemaFor(Map<String, List<Map<String, Object>>> tables) {
        Map<String, TableSchema> schemas = new LinkedHashMap<>();
        tables.forEach((tableName, rows) -> schemas.put(tableName, tableSchema(tableName, rows)));
        return new SchemaSnapshot(schemas);
    }

    public static SchemaSnapshot emptySchema() {
        return new SchemaSnapshot(Map.of());
    }

    private static TableSchema tableSchema(String tableName, List<Map<String, Object>> rows) {
        Map<String, ColumnSchema> columns = new LinkedHashMap<>();
        if (rows != null) {
            rows.stream()
                    .filter(row -> row != null && !row.isEmpty())
                    .findFirst()
                    .ifPresent(row -> row.forEach((columnName, value) -> columns.put(
                            columnName,
                            new ColumnSchema(columnName, typeOf(value), value == null))));
        }
        return new TableSchema(tableName, columns);
    }

    private static String typeOf(Object value) {
        return value == null ? "unknown" : value.getClass().getSimpleName();
    }
}
