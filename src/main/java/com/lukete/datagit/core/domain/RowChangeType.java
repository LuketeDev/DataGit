package com.lukete.datagit.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumerates the supported row change categories shown in diff output.
 */
@Getter
@AllArgsConstructor
public enum RowChangeType {
    /** Row added in the new snapshot. */
    INSERTED("inserted"),
    /** Row removed from the new snapshot. */
    DELETED("deleted"),
    /** Row present in both snapshots with at least one changed value. */
    UPDATED("updated");

    private final String label;

}
