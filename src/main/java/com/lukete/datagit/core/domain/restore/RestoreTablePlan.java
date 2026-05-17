package com.lukete.datagit.core.domain.restore;

public record RestoreTablePlan(
                String tableName,
                int rowCount) {
}