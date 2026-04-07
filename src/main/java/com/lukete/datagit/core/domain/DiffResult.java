package com.lukete.datagit.core.domain;

import java.util.Map;

/**
 * Represents the difference between two snapshots.
 */
public record DiffResult(
                Map<String, TableDiff> tables) {

}
