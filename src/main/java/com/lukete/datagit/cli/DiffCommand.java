package com.lukete.datagit.cli;

import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.RowChangeType;
import com.lukete.datagit.core.domain.TableDiff;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.ReferenceResolver;

import static com.lukete.datagit.core.util.JsonUtils.toJson;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "diff", description = "Compare two snapshots.")

@RequiredArgsConstructor
@Slf4j
public class DiffCommand implements Runnable {
    @Parameters(index = "0", description = "ID of the old snapshot (defaults to HEAD~1)", defaultValue = "HEAD~1")
    private String oldId = "HEAD~1";

    @Parameters(index = "1", description = "ID of the new snapshot (defaults to HEAD)", defaultValue = "HEAD")
    private String newId = "HEAD";

    @Option(names = { "-v",
            "--verbose" }, defaultValue = "false", fallbackValue = "true", description = "Display as object. If false, display as list.")
    private boolean verbose = false;

    private final DiffService service;
    private final ReferenceResolver refResolver;

    @Override
    public void run() {
        var oldSnap = refResolver.resolve(oldId);
        var newSnap = refResolver.resolve(newId);
        log.info(Boolean.toString(verbose));

        if (!verbose) {
            Map<String, TableDiff> tableDiffs = service.compareTableDiffs(oldSnap, newSnap);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('\n');

            if (allTableDiffsEmpty(tableDiffs)) {
                stringBuilder.append("No differences between the snapshots.");
            } else {
                appendTableDiffs(tableDiffs, stringBuilder);
            }

            log.info(stringBuilder.toString());
            return;
        }

        var diff = service.compare(oldSnap, newSnap);
        log.info(toJson(diff));
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