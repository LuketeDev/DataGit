package com.lukete.datagit.connector.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.InvalidDatabaseIdentifierException;
import com.lukete.datagit.core.exception.RestoreFailedException;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

/**
 * Extracts the current state of a PostgreSQL database into a {@link Snapshot}.
 */
@RequiredArgsConstructor
public class PostgresAdapter implements DataSourceAdapter {
    private final JdbcTemplate jdbc;

    /**
     * Reads all tables from the {@code public} schema and builds a snapshot from
     * their rows.
     *
     * @return a snapshot containing the data currently available in the configured
     *         database
     */
    @Override
    public Snapshot extract() {
        Map<String, List<Map<String, Object>>> tables = new HashMap<>();

        // Fetch all table names from public schema
        List<String> tableNames = jdbc
                .queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                        String.class);

        for (String table : tableNames) {
            // Fetch all rows from table
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM " + table);

            tables.put(table, rows);
        }

        return new Snapshot(
                null,
                null,
                "postgres",
                tables);
    }

    @Override
    public void restore(Snapshot snapshot) {
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                for (var tableEntry : snapshot.tables().entrySet()) {
                    String tableName = tableEntry.getKey();
                    List<Map<String, Object>> rows = tableEntry.getValue();

                    validateIdentifier(tableName);

                    try (var statement = connection.createStatement()) {
                        statement.executeUpdate("DELETE FROM " + tableName);
                    }

                    for (Map<String, Object> row : rows) {
                        insertRow(connection, tableName, row);
                    }

                }

                connection.commit();
                return null;
            } catch (Exception e) {
                connection.rollback();
                throw new RestoreFailedException("Failed to restore snapshot: " + snapshot.id(), e);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        });
    }

    private void insertRow(Connection connection, String tableName, Map<String, Object> row) throws SQLException {
        if (row == null || row.isEmpty()) {
            return;
        }

        row.keySet().forEach(this::validateIdentifier);

        String columns = String.join(", ", row.keySet());
        String placeholders = row.keySet().stream()
                .map(column -> "?")
                .collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object value : row.values()) {
                statement.setObject(index, value);
                index++;
            }
            statement.executeUpdate();
        }
    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[a-zA-Z_]\\w*")) {
            throw new InvalidDatabaseIdentifierException("Invalid database identifier: " + identifier);
        }
    }
}
