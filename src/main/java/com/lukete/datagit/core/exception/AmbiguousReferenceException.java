package com.lukete.datagit.core.exception;

/**
 * Raised when a snapshot reference matches more than one stored snapshot.
 */
public class AmbiguousReferenceException extends DataGitException {

    /**
     * Creates the exception for the provided ambiguous reference.
     *
     * @param ref the unresolved snapshot reference
     */
    public AmbiguousReferenceException(String ref) {
        super("AMIBUGUOUS_REFERENCE", "Ambiguous reference: " + ref);
    }

    /**
     * Creates the exception for the provided ambiguous reference and cause.
     *
     * @param ref the unresolved snapshot reference
     * @param cause the underlying cause
     */
    public AmbiguousReferenceException(String ref, Throwable cause) {
        super("AMIBUGUOUS_REFERENCE", "Ambiguous reference: " + ref, cause);
    }
}
