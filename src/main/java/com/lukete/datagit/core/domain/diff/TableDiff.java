package com.lukete.datagit.core.domain.diff;

import java.util.List;

import com.lukete.datagit.core.domain.snapshot.RowChange;

/**
 * Represents changes inside a table.
 *
 * @param deleted  rows that existed only in the old snapshot
 * @param inserted rows that exist only in the new snapshot
 * @param updated  rows present in both snapshots but with changed contents
 */
public record TableDiff(
        List<RowChange> deleted,
        List<RowChange> inserted,
        List<RowChange> updated
// ,TableSchemaDiff tableSchemaDiff
) {

}
