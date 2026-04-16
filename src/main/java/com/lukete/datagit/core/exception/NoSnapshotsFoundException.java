package com.lukete.datagit.core.exception;

/**
 * Raised when an operation requires snapshots but none are available in storage.
 */
public class NoSnapshotsFoundException extends DataGitException {

    /**
     * Creates the exception with the default message.
     */
    public NoSnapshotsFoundException() {
        super("NO_SNAPSHOTS_FOUND", "No snapshots available");
    }

    /**
     * Creates the exception with the default message and cause.
     *
     * @param cause the underlying cause
     */
    public NoSnapshotsFoundException(Throwable cause) {
        super("NO_SNAPSHOTS_FOUND", "No snapshots available", cause);
    }
}
