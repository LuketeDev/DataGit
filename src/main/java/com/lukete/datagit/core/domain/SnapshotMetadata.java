package com.lukete.datagit.core.domain;

import java.time.Instant;

/**
 * Lightweight snapshot descriptor used when only identifying information is needed.
 *
 * @param id the snapshot identifier
 * @param timestamp the snapshot creation time
 * @param source the originating data source
 */
public record SnapshotMetadata(
                String id,
                Instant timestamp, String source) {

}
