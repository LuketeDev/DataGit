package com.lukete.datagit.cli.command;

import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.core.service.InitService;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * CLI command responsible for creating the initial DataGit project structure.
 */
@Command(name = "init", description = "Initialize a datagit project")
@RequiredArgsConstructor

public class InitCommand implements Runnable {
    @ParentCommand
    DataGitCommand parent;

    private final InitService initService;
    private final CliPrinter printer;

    /**
     * Initializes the DataGit configuration files and directories.
     */
    @Override
    public void run() {
        initService.setupConfig(parent.isVerbose());
        printer.success("[v] DataGit initialized succesfully.");
        printer.success("[v] Created .datagit/ directory.");
    }
}
