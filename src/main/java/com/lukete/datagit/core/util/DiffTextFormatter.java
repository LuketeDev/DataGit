package com.lukete.datagit.core.util;

import java.util.List;
import java.util.Map;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.RowChangeType;
import com.lukete.datagit.core.domain.TableDiff;

public class DiffTextFormatter {
    public String format(DiffResult diffResult) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');

        Map<String, TableDiff> tableDiffs = diffResult.tables();
        if (allTableDiffsEmpty(tableDiffs)) {
            sb.append("No differences between the snapshots ").append(diffResult.oldId()).append(" and ")
                    .append(diffResult.newId());
            return sb.toString();
        }

        appendTableDiffs(tableDiffs, sb);
        return sb.toString();
    }

    private void appendTableDiffs(Map<String, TableDiff> tableDiffs, StringBuilder sb) {
        tableDiffs.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String tableName = entry.getKey();
                    TableDiff tableDiff = entry.getValue();

                    if (isTableDiffEmpty(tableDiff)) {
                        return;
                    }

                    sb.append("Table: ").append(tableName).append("\n");
                    appendChanges(tableDiff.inserted(), RowChangeType.INSERTED, sb);
                    appendChanges(tableDiff.deleted(), RowChangeType.DELETED, sb);
                    appendChanges(tableDiff.updated(), RowChangeType.UPDATED, sb);
                    sb.append('\n');
                });
    }

    private void appendChanges(List<RowChange> changes, RowChangeType type, StringBuilder sb) {
        if (changes.isEmpty()) {
            return;
        }

        sb.append("  ").append(type.getLabel()).append(":\n");

        for (RowChange change : changes) {
            switch (type) {
                case INSERTED -> sb.append("    + ").append(change.after()).append("\n");
                case DELETED -> sb.append("    - ").append(change.before()).append("\n");
                case UPDATED ->
                    sb.append("    ~ ")
                            .append(change.before())
                            .append('\n')
                            .append("      -> ")
                            .append(change.after())
                            .append("\n");
                default -> throw new IllegalArgumentException("Unknown change type: " + type);
            }
        }
    }

    private boolean isTableDiffEmpty(TableDiff diff) {
        return diff.inserted().isEmpty()
                && diff.deleted().isEmpty()
                && diff.updated().isEmpty();
    }

    private boolean allTableDiffsEmpty(Map<String, TableDiff> tableDiffs) {
        for (TableDiff diff : tableDiffs.values()) {
            if (!isTableDiffEmpty(diff)) {
                return false;
            }
        }
        return true;
    }

}
