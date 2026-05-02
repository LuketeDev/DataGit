package com.lukete.datagit.core.exception;

/**
 * Raised when the database identifier is invalid.
 */
public class InvalidDatabaseIdentifierException extends DataGitException {

    private static final String CODE = "INVALID_DATABASE_IDENTIFIER";

    /**
     * Creates the exception with the provided message.
     *
     * @param message the error message
     */
    public InvalidDatabaseIdentifierException(String message) {
        super(CODE, message);
    }

    /**
     * Creates the exception with the provided message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public InvalidDatabaseIdentifierException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

}
