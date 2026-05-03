package com.lukete.datagit.core.exception;

/**
 * Raised when the command options are invalid
 */
public class InvalidCommandOptionsException extends DataGitException {

    private static final String CODE = "INVALID_COMMAND_OPTIONS";

    /**
     * Creates the exception with the provided message.
     *
     * @param message the error message
     */
    public InvalidCommandOptionsException(String message) {
        super(CODE, message);
    }

    /**
     * Creates the exception with the provided message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public InvalidCommandOptionsException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

}
