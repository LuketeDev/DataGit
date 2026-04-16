package com.lukete.datagit.core.exception;

/**
 * Raised when a snapshot reference uses an unsupported or invalid format.
 */
public class InvalidReferenceException extends DataGitException {

    /**
     * Creates the exception for the provided invalid reference.
     *
     * @param ref the invalid snapshot reference
     */
    public InvalidReferenceException(String ref) {
        super("INVALID_REFERENCE", "Invalid reference: " + ref);
    }

    /**
     * Creates the exception for the provided invalid reference and cause.
     *
     * @param ref the invalid snapshot reference
     * @param cause the underlying cause
     */
    public InvalidReferenceException(String ref, Throwable cause) {
        super("INVALID_REFERENCE", "Invalid reference: " + ref, cause);
    }
}
