package com.lukete.datagit.core.domain.diff;

import java.util.List;
import java.util.Map;

import com.lukete.datagit.core.domain.schema.TableSchema;

public record SchemaDiffResult(
        List<TableSchema> createdTables,
        List<TableSchema> deletedTables,
        Map<String, TableSchemaDiff> updatedTables) {
}