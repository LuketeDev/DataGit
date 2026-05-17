package com.lukete.datagit.core.domain.diff;

import java.util.List;

import com.lukete.datagit.core.domain.schema.ColumnChange;
import com.lukete.datagit.core.domain.schema.ColumnSchema;

public record TableSchemaDiff(
        List<ColumnSchema> createdColumns,
        List<ColumnSchema> deletedColumns,
        List<ColumnChange> updatedColumns) {
}
