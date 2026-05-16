package com.lukete.datagit.core.domain;

import java.util.List;

public record TableSchemaDiff(
                List<ColumnSchema> createdColumns,
                List<ColumnSchema> deletedColumns,
                List<ColumnChanges> updatedColumns) {
}
