package com.lukete.datagit.core.ports;

import com.lukete.datagit.core.domain.Snapshot;

/**
 * Contract for extracting data from a data source.
 * Each database implementation must provide its own adapter.
 */
public interface DataSourceAdapter {

    /**
     * Extracts the full state of the data source.
     *
     * @return a complete snapshot of the data source
     */
    Snapshot extract();

    void restore(Snapshot snapshot);
}
