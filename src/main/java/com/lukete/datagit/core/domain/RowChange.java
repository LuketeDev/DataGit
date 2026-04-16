package com.lukete.datagit.core.domain;

import java.util.Map;

/**
 * Represents a row-level change between two snapshot versions.
 *
 * @param before the row state in the old snapshot, or {@code null} for inserted rows
 * @param after the row state in the new snapshot, or {@code null} for deleted rows
 */
public record RowChange(
        Map<String, Object> before,
        Map<String, Object> after) {

}
