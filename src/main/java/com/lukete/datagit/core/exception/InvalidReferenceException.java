package com.lukete.datagit.core.exception;

public class InvalidReferenceException extends DataGitException {

    public InvalidReferenceException(String ref) {
        super("Invalid reference: " + ref);
    }
}
