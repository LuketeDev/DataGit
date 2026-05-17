package com.lukete.datagit.core.domain.schema;

import java.util.Map;

public record SchemaSnapshot(
        Map<String, TableSchema> tables) {
}
