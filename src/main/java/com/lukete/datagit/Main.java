package com.lukete.datagit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.lukete.datagit.cli.DataGitCommand;
import com.lukete.datagit.cli.DiffCommand;
import com.lukete.datagit.cli.LogCommand;
import com.lukete.datagit.cli.SnapshotCommand;
import com.lukete.datagit.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.exception.CliExecutionExceptionHandler;
import com.lukete.datagit.core.exception.CliParameterExceptionHandler;
import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.ReferenceResolver;
import com.lukete.datagit.core.service.SnapshotService;
import com.lukete.datagit.core.usecase.CompareSnapshotUseCase;
import com.lukete.datagit.core.util.DiffJsonFormatter;
import com.lukete.datagit.core.util.DiffTextFormatter;
import com.lukete.datagit.storage.filesystem.FileSystemSnapshotStorage;

import picocli.CommandLine;

public class Main {
	public static void main(String[] args) {

		// Configure Datasource for Postgres
		var dataSource = new DriverManagerDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5433/datagit_db_dev");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");

		JdbcTemplate jdbc = new JdbcTemplate(dataSource);

		// Wire dependencies manually
		var adapter = new PostgresAdapter(jdbc);
		var storage = new FileSystemSnapshotStorage("storage/snapshots");
		var snapshotService = new SnapshotService(adapter, storage);
		var diffService = new DiffService();
		var resolver = new ReferenceResolver(storage);
		var compareSnapshotUseCase = new CompareSnapshotUseCase(resolver, diffService);

		// Diff formatters
		var diffJsonFormatter = new DiffJsonFormatter();
		var diffTextFormatter = new DiffTextFormatter();

		// CLI
		var root = new DataGitCommand();

		// inject dependency manually
		var snapshotCommand = new SnapshotCommand(snapshotService);
		var diffCommand = new DiffCommand(compareSnapshotUseCase, diffTextFormatter, diffJsonFormatter);
		var logCommand = new LogCommand(storage);

		// register subcommand instance
		var commandLine = new CommandLine(root);
		commandLine.addSubcommand("snapshot", snapshotCommand);
		commandLine.addSubcommand("diff", diffCommand);
		commandLine.addSubcommand("log", logCommand);

		commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(root));
		commandLine.setParameterExceptionHandler(new CliParameterExceptionHandler());
		// execute CLI
		int exitCode = commandLine.execute(args);
		System.exit(exitCode);

	}

}
