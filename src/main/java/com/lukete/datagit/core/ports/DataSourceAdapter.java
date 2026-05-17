package com.lukete.datagit.core.ports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.snapshot.Snapshot;

/**
 * Contract for extracting data from a data source.
 * Each database implementation must provide its own adapter.
 */
public interface DataSourceAdapter {

    /**
     * Extracts the full state of the data source.
     *
     * @return a complete snapshot of the data source
     * @throws JsonProcessingException
     */
    Snapshot extract();

    SchemaSnapshot extractSchema();

    void restore(Snapshot snapshot);
}
