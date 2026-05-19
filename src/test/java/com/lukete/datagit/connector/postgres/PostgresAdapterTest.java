package com.lukete.datagit.connector.postgres;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.lukete.datagit.support.TestSnapshots.schemaFor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.adapters.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.domain.snapshot.Snapshot;
import com.lukete.datagit.core.exception.InvalidDatabaseIdentifierException;
import com.lukete.datagit.core.exception.RestoreFailedException;
import com.lukete.datagit.core.service.DefaultJdbcValueNormalizer;

@Testcontainers
class PostgresAdapterTest {
        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                        .withDatabaseName("testdb")
                        .withUsername("postgres")
                        .withPassword("postgres");

        @Test
        void shouldExtractTableRowsAndSourceFromPostgres() {
                JdbcTemplate jdbc = jdbc();

                jdbc.execute("DROP TABLE IF EXISTS users");
                jdbc.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name TEXT)");
                jdbc.execute("INSERT INTO users (name) VALUES ('Lucas')");
                jdbc.execute("INSERT INTO users (name) VALUES ('Luquinhas')");

                PostgresAdapter adapter = adapter(jdbc);

                var snapshot = adapter.extract();

                assertThat(snapshot.source()).isEqualTo("postgres");
                assertThat(snapshot.tables()).containsKey("users");
                assertThat(snapshot.tables().get("users")).hasSize(2);
                assertThat(snapshot.tables().get("users"))
                                .extracting(row -> row.get("name"))
                                .containsExactlyInAnyOrder("Lucas", "Luquinhas");
                assertThat(snapshot.schema().tables()).containsKey("users");

        }

        @Test
        void shouldRestoreTableToPreviousSnapshotState() {
                JdbcTemplate jdbc = jdbc();
                jdbc.execute("DROP TABLE IF EXISTS users");
                jdbc.execute("CREATE TABLE users (id INT PRIMARY KEY, name TEXT, email TEXT)");
                jdbc.update("INSERT INTO users (id, name, email) VALUES (1, 'Current', 'current@example.com')");
                jdbc.update("INSERT INTO users (id, name, email) VALUES (99, 'Extra', 'extra@example.com')");

                adapter(jdbc).restore(snapshot(Map.of("users", List.of(
                                row("id", 7, "name", "Lucas", "email", "lucas@example.com"),
                                row("id", 8, "name", "Luquinhas", "email", "luquinhas@example.com")))));

                List<Map<String, Object>> users = jdbc.queryForList("SELECT id, name, email FROM users ORDER BY id");
                assertThat(users).containsExactly(
                                row("id", 7, "name", "Lucas", "email", "lucas@example.com"),
                                row("id", 8, "name", "Luquinhas", "email", "luquinhas@example.com"));
        }

        @Test
        void shouldRestoreMultipleTables() {
                JdbcTemplate jdbc = jdbc();
                jdbc.execute("DROP TABLE IF EXISTS orders");
                jdbc.execute("DROP TABLE IF EXISTS users");
                jdbc.execute("CREATE TABLE users (id INT PRIMARY KEY, name TEXT)");
                jdbc.execute("CREATE TABLE orders (id INT PRIMARY KEY, total INT)");
                jdbc.update("INSERT INTO users (id, name) VALUES (1, 'Current')");
                jdbc.update("INSERT INTO orders (id, total) VALUES (10, 10)");

                adapter(jdbc).restore(snapshot(Map.of(
                                "users", List.of(row("id", 2, "name", "Restored")),
                                "orders", List.of(row("id", 20, "total", 42), row("id", 21, "total", 84)))));

                assertThat(jdbc.queryForList("SELECT id, name FROM users ORDER BY id"))
                                .containsExactly(row("id", 2, "name", "Restored"));
                assertThat(jdbc.queryForList("SELECT id, total FROM orders ORDER BY id"))
                                .containsExactly(row("id", 20, "total", 42), row("id", 21, "total", 84));
        }

        @Test
        void shouldRestoreJsonbValuesSerializedBySnapshotStorage() {
                JdbcTemplate jdbc = jdbc();
                jdbc.execute("DROP TABLE IF EXISTS audit_events");
                jdbc.execute("CREATE TABLE audit_events (id INT PRIMARY KEY, payload JSONB)");

                adapter(jdbc).restore(snapshot(Map.of(
                                "audit_events", List.of(row(
                                                "id", 1,
                                                "payload", row("type", "jsonb", "value",
                                                                "{\"source\": \"enterprise-smoke\"}", "null",
                                                                false))))));

                String payload = jdbc.queryForObject("SELECT payload::text FROM audit_events WHERE id = 1",
                                String.class);
                assertThat(payload).isEqualTo("{\"source\": \"enterprise-smoke\"}");
        }

        @Test
        void shouldRollBackTransactionWhenRestoreFails() {
                JdbcTemplate jdbc = jdbc();
                jdbc.execute("DROP TABLE IF EXISTS users");
                jdbc.execute("CREATE TABLE users (id INT PRIMARY KEY, name TEXT)");
                jdbc.update("INSERT INTO users (id, name) VALUES (1, 'Current')");

                Snapshot snapshot = snapshot(Map.of(
                                "users", List.of(row("id", 2, "name", "Restored")),
                                "missing_table", List.of(row("id", 10))));

                assertThatThrownBy(() -> adapter(jdbc).restore(snapshot))
                                .isInstanceOf(RestoreFailedException.class);

                assertThat(jdbc.queryForList("SELECT id, name FROM users ORDER BY id"))
                                .containsExactly(row("id", 1, "name", "Current"));
        }

        @Test
        void shouldThrowInvalidDatabaseIdentifierExceptionForInvalidIdentifier() {
                JdbcTemplate jdbc = jdbc();

                assertThatThrownBy(() -> adapter(jdbc).restore(snapshot(Map.of(
                                "bad-table", List.of(row("id", 1))))))
                                .isInstanceOf(RestoreFailedException.class)
                                .hasCauseInstanceOf(InvalidDatabaseIdentifierException.class);

        }

        @Test
        void shouldRestoreTablesRespectingForeignKeyOrder() {
                JdbcTemplate jdbc = jdbc();

                jdbc.execute("DROP TABLE IF EXISTS customers");
                jdbc.execute("DROP TABLE IF EXISTS tenants");

                jdbc.execute("""
                                    CREATE TABLE tenants (
                                        id INT PRIMARY KEY,
                                        name TEXT
                                    )
                                """);

                jdbc.execute("""
                                    CREATE TABLE customers (
                                        id INT PRIMARY KEY,
                                        tenant_id INT NOT NULL REFERENCES tenants(id),
                                        name TEXT
                                    )
                                """);

                jdbc.update("INSERT INTO tenants (id, name) VALUES (1, 'Current Tenant')");
                jdbc.update("INSERT INTO customers (id, tenant_id, name) VALUES (10, 1, 'Current Customer')");

                Snapshot snapshot = snapshot(Map.of(
                                "tenants", List.of(
                                                row("id", 2, "name", "Restored Tenant")),
                                "customers", List.of(
                                                row("id", 20, "tenant_id", 2, "name", "Restored Customer"))));

                adapter(jdbc).restore(snapshot);

                assertThat(
                                jdbc.queryForList("SELECT id, name FROM tenants ORDER BY id")).containsExactly(
                                                row("id", 2, "name", "Restored Tenant"));

                assertThat(
                                jdbc.queryForList("SELECT id, tenant_id, name FROM customers ORDER BY id"))
                                .containsExactly(
                                                row("id", 20, "tenant_id", 2, "name", "Restored Customer"));
        }

        private static JdbcTemplate jdbc() {
                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setUrl(postgres.getJdbcUrl());
                dataSource.setUsername(postgres.getUsername());
                dataSource.setPassword(postgres.getPassword());
                return new JdbcTemplate(dataSource);
        }

        private static PostgresAdapter adapter(JdbcTemplate jdbc) {
                return new PostgresAdapter(jdbc, new DataSourceTransactionManager(jdbc.getDataSource()),
                                new DefaultJdbcValueNormalizer(new ObjectMapper()));
        }

        private static Snapshot snapshot(Map<String, List<Map<String, Object>>> tables) {
                return snapshot(tables, schemaFor(tables));
        }

        private static Snapshot snapshot(Map<String, List<Map<String, Object>>> tables, SchemaSnapshot schema) {
                return new Snapshot("snap-1", Instant.parse("2026-04-08T10:00:00Z"), "postgres", tables, schema);
        }

        private static Map<String, Object> row(Object... values) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < values.length; i += 2) {
                        row.put((String) values[i], values[i + 1]);
                }
                return row;
        }
}
