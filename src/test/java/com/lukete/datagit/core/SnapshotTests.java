package com.lukete.datagit.core;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.service.SnapshotService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SnapshotTests {
    private final SnapshotService service;

    @Test
    void should_create_snapshot_with_postgres_and_store() {
        // given
        // up postgres with testcontainers
        // insert data

        // when
        Snapshot snapshot = service.createSnapshot();

        // then
        assertThat(snapshot.tables()).isNotEmpty();

    }
}
