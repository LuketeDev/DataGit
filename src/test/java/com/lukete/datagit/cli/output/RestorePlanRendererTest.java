package com.lukete.datagit.cli.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.core.domain.RestorePlan;
import com.lukete.datagit.core.domain.RestoreTablePlan;

class RestorePlanRendererTest {

    @Test
    void shouldRenderWarningHeader() {
        RenderedOutput output = render(planWithTables());

        assertThat(output.combined()).contains("WARN").contains("overwrite current database data");
    }

    @Test
    void shouldRenderSnapshotIdSourceTableCountAndRowCount() {
        RenderedOutput output = render(planWithTables());

        assertThat(output.out())
                .contains("INFO")
                .contains("Target snapshot: snap-1")
                .contains("Source: postgres")
                .contains("Tables affected: 2")
                .contains("Rows to restore: 3");
    }

    @Test
    void shouldRenderTableRowsInAlignedFormat() {
        RenderedOutput output = render(planWithTables());

        assertThat(output.out())
                .contains("TABLE")
                .contains("ROWS")
                .contains("users")
                .contains("2")
                .contains("orders")
                .contains("1");
    }

    @Test
    void shouldHandleEmptyTablesGracefully() {
        RestorePlan plan = new RestorePlan("snap-empty", "postgres", 0, 0, List.of());

        RenderedOutput output = render(plan);

        assertThat(output.combined()).contains("WARN");
        assertThat(output.out())
                .contains("Target snapshot: snap-empty")
                .contains("Tables affected: 0")
                .contains("Rows to restore: 0")
                .contains("TABLE")
                .contains("ROWS");
    }

    private static RenderedOutput render(RestorePlan plan) {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        CliPrinter printer = new CliPrinter(new PrintWriter(out, true), new PrintWriter(err, true));

        new RestorePlanRenderer(printer).render(plan);

        return new RenderedOutput(out.toString(), err.toString());
    }

    private static RestorePlan planWithTables() {
        return new RestorePlan(
                "snap-1",
                "postgres",
                2,
                3,
                List.of(
                        new RestoreTablePlan("users", 2),
                        new RestoreTablePlan("orders", 1)));
    }

    private record RenderedOutput(String out, String err) {
        private String combined() {
            return out + err;
        }
    }
}
