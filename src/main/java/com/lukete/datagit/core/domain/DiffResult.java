package com.lukete.datagit.core.domain;

import java.util.Map;

/**
 * Represents the difference between two snapshots.
 *
 * @param oldId the identifier of the baseline snapshot
 * @param newId the identifier of the compared snapshot
 * @param tables the detected changes grouped by table name
 */
public record DiffResult(
        String oldId,
        String newId,
        Map<String, TableDiff> tables) {

}
