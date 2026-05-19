package com.lukete.datagit.cli.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.cli.render.renderer.JsonSchemaDiffRenderer;
import com.lukete.datagit.cli.render.renderer.TextSchemaDiffRenderer;
import com.lukete.datagit.core.domain.diff.SchemaDiffResult;
import com.lukete.datagit.core.domain.diff.TableSchemaDiff;
import com.lukete.datagit.core.domain.schema.ColumnChange;
import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.TableSchema;

class SchemaDiffRendererTest {
    @Test
    void shouldRenderTextSchemaDiffSectionsAndLabels() {
        RenderTarget target = new RenderTarget();
        TextSchemaDiffRenderer renderer = new TextSchemaDiffRenderer(target.printer());

        renderer.render("HEAD~1", "HEAD", diffWithChanges());

        String output = target.output();
        assertThat(output).contains("Comparing HEAD~1 -> HEAD");
        assertThat(output).contains("+ Table created: teams");
        assertThat(output).contains("id integer NOT NULL");
        assertThat(output).contains("name text NULL");
        assertThat(output).contains("- Table removed: audit_events");
        assertThat(output).contains("Table: users");
        assertThat(output).contains("+ column created:");
        assertThat(output).contains("email text NULL");
        assertThat(output).contains("- column removed:");
        assertThat(output).contains("legacy_code text NULL");
        assertThat(output).contains("~ column updated:");
        assertThat(output).contains("id integer -> bigint");
    }

    @Test
    void shouldRenderTextSchemaDiffInStableOrder() {
        RenderTarget target = new RenderTarget();
        TextSchemaDiffRenderer renderer = new TextSchemaDiffRenderer(target.printer());

        renderer.render("LEFT", "RIGHT", unorderedDiff());

        String output = target.output();
        assertThat(output).containsSubsequence(
                "+ Table created: a_table",
                "+ Table created: z_table",
                "- Table removed: m_table",
                "- Table removed: y_table",
                "Table: alpha",
                "Table: beta");
    }

    @Test
    void shouldRenderNoSchemaDifferencesForEmptyDiff() {
        RenderTarget target = new RenderTarget();
        TextSchemaDiffRenderer renderer = new TextSchemaDiffRenderer(target.printer());

        renderer.render("LEFT", "RIGHT", new SchemaDiffResult(List.of(), List.of(), Map.of()));

        assertThat(target.output()).contains("No schema differences found.");
    }

    @Test
    void shouldRenderSchemaDiffAsValidJson() throws Exception {
        RenderTarget target = new RenderTarget();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaDiffRenderer renderer = new JsonSchemaDiffRenderer(target.printer(), objectMapper);

        renderer.render("HEAD~1", "HEAD", diffWithChanges());

        JsonNode json = objectMapper.readTree(target.output().substring(8));
        assertThat(json.get("leftRef").asText()).isEqualTo("HEAD~1");
        assertThat(json.get("rightRef").asText()).isEqualTo("HEAD");
        assertThat(json.get("diff").get("createdTables").get(0).get("tableName").asText()).isEqualTo("teams");
        assertThat(json.get("diff").get("deletedTables").get(0).get("tableName").asText()).isEqualTo("audit_events");

        JsonNode users = json.get("diff").get("updatedTables").get("users");
        assertThat(users.get("createdColumns").get(0).get("name").asText()).isEqualTo("email");
        assertThat(users.get("deletedColumns").get(0).get("name").asText()).isEqualTo("legacy_code");
        assertThat(users.get("updatedColumns").get(0).get("before").get("type").asText()).isEqualTo("integer");
        assertThat(users.get("updatedColumns").get(0).get("after").get("type").asText()).isEqualTo("bigint");
    }

    private static SchemaDiffResult diffWithChanges() {
        Map<String, TableSchemaDiff> updatedTables = new LinkedHashMap<>();
        updatedTables.put("users", new TableSchemaDiff(
                List.of(column("email", "text", true)),
                List.of(column("legacy_code", "text", true)),
                List.of(new ColumnChange(
                        column("id", "integer", false),
                        column("id", "bigint", false)))));

        return new SchemaDiffResult(
                List.of(table("teams",
                        column("id", "integer", false),
                        column("name", "text", true))),
                List.of(table("audit_events", column("id", "integer", false))),
                updatedTables);
    }

    private static SchemaDiffResult unorderedDiff() {
        Map<String, TableSchemaDiff> updatedTables = new LinkedHashMap<>();
        updatedTables.put("beta", new TableSchemaDiff(List.of(column("b", "text", true)), List.of(), List.of()));
        updatedTables.put("alpha", new TableSchemaDiff(List.of(column("a", "text", true)), List.of(), List.of()));

        return new SchemaDiffResult(
                List.of(
                        table("z_table", column("id", "integer", false)),
                        table("a_table", column("id", "integer", false))),
                List.of(
                        table("y_table", column("id", "integer", false)),
                        table("m_table", column("id", "integer", false))),
                updatedTables);
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

    private static class RenderTarget {
        private final StringWriter out = new StringWriter();
        private final StringWriter err = new StringWriter();

        CliPrinter printer() {
            return new CliPrinter(new PrintWriter(out, true), new PrintWriter(err, true));
        }

        String output() {
            return out.toString();
        }
    }
}
