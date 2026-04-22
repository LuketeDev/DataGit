package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.domain.SnapshotConfig;
import com.lukete.datagit.core.domain.DiffResult;
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
                assertThat(result.tables().get("users").deleted()).isEmpty();
                assertThat(result.tables().get("users").updated()).isEmpty();
        }

        @Test
        void should_detect_updated_rows() {
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
                                                                Map.of("id", 1, "name", "luquinhas"))));

                DiffResult result = diffService.compare(oldSnapshot, newSnapshot);

                assertThat(result.tables().get("users").inserted()).isEmpty();
                assertThat(result.tables().get("users").deleted()).isEmpty();
                assertThat(result.tables().get("users").updated()).hasSize(1);

                // The value that was changed in the row 0
                var change = result.tables().get("users").updated().get(0);
                assertThat(change.before()).containsEntry("name", "lucas");
                assertThat(change.after()).containsEntry("name", "luquinhas");
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
                assertThat(result.tables().get("users").updated()).isEmpty();
        }
}
