package com.lukete.datagit.cli.command;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

/**
 * Root command for DataGit CLI
 */

@Command(name = "datagit", mixinStandardHelpOptions = true, version = "1.0", description = "DataGit CLI")
@Slf4j
@Getter
public class DataGitCommand implements Runnable {

    @Option(names = {
            "--verbose" }, defaultValue = "false", fallbackValue = "true", description = "Display full execution exceptions stack trace.", scope = ScopeType.INHERIT)
    private boolean verbose = false;

    @Override
    public void run() {
        log.info("Use a subcommand. Try --help");
    }

}
