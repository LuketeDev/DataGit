package com.lukete.datagit.core.exception;

/**
 * Raised when the checkout command fails to restore the database state.
 */
public class RestoreFailedException extends DataGitException {

    private static final String CODE = "SNAPSHOT_RESTORE_FAILED";

    /**
     * Creates the exception for the failed snapshot restoration
     *
     * @param message the error message
     */
    public RestoreFailedException(String message) {
        super(CODE, message);
    }

    /**
     * Creates the exception for the failed snapshot restoration and cause
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public RestoreFailedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}
