package com.lukete.datagit.core.exception;

/**
 * Thrown when a diff result cannot be rendered.
 */
public class DiffRenderingException extends DataGitException {

    public DiffRenderingException(String message, Throwable cause) {
        super("DIFF_RENDERING_EXCEPTION", message, cause);
    }
}