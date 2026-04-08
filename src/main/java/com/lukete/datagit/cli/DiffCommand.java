package com.lukete.datagit.cli;

import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.TableDiff;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.ReferenceResolver;

import static com.lukete.datagit.core.util.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

// TODO Defaults to latest and previous.
@Command(name = "diff", description = "Compare two snapshots.")

@RequiredArgsConstructor
@Slf4j
public class DiffCommand implements Runnable {
    @Parameters(index = "0", description = "ID of the old snapshot (defaults to HEAD~1)")
    private String oldId = "HEAD~1";

    @Parameters(index = "1", description = "ID of the new snapshot (defaults to HEAD)")
    private String newId = "HEAD";

    @Option(names = { "-v",
            "--verbose" }, negatable = true, defaultValue = "true", fallbackValue = "true", description = "Display as object. If false, display as list.")
    private boolean verbose;

    private final DiffService service;
    private final ReferenceResolver refResolver;

    @Override
    public void run() {
        var oldSnap = refResolver.resolve(oldId);
        var newSnap = refResolver.resolve(newId);

        if (!verbose) {
            Map<String, TableDiff> tableDiffs = service.compareTableDiffs(oldSnap, newSnap);
            List<RowChange> deletedList = new ArrayList<>();
            List<RowChange> insertedList = new ArrayList<>();
            List<RowChange> updatedList = new ArrayList<>();

            listChanges(tableDiffs, deletedList, insertedList, updatedList);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('\n');
            if (isAllEmpty(insertedList, deletedList, updatedList)) {
                stringBuilder.append("No differences between the snapshots.");
            } else {
                addToBuilder(insertedList, "inserted", stringBuilder);
                addToBuilder(deletedList, "deleted", stringBuilder);
                addToBuilder(updatedList, "updated", stringBuilder);
            }
            log.info(stringBuilder.toString());
            return;
        }

        var diff = service.compare(oldSnap, newSnap);
        log.info(toJson(diff));
    }

    private void addToBuilder(List<RowChange> changes, String type, StringBuilder sb) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        sb.append(type).append(":\n");

        for (RowChange change : changes) {
            switch (type) {
                case "inserted":
                    sb.append("+ ").append(change.after()).append("\n");
                    break;

                case "deleted":
                    sb.append("- ").append(change.before()).append("\n");
                    break;

                default: // updated
                    sb.append("~ ")
                            .append(change.before())
                            .append(" -> ")
                            .append(change.after())
                            .append("\n");
                    break;
            }
        }
    }

    private void listChanges(Map<String, TableDiff> tableDiffs, List<RowChange> deletedList,
            List<RowChange> insertedList,
            List<RowChange> updatedList) {
        for (var entry : tableDiffs.entrySet()) {
            List<RowChange> deleted = entry.getValue().deleted();
            List<RowChange> inserted = entry.getValue().created();
            List<RowChange> updated = entry.getValue().updated();

            deletedList.addAll(deleted);
            insertedList.addAll(inserted);
            updatedList.addAll(updated);
        }
    }

    private boolean isAllEmpty(Object... args) {
        for (Object arg : args) {
            if (!(arg instanceof List<?> list) || !list.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}