package com.lukete.datagit.core.exception;

public class NoSnapshotsFoundException extends DataGitException {

    public NoSnapshotsFoundException() {
        super("No snapshots available");
    }
}