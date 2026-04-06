package com.lukete.datagit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.lukete.datagit.cli.DataGitCommand;
import com.lukete.datagit.cli.SnapshotCommand;
import com.lukete.datagit.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.service.SnapshotService;
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
		var service = new SnapshotService(adapter, storage);

		// CLI
		var root = new DataGitCommand();

		// inject dependency manually
		var snapshotCommand = new SnapshotCommand(service);

		// register subcommand instance
		var commandLine = new CommandLine(root);
		commandLine.addSubcommand("snapshot", snapshotCommand);

		// execute CLI
		commandLine.execute(args);

	}

}
