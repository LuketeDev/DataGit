package com.lukete.datagit.cli.command;

import com.lukete.datagit.core.service.InitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * CLI command responsible for creating the initial DataGit project structure.
 */
@Command(name = "init", description = "Initialize a datagit project")
@RequiredArgsConstructor
@Slf4j
public class InitCommand implements Runnable {
    @ParentCommand
    DataGitCommand parent;

    private final InitService initService;

    /**
     * Initializes the DataGit configuration files and directories.
     */
    @Override
    public void run() {
        initService.setupConfig(parent.isVerbose());
        log.info("[v] DataGit initialized succesfully.");
        log.info("[v] Created .datagit/ directory.");
    }
}
