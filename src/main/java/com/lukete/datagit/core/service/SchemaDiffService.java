package com.lukete.datagit.core.service;

import com.lukete.datagit.core.domain.diff.SchemaDiffResult;
import com.lukete.datagit.core.domain.diff.TableSchemaDiff;
import com.lukete.datagit.core.domain.schema.ColumnChange;
import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.schema.TableSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchemaDiffService {

    public SchemaDiffResult compare(SchemaSnapshot oldSchema, SchemaSnapshot newSchema) {
        List<TableSchema> createdTables = new ArrayList<>();
        List<TableSchema> deletedTables = new ArrayList<>();
        Map<String, TableSchemaDiff> updatedTables = new HashMap<>();

        Map<String, TableSchema> oldTables = oldSchema.tables();
        Map<String, TableSchema> newTables = newSchema.tables();

        // Detect inserts
        for (var entry : newTables.entrySet()) {
            String tableName = entry.getKey();

            // Existed in old snapshot?
            if (!oldTables.containsKey(tableName)) {
                createdTables.add(entry.getValue());
            }
        }

        for (var entry : oldTables.entrySet()) {
            String tableName = entry.getKey();
            if (!newTables.containsKey(tableName)) {
                deletedTables.add(entry.getValue());
            }
        }

        for (Map.Entry<String, TableSchema> table : oldTables.entrySet()) {
            if (!newTables.containsKey(table.getKey())) {
                continue;
            }
            TableSchema oldTable = table.getValue();
            TableSchema newTable = newTables.get(table.getKey());

            TableSchemaDiff tableSchemaDiff = diffTable(oldTable, newTable);

            if (!isEmpty(tableSchemaDiff)) {
                updatedTables.put(table.getKey(), tableSchemaDiff);
            }
        }
        return new SchemaDiffResult(createdTables, deletedTables, updatedTables);
    }

    private TableSchemaDiff diffTable(
            TableSchema oldTable, TableSchema newTable) {
        List<ColumnSchema> created = new ArrayList<>();
        List<ColumnSchema> deleted = new ArrayList<>();
        List<ColumnChange> updated = new ArrayList<>();

        Map<String, ColumnSchema> oldColumns = oldTable.columns();
        Map<String, ColumnSchema> newColumns = newTable.columns();

        // Detect inserts and updates
        for (var entry : newColumns.entrySet()) {
            String columnName = entry.getKey();
            ColumnSchema newColumn = entry.getValue();

            // (not) Existed in old snapshot?
            if (!oldColumns.containsKey(columnName)) {
                created.add(newColumn);
            } else {
                ColumnSchema oldColumn = oldColumns.get(columnName);
                // Is new row different from self in old snapshot?
                if (!oldColumn.equals(newColumn)) {
                    updated.add(new ColumnChange(oldColumn, newColumn));
                }
            }
        }
        for (var entry : oldColumns.entrySet()) {
            String tableName = entry.getKey();
            if (!newColumns.containsKey(tableName)) {
                deleted.add(entry.getValue());
            }
        }
        return new TableSchemaDiff(created, deleted, updated);
    }

    private boolean isEmpty(TableSchemaDiff diff) {
        return diff.createdColumns().isEmpty()
                && diff.deletedColumns().isEmpty()
                && diff.updatedColumns().isEmpty();
    }
}
