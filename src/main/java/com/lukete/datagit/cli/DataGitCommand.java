package com.lukete.datagit.cli;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

/**
 * Root command for DataGit CLI
 */

@Command(name = "datagit", mixinStandardHelpOptions = true, version = "1.0", description = "DataGit CLI")
@Slf4j
public class DataGitCommand implements Runnable {

    @Override
    public void run() {
        log.info("Use a subcommand. Try --help");
    }

}
