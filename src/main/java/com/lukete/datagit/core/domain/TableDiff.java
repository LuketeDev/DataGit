package com.lukete.datagit.core.domain;

import java.util.List;

/**
 * Represents changes inside a table.
 */
public record TableDiff(
        List<RowChange> deleted,
        List<RowChange> inserted,
        List<RowChange> updated) {

}
