package com.lukete.datagit.config.exception;

import com.lukete.datagit.core.exception.DataGitException;

/**
 * Thrown when the DataGit config file is invalid.
 */
public class InvalidConfigException extends DataGitException {

    public InvalidConfigException(String message) {
        super("INVALID_CONFIGURATION", "Invalid configuration: " + message);
    }

    public InvalidConfigException(String message, Throwable cause) {
        super("INVALID_CONFIGURATION", "Invalid configuration: " + message, cause);
    }
}