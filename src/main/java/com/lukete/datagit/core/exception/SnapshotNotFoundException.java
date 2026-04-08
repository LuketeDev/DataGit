package com.lukete.datagit.core.exception;

public class SnapshotNotFoundException extends DataGitException {

    public SnapshotNotFoundException(String ref) {
        super("SNAPSHOT_NOT_FOUND", "\nSnapshot not found: " + ref + "\nTry: datagit log\n");
    }
}