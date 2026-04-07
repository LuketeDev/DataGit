package com.lukete.datagit.core.exception;

/**
 * Base exception for domain-related errors.
 */
public class DataGitException extends RuntimeException {
    public DataGitException(String message) {
        super(message);
    }
}
