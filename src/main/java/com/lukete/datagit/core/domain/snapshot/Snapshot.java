package com.lukete.datagit.core.domain.snapshot;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.lukete.datagit.core.domain.schema.SchemaSnapshot;

/**
 * Represents the full state of a data source captured at a specific point in
 * time.
 *
 * @param id        the unique snapshot identifier
 * @param timestamp the capture time of the snapshot
 * @param source    the name of the data source implementation
 * @param tables    a map keyed by table name containing the rows captured for
 *                  each table
 */
public record Snapshot(
        String id,
        Instant timestamp,
        String source,
        Map<String, List<Map<String, Object>>> tables,
        SchemaSnapshot schema) {
}
