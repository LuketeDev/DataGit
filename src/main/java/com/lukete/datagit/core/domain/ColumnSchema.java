package com.lukete.datagit.core.domain;

public record ColumnSchema(
        String name,
        String type,
        boolean nullable) {
}
