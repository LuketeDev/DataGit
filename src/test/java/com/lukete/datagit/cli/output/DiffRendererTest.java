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
import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.TableDiff;

class DiffRendererTest {
    @Test
    void shouldRenderInsertedUpdatedAndDeletedSections() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());

        renderer.render("HEAD~1", "HEAD", diffWithChanges());

        assertThat(target.output()).contains("+ inserted: 1");
        assertThat(target.output()).contains("~ updated: 1");
        assertThat(target.output()).contains("- deleted: 1");
    }

    @Test
    void shouldRenderNoDifferencesFoundForEmptyDiff() {
        RenderTarget target = new RenderTarget();
        TextDiffRenderer renderer = new TextDiffRenderer(target.printer());

        renderer.render("HEAD~1", "HEAD", new DiffResult("old", "new", Map.of()));

        assertThat(target.output()).contains("No differences found.");
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
    }

    private static DiffResult diffWithChanges() {
        TableDiff tableDiff = new TableDiff(
                List.of(new RowChange(Map.of("id", 1, "name", "deleted"), null)),
                List.of(new RowChange(null, Map.of("id", 2, "name", "inserted"))),
                List.of(new RowChange(Map.of("id", 3, "name", "before"), Map.of("id", 3, "name", "after"))));

        return new DiffResult("old", "new", Map.of("users", tableDiff));
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
