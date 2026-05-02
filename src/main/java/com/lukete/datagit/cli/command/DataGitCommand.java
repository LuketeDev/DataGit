package com.lukete.datagit.cli.command;

import com.lukete.datagit.cli.output.CliPrinter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

/**
 * Root command for the DataGit command-line interface.
 */
@Command(name = "datagit", mixinStandardHelpOptions = true, version = "1.0", description = "DataGit CLI")
@Getter
@RequiredArgsConstructor
public class DataGitCommand implements Runnable {
    @Setter
    private CliPrinter printer;

    @Option(names = {
            "--verbose" }, defaultValue = "false", fallbackValue = "true", description = "Display full execution exceptions stack trace.", scope = ScopeType.INHERIT)
    private boolean verbose = false;

    /**
     * Displays a hint when the root command is executed without a subcommand.
     */
    @Override
    public void run() {
        printer.info("Use a subcommand. Try --help");
    }

}
