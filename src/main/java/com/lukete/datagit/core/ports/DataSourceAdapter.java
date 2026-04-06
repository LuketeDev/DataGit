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
     * @return a complete Snapshot of the data source
     */
    Snapshot extract();
}