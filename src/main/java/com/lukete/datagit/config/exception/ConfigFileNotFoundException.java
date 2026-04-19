package com.lukete.datagit.config.exception;

import com.lukete.datagit.core.exception.DataGitException;

/**
 * Thrown when the DataGit config file cannot be found.
 */
public class ConfigFileNotFoundException extends DataGitException {

    public ConfigFileNotFoundException(String path) {
        super("CONFIG_FILE_NOT_FOUND", "Config file not found: " + path);
    }

    public ConfigFileNotFoundException(String path, Throwable cause) {
        super("CONFIG_FILE_NOT_FOUND", "Config file not found: " + path, cause);
    }
}