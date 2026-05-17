package com.lukete.datagit.adapters.connector.postgres;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.schema.TableSchema;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
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
    private final PlatformTransactionManager transactionManager;

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

        SchemaSnapshot schema = extractSchema();

        return new Snapshot(
                null,
                null,
                "postgres",
                tables,
                schema);
    }

    @Override
    public SchemaSnapshot extractSchema() {
        Map<String, TableSchema> tables = new HashMap<>();

        // Fetch all table names from public schema
        String query = """
                    SELECT
                table_name,
                column_name,
                data_type,
                is_nullable
                FROM information_schema.columns
                WHERE table_schema = 'public'
                """;

        var results = jdbc.queryForList(query);

        for (Map<String, Object> values : results) {
            String tableName = values.get("table_name").toString();
            String columnName = values.get("column_name").toString();
            String dataType = values.get("data_type").toString();
            boolean isNullable = "YES".equalsIgnoreCase(values.get("is_nullable").toString());

            ColumnSchema columnSchema = new ColumnSchema(columnName, dataType, isNullable);

            tables.computeIfAbsent(tableName, ignored -> new TableSchema(tableName, new LinkedHashMap<>()));
            tables.get(tableName).columns().put(columnName, columnSchema);
        }
        return new SchemaSnapshot(tables);
    }

    @Override
    public void restore(Snapshot snapshot) {
        try {
            var tables = snapshot.tables();
            var tableNames = tables.keySet();

            tableNames.forEach(this::validateIdentifier);

            Map<String, Set<String>> dependencies = loadForeignKeyDependencies(tableNames);
            List<String> insertOrder = sortForInsert(dependencies);
            List<String> deleteOrder = sortForDelete(insertOrder);

            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            transactionTemplate.executeWithoutResult(status -> {
                for (String tableName : deleteOrder) {
                    jdbc.update("DELETE FROM " + tableName);
                }

                for (String tableName : insertOrder) {
                    List<Map<String, Object>> rows = tables.getOrDefault(tableName, List.of());
                    for (Map<String, Object> row : rows) {
                        insertRow(tableName, row);
                    }
                }
            });
        } catch (RestoreFailedException e) {
            throw e;
        } catch (Exception e) {
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

        Object[] values = row.values().stream()
                .map(this::restoreValue)
                .toArray();

        jdbc.update(sql, values);
    }

    private Object restoreValue(Object value) {
        if (value instanceof Map<?, ?> typedValue && typedValue.containsKey("type")
                && typedValue.containsKey("value") && typedValue.containsKey("null")) {
            if (Boolean.TRUE.equals(typedValue.get("null"))) {
                return null;
            }

            Object type = typedValue.get("type");
            if ("json".equals(type) || "jsonb".equals(type)) {
                return new SqlParameterValue(Types.OTHER, typedValue.get("value"));
            }
        }

        return value;
    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[a-zA-Z_]\\w*")) {
            throw new InvalidDatabaseIdentifierException("Invalid database identifier: " + identifier);
        }
    }

    /**
     * This function was created when {@code datagit checkout} returned an exception
     * because of update/delete violating fk constraints.
     * 
     * This function loads all the dependencies of a foreign key to be
     * updated/deleted before updating/deleting the dependant.
     * 
     * @param snapshotTables
     * @return
     *
     */
    private Map<String, Set<String>> loadForeignKeyDependencies(Set<String> snapshotTables) {
        /*
         * SELECT:
         * - child.relname: the name of the table containing the foreign key.
         * - parent.relname: the table referenced by the foreign key.
         * - AS child_table and AS parent_table: rename the columns in SELECT result.
         * FROM:
         * - pg_constraint: PostgreSQL internal table containing all constraints.
         * - c: alias for the constraints.
         * JOIN pg_class child...:
         * - pg_class: contains information about tables.
         * - child: alias for the child table.
         * - c.conrelid: OID of the table that has the constraint.
         * - This JOIN connects the constraint to the table containing it.
         * JOIN pg_class parent...:
         * - pg_class: contains informations about tables.
         * - parent: alias for the referenced table.
         * - c.confrelid: OID of the table referenced by the foreign key.
         * - This JOIN finds the parent table
         * JOIN pg_namespace...:
         * - pg_namespace: stores db schemas
         * - child.relnamespace: points to which schema the child table is contained.
         * - This JOIN allows filtering by schema later.
         * WHERE c.contype = 'f': gets only fk constraints
         * AND n.nspname = 'public': only in the 'public' schema
         * 
         * The following query returns:
         * All the fk relations in the 'public' schema, showing which child_table
         * references which parent_table.
         */
        String sql = """
                SELECT
                    child.relname AS child_table,
                    parent.relname AS parent_table
                FROM pg_constraint c
                JOIN pg_class child ON child.oid = c.conrelid
                JOIN pg_class parent ON parent.oid = c.confrelid
                JOIN pg_namespace n ON n.oid = child.relnamespace
                WHERE c.contype = 'f'
                    AND n.nspname = 'public'
                        """;

        // Map<table name, Set<name of dependant tables>>
        Map<String, Set<String>> dependencies = new HashMap<>();

        /*
         * Creates an empty map to all snapshot tables.
         * For example:
         * users -> empty HashSet
         * products -> empty HashShet
         */
        for (String table : snapshotTables) {
            dependencies.put(table, new HashSet<>());
        }

        /*
         * Executes the query, which returns something like:
         * child_table | parent_table
         * ------------|-------------
         * customers---|-stores------
         * orders------|-customers---
         * order_items-|-orders------
         */
        jdbc.query(sql, rs -> {
            String child = rs.getString("child_table");
            String parent = rs.getString("parent_table");

            // Applies only to tables in the snapshot and fills the map accordingly.
            if (snapshotTables.contains(child) && snapshotTables.contains(parent)) {
                // Adds parent (dependency) to child (dependant)
                dependencies.get(child).add(parent);
            }
        });

        return dependencies;
    }

    private List<String> sortForInsert(Map<String, Set<String>> dependencies) {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String table : dependencies.keySet()) {
            visit(table, dependencies, visited, visiting, sorted);
        }

        return sorted;
    }

    private List<String> sortForDelete(List<String> insertOrder) {
        List<String> deleteOrder = new ArrayList<>(insertOrder);
        Collections.reverse(deleteOrder);
        return deleteOrder;
    }

    private void visit(
            String table,
            Map<String, Set<String>> dependencies,
            Set<String> visited,
            Set<String> visiting,
            List<String> sorted) {
        if (visited.contains(table)) {
            return;
        }

        if (visiting.contains(table)) {
            throw new RestoreFailedException("Cyclic foreign key dependency involving table: " + table);
        }

        visiting.add(table);

        for (String parent : dependencies.getOrDefault(table, Set.of())) {
            visit(parent, dependencies, visited, visiting, sorted);
        }

        visiting.remove(table);
        visited.add(table);
        sorted.add(table);
    }
}
