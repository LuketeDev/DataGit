package com.lukete.datagit.cli.output;

import com.lukete.datagit.core.domain.RestorePlan;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestorePlanRenderer {
    private final CliPrinter printer;

    public void render(RestorePlan plan) {
        printer.warn("This operation will overwrite current database data.");
        printer.info("Target snapshot: " + plan.snapshotId());
        printer.info("Source: " + plan.source());
        printer.info("Tables affected: " + plan.tableCount());
        printer.info("Rows to restore: " + plan.rowCount());
        printer.blankLine();

        printer.info(String.format("%-20s %s", "TABLE", "ROWS"));

        for (var table : plan.tables()) {
            printer.info(String.format("%-20s %s", table.tableName(), table.rowCount()));
        }
    }
}
