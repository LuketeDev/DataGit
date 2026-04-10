package com.lukete.datagit.core.exception;

public class SnapshotNotFoundException extends DataGitException {

    public SnapshotNotFoundException(String ref) {
        super("SNAPSHOT_NOT_FOUND", "\nSnapshot not found: " + ref);
    }

    public SnapshotNotFoundException(String ref, Throwable cause) {
        super("SNAPSHOT_NOT_FOUND", "Snapshot not found: " + ref, cause);
    }
}