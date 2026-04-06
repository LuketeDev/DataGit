package com.lukete.datagit.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// Represents a full snapshot of a data source at a given moment in time.
public record Snapshot(
        String id,
        Instant timestamp,
        String source,

        // Map of List of Map
        // String:table_name, List:rows, String:collumn, Object:value
        Map<String, List<Map<String, Object>>> tables) {
}