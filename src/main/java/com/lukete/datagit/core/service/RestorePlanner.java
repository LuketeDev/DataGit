package com.lukete.datagit.core.service;

import com.lukete.datagit.core.domain.restore.RestorePlan;
import com.lukete.datagit.core.domain.restore.RestoreTablePlan;
import com.lukete.datagit.core.domain.snapshot.Snapshot;

public class RestorePlanner {
        public RestorePlan plan(Snapshot snapshot) {
                var tables = snapshot
                                .tables()
                                .entrySet()
                                .stream()
                                .map(entry -> new RestoreTablePlan(entry.getKey(),
                                                entry.getValue() == null ? 0 : entry.getValue().size()))
                                .toList();

                int totalRows = tables.stream().mapToInt(RestoreTablePlan::rowCount).sum();

                return new RestorePlan(
                                snapshot.id(),
                                snapshot.source(),
                                tables.size(),
                                totalRows,
                                tables);
        }
}
