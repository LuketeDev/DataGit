package com.lukete.datagit.core.exception;

public class SnapshotNotFoundException extends DataGitException {

    public SnapshotNotFoundException(String ref) {
        super("Snapshot not found: " + ref);
    }

}
