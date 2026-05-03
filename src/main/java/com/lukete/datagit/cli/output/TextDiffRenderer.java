package com.lukete.datagit.cli.output;

import java.util.Map;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.TableDiff;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextDiffRenderer implements DiffRenderer {
    private final CliPrinter printer;

    @Override
    public void render(String leftRef, String rightRef, DiffResult diffResult) {
        printer.info("Comparing " + leftRef + " -> " + rightRef);
        printer.info("Comparing " + leftRef + " -> " + rightRef);
        printer.blankLine();

        if (diffResult == null || diffResult.tables() == null || diffResult.tables().isEmpty()) {
            printer.info("No differences found.");
            return;
        }

        boolean printedAnyTable = false;

        for (Map.Entry<String, TableDiff> entry : diffResult.tables().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            String tableName = entry.getKey();
            TableDiff tableDiff = entry.getValue();

            if (isDiffEmpty(tableDiff)) {
                continue;
            }

            printedAnyTable = true;

            printer.info("Table: " + tableName);
            renderInserted(tableDiff);
            renderUpdated(tableDiff);
            renderDeleted(tableDiff);
            printer.blankLine();
        }
        if (!printedAnyTable) {
            printer.info("No differences found.");
        }
    }

    private void renderInserted(TableDiff tableDiff) {
        if (tableDiff.inserted() == null || tableDiff.inserted().isEmpty()) {
            return;
        }
        printer.info("  + inserted: " + tableDiff.inserted().size());

        for (RowChange change : tableDiff.inserted()) {
            printer.info("    + " + summarizeRow(change.after()));
        }
        printer.blankLine();
    }

    private void renderUpdated(TableDiff tableDiff) {
        if (tableDiff.updated() == null || tableDiff.updated().isEmpty()) {
            return;
        }

        printer.info("  ~ updated: " + tableDiff.updated().size());

        for (RowChange change : tableDiff.updated()) {
            Object id = extractId(change.before(), change.after());
            printer.info("    ~ id=" + (id == null ? "?" : id));
            printer.info("      before: " + renderRow(change.before()));
            printer.info("      after:  " + renderRow(change.after()));
        }

        printer.blankLine();
    }

    private void renderDeleted(TableDiff tableDiff) {
        if (tableDiff.deleted() == null || tableDiff.deleted().isEmpty()) {
            return;
        }

        printer.info("  - deleted: " + tableDiff.deleted().size());

        for (RowChange change : tableDiff.deleted()) {
            printer.info("    - " + summarizeRow(change.before()));
        }

        printer.blankLine();
    }

    private boolean isDiffEmpty(TableDiff diff) {
        return ((diff.inserted() == null || diff.inserted().isEmpty())
                && (diff.updated() == null || diff.updated().isEmpty())
                && (diff.deleted() == null || diff.deleted().isEmpty()));

    }

    private String summarizeRow(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return "{}";
        }
        return row.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + " " + right)
                .orElse("{}");
    }

    private Object extractId(Map<String, Object> before, Map<String, Object> after) {
        if (before != null && before.containsKey("id")) {
            return before.get("id");
        }
        if (after != null && after.containsKey("id")) {
            return after.get("id");
        }

        return null;
    }

    private String renderRow(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return "{}";
        }

        return row.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .map(value -> "{" + value + "}")
                .orElse("{}");
    }
}