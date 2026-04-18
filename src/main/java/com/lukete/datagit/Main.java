package com.lukete.datagit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.yaml.snakeyaml.Yaml;

import com.lukete.datagit.cli.command.DataGitCommand;
import com.lukete.datagit.cli.command.DiffCommand;
import com.lukete.datagit.cli.command.InitCommand;
import com.lukete.datagit.cli.command.LogCommand;
import com.lukete.datagit.cli.command.SnapshotCommand;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.cli.output.LogCliRenderer;
import com.lukete.datagit.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.exception.CliExecutionExceptionHandler;
import com.lukete.datagit.core.exception.CliParameterExceptionHandler;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.InitService;
import com.lukete.datagit.core.service.ReferenceResolver;
import com.lukete.datagit.core.service.SnapshotService;
import com.lukete.datagit.core.usecase.CompareSnapshotUseCase;
import com.lukete.datagit.core.util.DiffJsonFormatter;
import com.lukete.datagit.core.util.DiffTextFormatter;
import com.lukete.datagit.storage.filesystem.FileSystemSnapshotStorage;

import picocli.CommandLine;

/**
 * Application entry point that wires the CLI dependencies and executes the
 * requested command.
 */
public class Main {
	private static final Path ROOT = Path.of(System.getProperty("user.dir"));
	private static final Path CONFIG_PATH = ROOT.resolve(".datagit").resolve("config.yml");

	/**
	 * Bootstraps the application and delegates execution to Picocli.
	 *
	 * @param args command-line arguments passed by the user
	 */
	public static void main(String[] args) {
		var root = new DataGitCommand();
		var commandLine = new CommandLine(root);
		var initService = new InitService(new CliPrinter(commandLine.getOut(), commandLine.getErr()));
		var initCommand = new InitCommand(initService, new CliPrinter(commandLine.getOut(), commandLine.getErr()));

		commandLine.addSubcommand("init", initCommand);

		// Allow `datagit init` and help/version output to run before a project has been
		// initialized. Every other command depends on values from .datagit/config.yml.
		if (Files.exists(CONFIG_PATH)) {
			registerConfiguredCommands(commandLine);
		} else if (requiresConfig(args)) {
			throw new IllegalStateException("Missing .datagit/config.yml. Run `datagit init` first.");
		}

		// Register handlers after the full command tree is in place.
		commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(root));
		commandLine.setParameterExceptionHandler(new CliParameterExceptionHandler());

		int exitCode = commandLine.execute(args);
		System.exit(exitCode);
	}

	private static void registerConfiguredCommands(CommandLine commandLine) {
		String databaseType = getYamlConfigValueFromKey("database.type");
		if (!"postgres".equalsIgnoreCase(databaseType)) {
			throw new IllegalStateException("Unsupported database.type: " + databaseType);
		}

		var dataSource = new DriverManagerDataSource();
		dataSource.setUrl(String.format(
				"jdbc:postgresql://%s:%s/%s",
				getYamlConfigValueFromKey("database.host"),
				getYamlConfigValueFromKey("database.port"),
				getYamlConfigValueFromKey("database.name")));
		dataSource.setUsername(getYamlConfigValueFromKey("database.username"));
		dataSource.setPassword(getYamlConfigValueFromKey("database.password"));

		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		var adapter = new PostgresAdapter(jdbc);

		// Resolve relative storage paths from the project root so the YAML can stay
		// portable, but the storage implementation still gets an absolute path.
		var storage = new FileSystemSnapshotStorage(resolvePath(getYamlConfigValueFromKey("storage.path")).toString());
		var snapshotService = new SnapshotService(adapter, storage);
		var diffService = new DiffService();
		var resolver = new ReferenceResolver(storage);
		var compareSnapshotUseCase = new CompareSnapshotUseCase(resolver, diffService);

		var diffJsonFormatter = new DiffJsonFormatter();
		var diffTextFormatter = new DiffTextFormatter();
		var printer = new CliPrinter(commandLine.getOut(), commandLine.getErr());
		var logCliRenderer = new LogCliRenderer(printer);

		commandLine.addSubcommand("snapshot", new SnapshotCommand(snapshotService, printer));
		commandLine.addSubcommand("diff",
				new DiffCommand(compareSnapshotUseCase, diffTextFormatter, diffJsonFormatter, printer));
		commandLine.addSubcommand("log", new LogCommand(storage, logCliRenderer));
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

	private static String getYamlConfigValueFromKey(String key) {
		Yaml yaml = new Yaml();

		try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
			Map<String, Object> configMap = yaml.load(inputStream);
			if (configMap == null) {
				throw new IllegalStateException("Configuration file is empty: " + CONFIG_PATH);
			}

			Object currentValue = configMap;
			// Support dotted keys like `storage.path` by walking nested YAML maps one
			// segment at a time.
			for (String keyPart : key.split("\\.")) {
				if (!(currentValue instanceof Map<?, ?> currentMap)) {
					throw new IllegalStateException("Config key does not point to a scalar value: " + key);
				}
				// Checks if
				currentValue = currentMap.get(keyPart);
				if (currentValue == null) {
					throw new IllegalStateException("Missing config key: " + key);
				}
			}
			return currentValue.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read configuration file: " + CONFIG_PATH, e);
		}
	}
}
