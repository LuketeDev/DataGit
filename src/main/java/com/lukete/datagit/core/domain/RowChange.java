package com.lukete.datagit.core.domain;

import java.util.List;
import java.util.Map;

/**
 * Represents a row-level change between two snapshot versions.
 *
 * @param before       the row state in the old snapshot, or {@code null} for
 *                     inserted rows
 * @param after        the row state in the new snapshot, or {@code null} for
 *                     deleted rows
 * @param fieldChanges the field-level changes for updated rows
 */
public record RowChange(
        Map<String, Object> before,
        Map<String, Object> after,
        List<FieldChange> fieldChanges) {

    // Prevents null
    public RowChange {
        fieldChanges = fieldChanges == null ? List.of() : List.copyOf(fieldChanges);
    }
}
