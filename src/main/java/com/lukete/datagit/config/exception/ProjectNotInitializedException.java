package com.lukete.datagit.config.exception;

import com.lukete.datagit.core.exception.DataGitException;

/**
 * Thrown when no DataGit project is found in the current directory.
 */
public class ProjectNotInitializedException extends DataGitException {

    public ProjectNotInitializedException() {
        super("PROJECT_NOT_INITIALIZED", "DataGit project not initialized in this directory.");
    }

    public ProjectNotInitializedException(Throwable cause) {
        super("PROJECT_NOT_INITIALIZED", "DataGit project not initialized in this directory.", cause);
    }
}