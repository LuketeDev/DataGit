package com.lukete.datagit.core.domain.schema;

public record ColumnSchema(
                String name,
                String type,
                boolean nullable) {
}
