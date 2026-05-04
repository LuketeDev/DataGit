package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.domain.FieldChange;
import com.lukete.datagit.core.domain.Snapshot;

class DiffServiceTest {
        private final DiffService diffService = new DiffService(new SnapshotNormalizer(), createConfig());

        private static DataGitConfig createConfig() {
                DataGitConfig config = new DataGitConfig();
                SnapshotConfig snapshotConfig = new SnapshotConfig();
                snapshotConfig.setIgnoredColumns(List.of());
                config.setSnapshotConfig(snapshotConfig);
                return config;
        }

        @Test
        void should_detect_inserted_rows() {
                Snapshot oldSnapshot = new Snapshot(
                                "old",
                                Instant.now(),
                                "postgres",
                                Map.of(
                                                "users", List.of(
                                                                Map.of("id", 1, "name", "lucas"))));

                Snapshot newSnapshot = new Snapshot(
                                "new",
                                Instant.now(),
                                "postgres",
                                Map.of(
                                                "users", List.of(
                                                                Map.of("id", 1, "name", "lucas"),
                                                                Map.of("id", 2, "name", "luquinhas"))));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables()).containsKey("users");
                assertThat(result.tables().get("users").inserted()).hasSize(1);
                assertThat(result.tables().get("users").inserted().get(0).fieldChanges()).isEmpty();
                assertThat(result.tables().get("users").deleted()).isEmpty();
                assertThat(result.tables().get("users").updated()).isEmpty();
        }

        @Test
        void should_detect_updated_rows_with_field_changes() {
                Snapshot oldSnapshot = snapshot("old", Map.of(
                                "users", List.of(row("id", 1, "name", "Lucas", "age", 20, "city", "SP"))));
                Snapshot newSnapshot = snapshot("new", Map.of(
                                "users", List.of(row("id", 1, "name", "Lucas Silva", "age", 21, "city", "SP"))));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables().get("users").inserted()).isEmpty();
                assertThat(result.tables().get("users").deleted()).isEmpty();
                assertThat(result.tables().get("users").updated()).hasSize(1);

                var change = result.tables().get("users").updated().get(0);
                assertThat(change.before()).containsEntry("name", "Lucas");
                assertThat(change.after()).containsEntry("name", "Lucas Silva");
                assertThat(change.fieldChanges()).containsExactly(
                                new FieldChange("age", 20, 21),
                                new FieldChange("name", "Lucas", "Lucas Silva"));
                assertThat(change.fieldChanges())
                                .extracting(FieldChange::field)
                                .doesNotContain("id", "city");
        }

        @Test
        void should_detect_deleted_rows() {
                Snapshot oldSnapshot = new Snapshot(
                                "old",
                                Instant.now(),
                                "postgres",
                                Map.of(
                                                "users", List.of(
                                                                Map.of("id", 1, "name", "lucas"))));

                Snapshot newSnapshot = new Snapshot(
                                "new",
                                Instant.now(),
                                "postgres",
                                Map.of(
                                                "users", List.of()));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables().get("users").inserted()).isEmpty();
                assertThat(result.tables().get("users").deleted()).hasSize(1);
                assertThat(result.tables().get("users").deleted().get(0).fieldChanges()).isEmpty();
                assertThat(result.tables().get("users").updated()).isEmpty();
        }

        @Test
        void should_detect_no_differences() {
                Snapshot oldSnapshot = snapshot("old", Map.of(
                                "users", List.of(Map.of("id", 1, "name", "lucas"))));
                Snapshot newSnapshot = snapshot("new", Map.of(
                                "users", List.of(Map.of("id", 1, "name", "lucas"))));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables().get("users").inserted()).isEmpty();
                assertThat(result.tables().get("users").deleted()).isEmpty();
                assertThat(result.tables().get("users").updated()).isEmpty();
        }

        @Test
        void should_handle_multiple_tables_with_field_changes() {
                Snapshot oldSnapshot = snapshot("old", Map.of(
                                "users", List.of(Map.of("id", 1, "name", "lucas")),
                                "orders", List.of(Map.of("id", 10, "total", 50))));
                Snapshot newSnapshot = snapshot("new", Map.of(
                                "users", List.of(Map.of("id", 1, "name", "luquinhas")),
                                "orders", List.of(Map.of("id", 10, "total", 50), Map.of("id", 11, "total", 20))));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables()).containsOnlyKeys("users", "orders");
                assertThat(result.tables().get("users").updated()).hasSize(1);
                assertThat(result.tables().get("users").updated().get(0).fieldChanges())
                                .containsExactly(new FieldChange("name", "lucas", "luquinhas"));
                assertThat(result.tables().get("orders").inserted()).hasSize(1);
                assertThat(result.tables().get("orders").inserted().get(0).fieldChanges()).isEmpty();
        }

        @Test
        void should_handle_empty_table_lists_safely() {
                Snapshot oldSnapshot = snapshot("old", Map.of());
                Snapshot newSnapshot = snapshot("new", Map.of());

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables()).isEmpty();
        }

        @Test
        void should_assume_id_column_for_row_identity() {
                Snapshot oldSnapshot = snapshot("old", Map.of(
                                "users", List.of(Map.of("name", "lucas"))));
                Snapshot newSnapshot = snapshot("new", Map.of(
                                "users", List.of(Map.of("name", "luquinhas"))));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables().get("users").inserted()).isEmpty();
                assertThat(result.tables().get("users").deleted()).isEmpty();
                assertThat(result.tables().get("users").updated()).isEmpty();
        }

        private static Snapshot snapshot(String id, Map<String, List<Map<String, Object>>> tables) {
                return new Snapshot(id, Instant.parse("2026-04-08T10:00:00Z"), "postgres", tables);
        }

        private static Map<String, Object> row(Object... entries) {
                assertThat(entries.length % 2).isZero();

                java.util.LinkedHashMap<String, Object> row = new java.util.LinkedHashMap<>();
                for (int i = 0; i < entries.length; i += 2) {
                        row.put((String) entries[i], entries[i + 1]);
                }
                return row;
        }
}
