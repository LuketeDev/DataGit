package com.lukete.datagit.core.exception;

/**
 * Raised when no stored snapshot can be found for a given reference.
 */
public class SnapshotNotFoundException extends DataGitException {

    /**
     * Creates the exception for the provided unresolved reference.
     *
     * @param ref the snapshot reference that could not be resolved
     */
    public SnapshotNotFoundException(String ref) {
        super("SNAPSHOT_NOT_FOUND", "\nSnapshot not found: " + ref);
    }

    /**
     * Creates the exception for the provided unresolved reference and cause.
     *
     * @param ref the snapshot reference that could not be resolved
     * @param cause the underlying cause
     */
    public SnapshotNotFoundException(String ref, Throwable cause) {
        super("SNAPSHOT_NOT_FOUND", "Snapshot not found: " + ref, cause);
    }
}
