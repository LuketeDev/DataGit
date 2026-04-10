package com.lukete.datagit.core.exception;

public class AmbiguousReferenceException extends DataGitException {

    public AmbiguousReferenceException(String ref) {
        super("AMIBUGUOUS_REFERENCE", "Ambiguous reference: " + ref);
    }

    public AmbiguousReferenceException(String ref, Throwable cause) {
        super("AMIBUGUOUS_REFERENCE", "Ambiguous reference: " + ref, cause);
    }
}