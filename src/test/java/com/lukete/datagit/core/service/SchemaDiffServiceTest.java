package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.diff.SchemaDiffResult;
import com.lukete.datagit.core.domain.schema.ColumnChange;
import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.schema.TableSchema;

class SchemaDiffServiceTest {
    private final SchemaDiffService service = new SchemaDiffService();

    @Test
    void shouldDetectCreatedTables() {
        SchemaDiffResult diff = service.compare(schema(), schema(
                table("users", column("id", "integer", false))));

        assertThat(diff.createdTables()).containsExactly(
                table("users", column("id", "integer", false)));
        assertThat(diff.deletedTables()).isEmpty();
        assertThat(diff.updatedTables()).isEmpty();
    }

    @Test
    void shouldDetectDeletedTables() {
        SchemaDiffResult diff = service.compare(
                schema(table("users", column("id", "integer", false))),
                schema());

        assertThat(diff.createdTables()).isEmpty();
        assertThat(diff.deletedTables()).containsExactly(
                table("users", column("id", "integer", false)));
        assertThat(diff.updatedTables()).isEmpty();
    }

    @Test
    void shouldDetectCreatedColumns() {
        SchemaDiffResult diff = service.compare(
                schema(table("users", column("id", "integer", false))),
                schema(table("users",
                        column("id", "integer", false),
                        column("email", "text", true))));

        assertThat(diff.updatedTables()).containsOnlyKeys("users");
        assertThat(diff.updatedTables().get("users").createdColumns())
                .containsExactly(column("email", "text", true));
        assertThat(diff.updatedTables().get("users").deletedColumns()).isEmpty();
        assertThat(diff.updatedTables().get("users").updatedColumns()).isEmpty();
    }

    @Test
    void shouldDetectDeletedColumns() {
        SchemaDiffResult diff = service.compare(
                schema(table("users",
                        column("id", "integer", false),
                        column("email", "text", true))),
                schema(table("users", column("id", "integer", false))));

        assertThat(diff.updatedTables().get("users").deletedColumns())
                .containsExactly(column("email", "text", true));
    }

    @Test
    void shouldDetectTypeChanges() {
        SchemaDiffResult diff = service.compare(
                schema(table("users", column("id", "integer", false))),
                schema(table("users", column("id", "bigint", false))));

        assertThat(diff.updatedTables().get("users").updatedColumns())
                .containsExactly(new ColumnChange(
                        column("id", "integer", false),
                        column("id", "bigint", false)));
    }

    @Test
    void shouldDetectNullableChanges() {
        SchemaDiffResult diff = service.compare(
                schema(table("users", column("email", "text", true))),
                schema(table("users", column("email", "text", false))));

        assertThat(diff.updatedTables().get("users").updatedColumns())
                .containsExactly(new ColumnChange(
                        column("email", "text", true),
                        column("email", "text", false)));
    }

    @Test
    void shouldReturnNoOpDiffForIdenticalSchemas() {
        SchemaSnapshot schema = schema(table("users",
                column("id", "integer", false),
                column("email", "text", true)));

        SchemaDiffResult diff = service.compare(schema, schema);

        assertThat(diff.createdTables()).isEmpty();
        assertThat(diff.deletedTables()).isEmpty();
        assertThat(diff.updatedTables()).isEmpty();
    }

    @Test
    void shouldDetectMultipleSimultaneousChanges() {
        SchemaDiffResult diff = service.compare(
                schema(
                        table("users",
                                column("id", "integer", false),
                                column("name", "text", true),
                                column("legacy_code", "text", true)),
                        table("orders", column("id", "integer", false))),
                schema(
                        table("users",
                                column("id", "bigint", false),
                                column("name", "text", true),
                                column("email", "text", true)),
                        table("teams", column("id", "integer", false))));

        assertThat(diff.createdTables()).containsExactly(table("teams", column("id", "integer", false)));
        assertThat(diff.deletedTables()).containsExactly(table("orders", column("id", "integer", false)));
        assertThat(diff.updatedTables()).containsOnlyKeys("users");
        assertThat(diff.updatedTables().get("users").createdColumns())
                .containsExactly(column("email", "text", true));
        assertThat(diff.updatedTables().get("users").deletedColumns())
                .containsExactly(column("legacy_code", "text", true));
        assertThat(diff.updatedTables().get("users").updatedColumns())
                .containsExactly(new ColumnChange(
                        column("id", "integer", false),
                        column("id", "bigint", false)));
    }

    @Test
    void shouldHandleEmptySchemas() {
        SchemaDiffResult diff = service.compare(schema(), schema());

        assertThat(diff.createdTables()).isEmpty();
        assertThat(diff.deletedTables()).isEmpty();
        assertThat(diff.updatedTables()).isEmpty();
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
}
