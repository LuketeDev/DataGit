package com.lukete.datagit.connector.postgres;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

@SpringBootTest
public class PostgresAdapterTests {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void should_extract_tables() {
        postgres.start();

        var dataSource = new DriverManagerDataSource();

        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name TEXT)");
        jdbc.execute("INSERT INTO users (name) VALUES ('Lukete')");

        PostgresAdapter adapter = new PostgresAdapter(jdbc);

        var snapshot = adapter.extract();

        assertThat(snapshot.tables()).containsKey("users");

        postgres.stop();
    }
}
