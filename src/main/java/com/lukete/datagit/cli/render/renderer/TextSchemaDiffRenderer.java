package com.lukete.datagit.cli.render.renderer;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lukete.datagit.cli.render.CliPrinter;
import com.lukete.datagit.core.domain.diff.SchemaDiffResult;
import com.lukete.datagit.core.domain.diff.TableSchemaDiff;
import com.lukete.datagit.core.domain.schema.ColumnChange;
import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.TableSchema;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextSchemaDiffRenderer implements SchemaDiffRenderer {
    private final CliPrinter printer;

    @Override
    public void render(String leftRef, String rightRef, SchemaDiffResult diffResult) {
        printer.info("Comparing " + leftRef + " -> " + rightRef);
        printer.blankLine();

        if (isEmpty(diffResult)) {
            printer.info("No schema differences found.");
            return;
        }

        var createdTables = diffResult.createdTables().stream()
                .sorted(Comparator.comparing(TableSchema::tableName)).toList();
        var deletedTables = diffResult.deletedTables().stream()
                .sorted(Comparator.comparing(TableSchema::tableName)).toList();
        var updatedTables = diffResult.updatedTables()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        renderCreatedTables(createdTables);
        renderDeletedTables(deletedTables);
        renderUpdatedTables(updatedTables);

        printer.blankLine();
    }

    private void renderCreatedTables(List<TableSchema> tables) {
        for (TableSchema table : tables) {
            printer.info("+ Table created: " + table.tableName());
            printer.blankLine();

            renderColumns(table.columns());

            printer.blankLine();
        }

    }

    private void renderDeletedTables(List<TableSchema> tables) {
        for (TableSchema table : tables) {
            printer.info("- Table removed: " + table.tableName());
            printer.blankLine();

            renderColumns(table.columns());

            printer.blankLine();
        }

    }

    private void renderUpdatedTables(Map<String, TableSchemaDiff> tableSchemaDiffs) {
        for (var entry : tableSchemaDiffs.entrySet()) {
            String tableName = entry.getKey();
            TableSchemaDiff diff = entry.getValue();

            printer.info("Table: " + tableName);

            renderCreatedColumns(diff);
            renderRemovedColumns(diff);
            renderUpdatedColumns(diff);

            printer.blankLine();
        }
    }

    private void renderCreatedColumns(TableSchemaDiff diff) {

        if (diff.createdColumns().isEmpty()) {
            return;
        }

        printer.info("  + column created:");

        for (ColumnSchema column : diff.createdColumns()) {
            printer.info("    " + formatColumn(column));
        }

        printer.blankLine();
    }

    private void renderRemovedColumns(TableSchemaDiff diff) {

        if (diff.deletedColumns().isEmpty()) {
            return;
        }

        printer.info("  - column removed:");

        for (ColumnSchema column : diff.deletedColumns()) {
            printer.info("    " + formatColumn(column));
        }

        printer.blankLine();
    }

    private void renderUpdatedColumns(TableSchemaDiff diff) {

        if (diff.updatedColumns().isEmpty()) {
            return;
        }

        printer.info("  ~ column updated:");

        for (ColumnChange change : diff.updatedColumns()) {

            printer.info("    "
                    + change.before().name()
                    + " "
                    + change.before().type()
                    + " -> "
                    + change.after().type());
        }

        printer.blankLine();
    }

    private void renderColumns(Map<String, ColumnSchema> columns) {
        columns.values().stream().sorted(Comparator.comparing(ColumnSchema::name)).forEach(this::renderColumn);
    }

    private void renderColumn(ColumnSchema column) {
        printer.info("    " + formatColumn(column));
    }

    private String formatColumn(ColumnSchema column) {

        return column.name()
                + " "
                + column.type()
                + " "
                + (column.nullable() ? "NULL" : "NOT NULL");
    }

    private boolean isEmpty(SchemaDiffResult diffResult) {

        return diffResult.createdTables().isEmpty()
                && diffResult.deletedTables().isEmpty()
                && diffResult.updatedTables().isEmpty();
    }
}