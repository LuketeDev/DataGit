package com.lukete.datagit.core.domain;

import java.util.Map;

public record SchemaSnapshot(
                Map<String, TableSchema> tables) {
}
