package com.lukete.datagit.connector.postgres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.lukete.datagit.core.domain.Snapshot;
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
}
