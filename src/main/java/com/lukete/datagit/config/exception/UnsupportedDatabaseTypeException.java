package com.lukete.datagit.config.exception;

import com.lukete.datagit.core.exception.DataGitException;

/**
 * Thrown when the configured database type is not supported.
 */
public class UnsupportedDatabaseTypeException extends DataGitException {

    public UnsupportedDatabaseTypeException(String type) {
        super("UNSUPPORTED_DATABASE_TYPE", "Unsupported database type: " + type);
    }

    public UnsupportedDatabaseTypeException(String type, Throwable cause) {
        super("UNSUPPORTED_DATABASE_TYPE", "Unsupported database type: " + type, cause);
    }
}