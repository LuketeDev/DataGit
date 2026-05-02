package com.lukete.datagit.connector.postgres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        jdbc.execute("BEGIN");
        try {
            for (var tableEntry : snapshot.tables().entrySet()) {
                String tableName = tableEntry.getKey();
                List<Map<String, Object>> rows = tableEntry.getValue();

                validateIdentifier(tableName);

                jdbc.execute("DELETE FROM " + tableName);

                for (Map<String, Object> row : rows) {
                    insertRow(tableName, row);
                }

            }

            jdbc.execute("COMMIT");
        } catch (Exception e) {
            jdbc.execute("ROLLBACK");
            throw new RestoreFailedException("Failed to restore snapshot: " + snapshot.id(), e);
        }
    }

    private void insertRow(String tableName, Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return;
        }

        row.keySet().forEach(this::validateIdentifier);

        String columns = String.join(", ", row.keySet());
        String placeholders = row.keySet().stream()
                .map(column -> "?")
                .collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        jdbc.update(sql, row.values().toArray());

    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[a-zA-Z_]\\w*")) {
            throw new InvalidDatabaseIdentifierException("Invalid database identifier: " + identifier);
        }
    }
}
