package com.lukete.datagit.config.exception;

import com.lukete.datagit.core.exception.DataGitException;

/**
 * Thrown when the DataGit config file cannot be found.
 */
public class DatabaseNormalizationException extends DataGitException {

    public DatabaseNormalizationException(String type) {
        super("TYPE_NORMALIZATION_EXCEPTION", "Failed to normalize type: " + type);
    }

    public DatabaseNormalizationException(String type, Throwable cause) {
        super("TYPE_NORMALIZATION_EXCEPTION", "Failed to normalize type: " + type, cause);
    }
}