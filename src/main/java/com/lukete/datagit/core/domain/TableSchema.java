package com.lukete.datagit.core.domain;

import java.util.Map;

public record TableSchema(
        String tableName,
        Map<String, ColumnSchema> columns) {

}
