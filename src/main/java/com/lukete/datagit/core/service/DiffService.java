package com.lukete.datagit.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.domain.RowChange;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.domain.TableDiff;

/**
 * Compares two snapshots and generates a diff
 */
public class DiffService {
    public DiffResult compare(Snapshot oldSnap, Snapshot newSnap) {
        Map<String, TableDiff> tableDiffs = new HashMap<>();

        Set<String> allTables = new HashSet<>();
        allTables.addAll(oldSnap.tables().keySet());
        allTables.addAll(newSnap.tables().keySet());
        for (String tableName : allTables) {
            var oldRows = oldSnap.tables().getOrDefault(tableName, List.of());
            var newRows = newSnap.tables().getOrDefault(tableName, List.of());

            TableDiff diff = diffTable(oldRows, newRows);
            tableDiffs.put(tableName, diff);
        }

        return new DiffResult(oldSnap.id(), newSnap.id(), tableDiffs);
    }

    private TableDiff diffTable(
            List<Map<String, Object>> oldRows,
            List<Map<String, Object>> newRows) {
        List<RowChange> inserted = new ArrayList<>();
        List<RowChange> deleted = new ArrayList<>();
        List<RowChange> updated = new ArrayList<>();

        Map<Object, Map<String, Object>> oldMap = indexById(oldRows);
        Map<Object, Map<String, Object>> newMap = indexById(newRows);

        // Detect inserts and updates
        for (var entry : newMap.entrySet()) {
            Object id = entry.getKey();
            Map<String, Object> newRow = entry.getValue();

            // Existed in old snapshot?
            if (!oldMap.containsKey(id)) {
                inserted.add(new RowChange(null, newRow));
            } else {
                Map<String, Object> oldRow = oldMap.get(id);
                // Is new row different from self in old snapshot?
                if (!oldRow.equals(newRow)) {
                    updated.add(new RowChange(oldRow, newRow));
                }
            }
        }
        for (var entry : oldMap.entrySet()) {
            Object id = entry.getKey();
            if (!newMap.containsKey(id)) {
                deleted.add(new RowChange(entry.getValue(), null));
            }
        }
        return new TableDiff(deleted, inserted, updated);
    }

    /**
     * Assumes every table has an "id" column.
     */
    private Map<Object, Map<String, Object>> indexById(
            List<Map<String, Object>> rows) {
        Map<Object, Map<String, Object>> map = new HashMap<>();

        for (var row : rows) {
            Object id = row.get("id");

            if (id != null) {
                map.put(id, row);
            }
        }

        return map;
    }
}
