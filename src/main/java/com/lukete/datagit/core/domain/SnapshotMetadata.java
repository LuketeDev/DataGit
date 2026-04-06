package com.lukete.datagit.core.domain;

import java.time.Instant;

public record SnapshotMetadata(
                String id,
                Instant timestamp, String source) {

}
