package com.lukete.datagit.core.domain.schema;

import java.util.Map;

public record TableSchema(
                String tableName,
                Map<String, ColumnSchema> columns) {

}
