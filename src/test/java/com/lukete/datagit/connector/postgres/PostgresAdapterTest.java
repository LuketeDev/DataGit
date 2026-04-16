package com.lukete.datagit.connector.postgres;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

@Testcontainers
public class PostgresAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    void should_extract_tables_and_rows() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name TEXT)");
        jdbc.execute("INSERT INTO users (name) VALUES ('Lucas')");
        jdbc.execute("INSERT INTO users (name) VALUES ('Luquinhas')");

        PostgresAdapter adapter = new PostgresAdapter(jdbc);

        var snapshot = adapter.extract();

        assertThat(snapshot.source()).isEqualTo("postgres");
        assertThat(snapshot.tables()).containsKey("users");
        assertThat(snapshot.tables().get("users")).hasSize(2);

    }
}
