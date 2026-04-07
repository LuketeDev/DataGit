package com.lukete.datagit.core.exception;

/**
 * Base exception for domain-related errors.
 */

public class DataGitException extends RuntimeException {
    private final String code;

    public DataGitException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
