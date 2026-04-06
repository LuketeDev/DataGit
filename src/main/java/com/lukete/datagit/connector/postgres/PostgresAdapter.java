package com.lukete.datagit.connector.postgres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

/**
 * Extracts data from a PostgreSQL Database
 */

@RequiredArgsConstructor
public class PostgresAdapter implements DataSourceAdapter {
    private final JdbcTemplate jdbc;

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
