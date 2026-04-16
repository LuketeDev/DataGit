package com.lukete.datagit.core.exception;

/**
 * Raised when the initial DataGit configuration cannot be created.
 */
public class ConfigCreationException extends DataGitException {
    private static final String CODE = "CONFIG_FAILED_TO_CREATE";

    /**
     * Creates the exception with the provided message.
     *
     * @param message the error message
     */
    public ConfigCreationException(String message) {
        super(CODE, message);
    }

    /**
     * Creates the exception with the provided message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public ConfigCreationException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

}
