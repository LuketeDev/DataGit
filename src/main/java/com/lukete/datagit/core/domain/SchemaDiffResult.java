package com.lukete.datagit.core.domain;

import java.util.List;
import java.util.Map;

public record SchemaDiffResult(
        List<TableSchema> createdTables,
        List<TableSchema> deletedTables,
        Map<String, TableSchemaDiff> updatedTables) {
}