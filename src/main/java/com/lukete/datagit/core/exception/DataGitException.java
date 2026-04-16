package com.lukete.datagit.core.exception;

/**
 * Base exception for domain-related errors.
 */
public class DataGitException extends RuntimeException {
    private final String code;

    /**
     * Creates a new DataGit exception with an error code and message.
     *
     * @param code the stable error code for the failure
     * @param message the human-readable error message
     */
    public DataGitException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Creates a new DataGit exception with an error code, message, and root cause.
     *
     * @param code the stable error code for the failure
     * @param message the human-readable error message
     * @param cause the underlying cause of the failure
     */
    public DataGitException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Returns the application-specific error code associated with this exception.
     *
     * @return the error code
     */
    public String code() {
        return code;
    }
}
