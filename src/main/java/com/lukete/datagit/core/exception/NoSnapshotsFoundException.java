package com.lukete.datagit.core.exception;

public class NoSnapshotsFoundException extends DataGitException {

    public NoSnapshotsFoundException() {
        super("NO_SNAPSHOTS_FOUND", "No snapshots available");
    }

    public NoSnapshotsFoundException(Throwable cause) {
        super("NO_SNAPSHOTS_FOUND", "No snapshots available", cause);
    }
}