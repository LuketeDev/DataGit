package com.lukete.datagit.core.exception;

public class InvalidReferenceException extends DataGitException {

    public InvalidReferenceException(String ref) {
        super("INVALID_REFERENCE", "Invalid reference: " + ref);
    }
}
