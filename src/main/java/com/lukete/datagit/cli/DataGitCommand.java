package com.lukete.datagit.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Root command for DataGit CLI
 */

@Command(name = "datagit", mixinStandardHelpOptions = true, version = "1.0", description = "DataGit CLI")
@Slf4j
@Getter
public class DataGitCommand implements Runnable {

    @Option(names = {
            "--display-errors" }, defaultValue = "false", fallbackValue = "true", description = "Display full execution exceptions stack trace.")
    private boolean verbose = false;

    @Override
    public void run() {
        log.info("Use a subcommand. Try --help");
    }

}
