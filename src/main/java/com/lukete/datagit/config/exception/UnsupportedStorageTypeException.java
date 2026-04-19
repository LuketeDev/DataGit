package com.lukete.datagit.config.exception;

import com.lukete.datagit.core.exception.DataGitException;

/**
 * Thrown when the configured database type is not supported.
 */
public class UnsupportedStorageTypeException extends DataGitException {

    public UnsupportedStorageTypeException(String type) {
        super("UNSUPPORTED_STORAGE_TYPE", "Unsupported storage type: " + type);
    }

    public UnsupportedStorageTypeException(String type, Throwable cause) {
        super("UNSUPPORTED_STORAGE_TYPE", "Unsupported storage type: " + type, cause);
    }
}