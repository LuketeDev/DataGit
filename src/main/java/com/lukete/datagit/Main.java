package com.lukete.datagit;

import java.nio.file.Path;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lukete.datagit.cli.command.DataGitCommand;
import com.lukete.datagit.cli.command.DiffCommand;
import com.lukete.datagit.cli.command.InitCommand;
import com.lukete.datagit.cli.command.LogCommand;
import com.lukete.datagit.cli.command.SnapshotCommand;
import com.lukete.datagit.cli.command.StatusCommand;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.cli.output.JsonDiffRenderer;
import com.lukete.datagit.cli.output.LogCliRenderer;
import com.lukete.datagit.cli.output.TextDiffRenderer;
import com.lukete.datagit.config.ConfigLoader;
import com.lukete.datagit.config.ConfigValidator;
import com.lukete.datagit.config.ProjectLocator;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.exception.CliExecutionExceptionHandler;
import com.lukete.datagit.core.exception.CliParameterExceptionHandler;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.InitService;
import com.lukete.datagit.core.service.ReferenceResolver;
import com.lukete.datagit.core.service.SnapshotNormalizer;
import com.lukete.datagit.core.service.SnapshotService;
import com.lukete.datagit.core.service.StatusService;
import com.lukete.datagit.core.usecase.CompareSnapshotUseCase;
import com.lukete.datagit.storage.filesystem.FileSystemSnapshotStorage;

import picocli.CommandLine;

/**
 * Application entry point that wires the CLI dependencies and executes the
 * requested command.
 */
public class Main {
	private static final Path ROOT = Path.of(System.getProperty("user.dir"));

	/**
	 * Bootstraps the application and delegates execution to Picocli.
	 *
	 * @param args command-line arguments passed by the user
	 */
	public static void main(String[] args) {
		var root = new DataGitCommand();
		var commandLine = new CommandLine(root);
		var printer = new CliPrinter(commandLine.getOut(), commandLine.getErr());

		registerBaseCommands(commandLine, printer);

		if (requiresConfig(args)) {
			var config = bootstrap();
			registerConfiguredCommands(commandLine, config, printer);
		}

		// Register handlers after the full command tree is in place.
		commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(root));
		commandLine.setParameterExceptionHandler(new CliParameterExceptionHandler());

		int exitCode = commandLine.execute(args);
		System.exit(exitCode);
	}

	private static void registerBaseCommands(CommandLine commandLine, CliPrinter printer) {
		var initService = new InitService(printer);
		var initCommand = new InitCommand(initService, printer);

		commandLine.addSubcommand("init", initCommand);
	}

	private static void registerConfiguredCommands(CommandLine commandLine, DataGitConfig config, CliPrinter printer) {
		var databaseConfig = config.getDatabaseConfig();
		var storageConfig = config.getStorageConfig();

		var dataSource = new DriverManagerDataSource();
		dataSource.setUrl(String.format(
				"jdbc:postgresql://%s:%s/%s",
				databaseConfig.getHost(),
				databaseConfig.getPort(),
				databaseConfig.getName()));
		dataSource.setUsername(databaseConfig.getUsername());
		dataSource.setPassword(databaseConfig.getPassword());

		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		var adapter = new PostgresAdapter(jdbc);

		// Resolve relative storage paths from the project root so the YAML can stay
		// portable, but the storage implementation still gets an absolute path.
		var storage = new FileSystemSnapshotStorage(resolvePath(storageConfig.getPath()).toString());
		var snapshotNormalizer = new SnapshotNormalizer();

		var snapshotService = new SnapshotService(adapter, storage, snapshotNormalizer, config);
		var diffService = new DiffService(snapshotNormalizer, config);
		var resolver = new ReferenceResolver(storage);
		var compareSnapshotUseCase = new CompareSnapshotUseCase(resolver, diffService);
		var statusService = new StatusService(adapter, resolver, diffService, snapshotNormalizer, config);

		var objMapper = new ObjectMapper();
		var jsonDiffRenderer = new JsonDiffRenderer(printer, objMapper);
		var textDiffRenderer = new TextDiffRenderer(printer);
		var logCliRenderer = new LogCliRenderer(printer);

		commandLine.addSubcommand("snapshot", new SnapshotCommand(snapshotService, printer));
		commandLine.addSubcommand("diff",
				new DiffCommand(compareSnapshotUseCase, jsonDiffRenderer, textDiffRenderer));
		commandLine.addSubcommand("log", new LogCommand(storage, logCliRenderer));
		commandLine.addSubcommand("status", new StatusCommand(textDiffRenderer, statusService, printer));
	}

	private static DataGitConfig bootstrap() {
		var projectLocator = new ProjectLocator();
		projectLocator.validateProjectInitialized();

		var configLoader = new ConfigLoader(new ObjectMapper(new YAMLFactory()));
		var config = configLoader.load(projectLocator.getConfigFile());

		var configValidator = new ConfigValidator();
		configValidator.validateConfig(config);

		return config;
	}

	private static boolean requiresConfig(String[] args) {
		for (String arg : args) {
			if ("-h".equals(arg) || "--help".equals(arg) || "-V".equals(arg) || "--version".equals(arg)) {
				return false;
			}
			if (!arg.startsWith("-")) {
				return !"init".equals(arg);
			}
		}
		return false;
	}

	private static Path resolvePath(String configuredPath) {
		Path path = Path.of(configuredPath);
		return path.isAbsolute() ? path : ROOT.resolve(path).normalize();
	}
}