package com.lukete.datagit.core.exception;

public class ConfigCreationException extends DataGitException {
    private static final String CODE = "CONFIG_FAILED_TO_CREATE";

    public ConfigCreationException(String message) {
        super(CODE, message);
    }

    public ConfigCreationException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

}
