package com.lukete.datagit.core.domain;

import java.util.List;

public record RestorePlan(
        String snapshotId,
        String source,
        int tableCount,
        int rowCount, // sum rowCount for each RestoreTablePlan
        List<RestoreTablePlan> tables) {
}