package com.lukete.datagit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.command.DataGitCommand;
import com.lukete.datagit.cli.command.DiffCommand;
import com.lukete.datagit.cli.command.InitCommand;
import com.lukete.datagit.cli.command.LogCommand;
import com.lukete.datagit.cli.command.RestoreCommand;
import com.lukete.datagit.cli.command.SnapshotCommand;
import com.lukete.datagit.cli.command.StatusCommand;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.cli.output.JsonDiffRenderer;
import com.lukete.datagit.cli.output.LogCliRenderer;
import com.lukete.datagit.cli.output.TextDiffRenderer;
import com.lukete.datagit.core.exception.CliExecutionExceptionHandler;
import com.lukete.datagit.core.exception.CliParameterExceptionHandler;
import com.lukete.datagit.core.service.InitService;
import picocli.CommandLine;

/**
 * Application entry point that wires the CLI dependencies and executes the
 * requested command.
 */
public class Main {

	/**
	 * Bootstraps the application and delegates execution to Picocli.
	 *
	 * @param args command-line arguments passed by the user
	 */
	public static void main(String[] args) {
		var root = new DataGitCommand();
		var commandLine = new CommandLine(root);
		var printer = new CliPrinter(commandLine.getOut(), commandLine.getErr());
		root.setPrinter(printer);
		var contextProvider = new DataGitContextProvider();
		var initService = new InitService();
		var objMapper = new ObjectMapper();
		var jsonDiffRenderer = new JsonDiffRenderer(printer, objMapper);
		var textDiffRenderer = new TextDiffRenderer(printer);
		var logCliRenderer = new LogCliRenderer(printer);

		commandLine.addSubcommand("init", new InitCommand(initService, printer));
		commandLine.addSubcommand("snapshot", new SnapshotCommand(contextProvider, printer));
		commandLine.addSubcommand("diff",
				new DiffCommand(contextProvider, jsonDiffRenderer, textDiffRenderer));
		commandLine.addSubcommand("log", new LogCommand(contextProvider, logCliRenderer));
		commandLine.addSubcommand("status", new StatusCommand(contextProvider, textDiffRenderer, printer));
		commandLine.addSubcommand("checkout", new RestoreCommand(contextProvider, printer));

		// Register handlers after the full command tree is in place.
		commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(root));
		commandLine.setParameterExceptionHandler(new CliParameterExceptionHandler());

		int exitCode = commandLine.execute(args);
		System.exit(exitCode);
	}
}