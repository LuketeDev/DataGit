package com.lukete.datagit.cli.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.domain.FieldChange;
import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.TableDiff;

class DiffRendererTest {
    @Test
    void shouldRenderInsertedUpdatedAndDeletedSections() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());

        renderer.render("HEAD~1", "HEAD", diffWithChanges());

        String output = target.output();

        assertThat(output).contains("+ inserted: 1");
        assertThat(output).contains("+ id=2 name=inserted");
        assertThat(output).contains("~ updated: 1");
        assertThat(output).contains("~ id=3");
        assertThat(output).contains("name: before -> after");
        assertThat(output).contains("- deleted: 1");
        assertThat(output).contains("- id=1 name=deleted");
        assertThat(output).doesNotContain("name=before");
        assertThat(output).doesNotContain("name=after");
    }

    @Test
    void shouldRenderComparingHeaderExactlyOnce() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());

        renderer.render("LEFT", "RIGHT", diffWithChanges());

        assertThat(countOccurrences(target.output(), "Comparing LEFT -> RIGHT")).isEqualTo(1);
    }

    @Test
    void shouldRenderNoDifferencesFoundForEmptyDiff() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());

        renderer.render("HEAD~1", "HEAD", new DiffResult("old", "new", Map.of()));

        assertThat(target.output()).contains("No differences found.");
    }

    @Test
    void shouldRenderMissingUpdatedRowIdAsQuestionMark() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());
        DiffResult diff = new DiffResult("old", "new", Map.of(
                "users", new TableDiff(
                        List.of(),
                        List.of(),
                        List.of(new RowChange(
                                Map.of("name", "before"),
                                Map.of("name", "after"),
                                List.of(new FieldChange("name", "before", "after")))))));

        renderer.render("LEFT", "RIGHT", diff);

        assertThat(target.output()).contains("~ id=?");
        assertThat(target.output()).contains("name: before -> after");
    }

    @Test
    void shouldSkipTablesWithNoChanges() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());
        DiffResult diff = new DiffResult("old", "new", Map.of(
                "audit", new TableDiff(List.of(), List.of(), List.of()),
                "users", new TableDiff(
                        List.of(),
                        List.of(new RowChange(null, Map.of("id", 2, "name", "inserted"), List.of())),
                        List.of())));

        renderer.render("LEFT", "RIGHT", diff);

        assertThat(target.output()).doesNotContain("Table: audit");
        assertThat(target.output()).contains("Table: users");
    }

    @Test
    void shouldRenderValidJson() throws Exception {
        RenderTarget target = new RenderTarget();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonDiffRenderer renderer = new JsonDiffRenderer(target.printer(), objectMapper);

        renderer.render("HEAD~1", "HEAD", diffWithChanges());

        JsonNode json = objectMapper.readTree(target.output().substring(8));
        assertThat(json.get("leftRef").asText()).isEqualTo("HEAD~1");
        assertThat(json.get("rightRef").asText()).isEqualTo("HEAD");
        assertThat(json.get("diff").get("tables").has("users")).isTrue();

        JsonNode users = json.get("diff").get("tables").get("users");
        JsonNode fieldChange = users.get("updated").get(0).get("fieldChanges").get(0);
        assertThat(fieldChange.get("field").asText()).isEqualTo("name");
        assertThat(fieldChange.get("before").asText()).isEqualTo("before");
        assertThat(fieldChange.get("after").asText()).isEqualTo("after");
        assertThat(users.get("inserted").get(0).get("fieldChanges")).isEmpty();
        assertThat(users.get("deleted").get(0).get("fieldChanges")).isEmpty();
    }

    private static DiffResult diffWithChanges() {
        TableDiff tableDiff = new TableDiff(
                List.of(new RowChange(Map.of("id", 1, "name", "deleted"), null, List.of())),
                List.of(new RowChange(null, Map.of("id", 2, "name", "inserted"), List.of())),
                List.of(new RowChange(
                        Map.of("id", 3, "name", "before"),
                        Map.of("id", 3, "name", "after"),
                        List.of(new FieldChange("name", "before", "after")))));

        return new DiffResult("old", "new", Map.of("users", tableDiff));
    }

    private static int countOccurrences(String value, String needle) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
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
