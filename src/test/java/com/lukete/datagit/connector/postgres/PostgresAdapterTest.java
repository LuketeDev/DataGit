package com.lukete.datagit.connector.postgres;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class PostgresAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    void shouldExtractTableRowsAndSourceFromPostgres() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("DROP TABLE IF EXISTS users");
        jdbc.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name TEXT)");
        jdbc.execute("INSERT INTO users (name) VALUES ('Lucas')");
        jdbc.execute("INSERT INTO users (name) VALUES ('Luquinhas')");

        PostgresAdapter adapter = new PostgresAdapter(jdbc);

        var snapshot = adapter.extract();

        assertThat(snapshot.source()).isEqualTo("postgres");
        assertThat(snapshot.tables()).containsKey("users");
        assertThat(snapshot.tables().get("users")).hasSize(2);
        assertThat(snapshot.tables().get("users"))
                .extracting(row -> row.get("name"))
                .containsExactlyInAnyOrder("Lucas", "Luquinhas");

    }
}
