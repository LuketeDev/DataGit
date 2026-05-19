package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.diff.DiffResult;
import com.lukete.datagit.core.domain.diff.SchemaDiffResult;
import com.lukete.datagit.core.domain.schema.ColumnChange;
import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.schema.TableSchema;
import com.lukete.datagit.core.domain.snapshot.FieldChange;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.ports.SnapshotStorage;

class SchemaDiffRegressionTest {
    @Test
    void shouldReportSchemaChangesAlongsideRowChanges() {
        Snapshot oldSnapshot = snapshot(
                "old",
                "2026-04-08T10:00:00Z",
                Map.of("users", List.of(row("id", 1, "name", "Lucas"))),
                schema(table("users",
                        column("id", "integer", false),
                        column("name", "text", true))));
        Snapshot newSnapshot = snapshot(
                "new",
                "2026-04-08T11:00:00Z",
                Map.of("users", List.of(row("id", 1, "name", "Lucas Silva", "email", "lucas@example.com"))),
                schema(table("users",
                        column("id", "bigint", false),
                        column("name", "text", true),
                        column("email", "text", true))));

        DiffResult rowDiff = new DiffService(new SnapshotNormalizer(), config()).compare(oldSnapshot, newSnapshot);
        SchemaDiffResult schemaDiff = new SchemaDiffService().compare(oldSnapshot.schema(), newSnapshot.schema());

        assertThat(rowDiff.tables().get("users").updated().getFirst().fieldChanges())
                .containsExactly(
                        new FieldChange("email", null, "lucas@example.com"),
                        new FieldChange("name", "Lucas", "Lucas Silva"));
        assertThat(schemaDiff.updatedTables().get("users").createdColumns())
                .containsExactly(column("email", "text", true));
        assertThat(schemaDiff.updatedTables().get("users").updatedColumns())
                .containsExactly(new ColumnChange(
                        column("id", "integer", false),
                        column("id", "bigint", false)));
    }

    @Test
    void shouldComputeSchemaDiffAgainstHeadReferences() {
        InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        storage.save(snapshot(
                "snap-1",
                "2026-04-08T10:00:00Z",
                Map.of(),
                schema(table("users", column("id", "integer", false)))));
        storage.save(snapshot(
                "snap-2",
                "2026-04-08T11:00:00Z",
                Map.of(),
                schema(table("users",
                        column("id", "integer", false),
                        column("email", "text", true)))));

        ReferenceResolver resolver = new ReferenceResolver(storage);
        SchemaDiffResult diff = new SchemaDiffService().compare(
                resolver.resolve("HEAD~1").schema(),
                resolver.resolve("HEAD").schema());

        assertThat(diff.updatedTables()).containsOnlyKeys("users");
        assertThat(diff.updatedTables().get("users").createdColumns())
                .containsExactly(column("email", "text", true));
    }

    private static DataGitConfig config() {
        DataGitConfig config = new DataGitConfig();
        SnapshotConfig snapshotConfig = new SnapshotConfig();
        snapshotConfig.setIgnoredColumns(List.of());
        config.setSnapshotConfig(snapshotConfig);
        return config;
    }

    private static Snapshot snapshot(
            String id,
            String timestamp,
            Map<String, List<Map<String, Object>>> tables,
            SchemaSnapshot schema) {
        return new Snapshot(id, Instant.parse(timestamp), "postgres", tables, schema);
    }

    private static SchemaSnapshot schema(TableSchema... tables) {
        Map<String, TableSchema> tableMap = new LinkedHashMap<>();
        for (TableSchema table : tables) {
            tableMap.put(table.tableName(), table);
        }
        return new SchemaSnapshot(tableMap);
    }

    private static TableSchema table(String tableName, ColumnSchema... columns) {
        Map<String, ColumnSchema> columnMap = new LinkedHashMap<>();
        for (ColumnSchema column : columns) {
            columnMap.put(column.name(), column);
        }
        return new TableSchema(tableName, columnMap);
    }

    private static ColumnSchema column(String name, String type, boolean nullable) {
        return new ColumnSchema(name, type, nullable);
    }

    private static Map<String, Object> row(Object... entries) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            row.put((String) entries[i], entries[i + 1]);
        }
        return row;
    }

    private static class InMemorySnapshotStorage implements SnapshotStorage {
        private final List<Snapshot> snapshots = new ArrayList<>();

        @Override
        public void save(Snapshot snapshot) {
            snapshots.add(snapshot);
        }

        @Override
        public Optional<Snapshot> load(String id) {
            return snapshots.stream()
                    .filter(snapshot -> snapshot.id().equals(id))
                    .findFirst();
        }

        @Override
        public List<Snapshot> list() {
            return new ArrayList<>(snapshots);
        }
    }
}
