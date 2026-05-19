package com.lukete.datagit.connector.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.adapters.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.domain.schema.ColumnSchema;
import com.lukete.datagit.core.domain.schema.SchemaSnapshot;
import com.lukete.datagit.core.service.DefaultJdbcValueNormalizer;

@Testcontainers
class PostgresSchemaExtractionTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = jdbc();
        dropPublicTables();
    }

    @Test
    void shouldExtractTableNamesColumnNamesTypesAndNullableState() {
        jdbc.execute("""
                CREATE TABLE users (
                    id INTEGER NOT NULL,
                    email TEXT,
                    handle VARCHAR(40) NOT NULL,
                    active BOOLEAN NOT NULL,
                    created_at TIMESTAMP,
                    metadata JSONB
                )
                """);

        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(schema.tables()).containsOnlyKeys("users");
        assertThat(schema.tables().get("users").columns()).containsOnlyKeys(
                "id", "email", "handle", "active", "created_at", "metadata");
        assertColumn(schema, "users", "id", "integer", false);
        assertColumn(schema, "users", "email", "text", true);
        assertColumn(schema, "users", "handle", "character varying", false);
        assertColumn(schema, "users", "active", "boolean", false);
        assertColumn(schema, "users", "created_at", "timestamp without time zone", true);
        assertColumn(schema, "users", "metadata", "jsonb", true);
    }

    @Test
    void shouldExtractMultipleTablesInStableNameOrder() {
        jdbc.execute("CREATE TABLE teams (id BIGINT NOT NULL, name TEXT NOT NULL)");
        jdbc.execute("CREATE TABLE audit_events (id BIGINT NOT NULL, payload JSONB)");
        jdbc.execute("CREATE TABLE orders (id INTEGER NOT NULL, total_cents BIGINT NOT NULL)");
        jdbc.execute("CREATE TABLE users (id INTEGER NOT NULL, email TEXT)");

        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(schema.tables().keySet()).containsExactly("audit_events", "orders", "teams", "users");
        assertColumn(schema, "orders", "total_cents", "bigint", false);
        assertColumn(schema, "audit_events", "payload", "jsonb", true);
    }

    @Test
    void shouldPreserveColumnOrdinalOrderingWithinATable() {
        jdbc.execute("""
                CREATE TABLE audit_events (
                    z_last TEXT,
                    id INTEGER NOT NULL,
                    a_first BOOLEAN,
                    payload JSONB
                )
                """);

        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(schema.tables().get("audit_events").columns().keySet())
                .containsExactly("z_last", "id", "a_first", "payload");
    }

    @Test
    void shouldIgnoreSystemAndNonPublicSchemas() {
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS datagit_hidden");
        jdbc.execute("CREATE TABLE datagit_hidden.users (id INTEGER NOT NULL)");
        jdbc.execute("CREATE TABLE public.users (id INTEGER NOT NULL)");

        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(schema.tables()).containsOnlyKeys("users");
    }

    @Test
    void shouldHandleEmptyDatabase() {
        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(schema.tables()).isEmpty();
    }

    @Test
    void shouldExtractTablesWithoutRows() {
        jdbc.execute("CREATE TABLE teams (id INTEGER NOT NULL, name TEXT)");

        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM teams", Integer.class)).isZero();
        assertThat(schema.tables()).containsOnlyKeys("teams");
        assertColumn(schema, "teams", "id", "integer", false);
        assertColumn(schema, "teams", "name", "text", true);
    }

    @Test
    void shouldExtractTablesWithOnlyNullableColumns() {
        jdbc.execute("CREATE TABLE audit_events (message TEXT, payload JSONB, occurred_at TIMESTAMP)");

        SchemaSnapshot schema = adapter().extractSchema();

        assertThat(schema.tables().get("audit_events").columns().values())
                .extracting(ColumnSchema::nullable)
                .containsOnly(true);
    }

    @Test
    void shouldExtractCompositeKeyColumnsAsNotNullable() {
        jdbc.execute("""
                CREATE TABLE memberships (
                    user_id INTEGER,
                    team_id INTEGER,
                    role TEXT,
                    PRIMARY KEY (user_id, team_id)
                )
                """);

        SchemaSnapshot schema = adapter().extractSchema();

        assertColumn(schema, "memberships", "user_id", "integer", false);
        assertColumn(schema, "memberships", "team_id", "integer", false);
        assertColumn(schema, "memberships", "role", "text", true);
    }

    private static JdbcTemplate jdbc() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return new JdbcTemplate(dataSource);
    }

    private PostgresAdapter adapter() {
        return new PostgresAdapter(jdbc, new DataSourceTransactionManager(jdbc.getDataSource()),
                new DefaultJdbcValueNormalizer(new ObjectMapper()));
    }

    private void dropPublicTables() {
        List<String> tables = jdbc.queryForList(
                """
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public'
                        """,
                String.class);

        for (String table : tables) {
            jdbc.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
        }
    }

    private static void assertColumn(
            SchemaSnapshot schema,
            String table,
            String column,
            String type,
            boolean nullable) {
        assertThat(schema.tables().get(table).columns().get(column))
                .isEqualTo(new ColumnSchema(column, type, nullable));
    }
}
