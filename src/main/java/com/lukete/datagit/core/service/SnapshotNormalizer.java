package com.lukete.datagit.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.lukete.datagit.core.domain.Snapshot;

public class SnapshotNormalizer {
    public Snapshot normalize(Snapshot snapshot, List<String> ignoredColumns) {
        if (snapshot == null) {
            return null;
        }

        if (ignoredColumns == null || ignoredColumns.isEmpty()) {
            return snapshot;
        }

        Set<String> ignored = ignoredColumns.stream()
                .filter(column -> column != null && !column.isBlank())
                .collect(Collectors.toSet());

        Map<String, List<Map<String, Object>>> normalizedTables = new HashMap<>();

        for (var tableEntry : snapshot.tables().entrySet()) {
            List<Map<String, Object>> normalizedRows = tableEntry.getValue().stream()
                    .map(row -> normalizeRow(row, ignored))
                    .toList();

            normalizedTables.put(tableEntry.getKey(), normalizedRows);
        }

        return new Snapshot(
                snapshot.id(),
                snapshot.timestamp(),
                snapshot.source(),
                normalizedTables);
    }

    private Map<String, Object> normalizeRow(Map<String, Object> row, Set<String> ignored) {
        Map<String, Object> normalizedRow = new HashMap<>();

        for (var entry : row.entrySet()) {
            if (!ignored.contains(entry.getKey())) {
                normalizedRow.put(entry.getKey(), entry.getValue());
            }
        }

        return normalizedRow;
    }
}
