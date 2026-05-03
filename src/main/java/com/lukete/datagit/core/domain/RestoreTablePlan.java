package com.lukete.datagit.core.domain;

public record RestoreTablePlan(
        String tableName,
        int rowCount) {
}