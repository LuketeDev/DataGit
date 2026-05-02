package com.lukete.datagit.storage.filesystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lukete.datagit.core.domain.Snapshot;

class FileSystemSnapshotStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void should_save_and_load_snapshot() {
        FileSystemSnapshotStorage storage = new FileSystemSnapshotStorage(tempDir.toString());

        Snapshot snapshot = new Snapshot("abc123", Instant.now(), "postgres", Map.of(
                "users",
                List.of(
                        Map.of("id", 1, "name", "luquinhas"))));
        storage.save(snapshot);

        var loaded = storage.load("abc123");

        assertThat(loaded).isPresent();
        assertThat(loaded.get().id()).isEqualTo("abc123");
        assertThat(loaded.get().tables()).containsKey("users");
    }

    @Test
    void shouldSaveSnapshotAsJson() {
        FileSystemSnapshotStorage storage = new FileSystemSnapshotStorage(tempDir.toString());

        storage.save(new Snapshot("abc123", Instant.parse("2026-04-08T10:00:00Z"), "postgres", Map.of()));

        assertThat(tempDir.resolve("abc123.json")).exists();
    }

    @Test
    void shouldReturnEmptyWhenSnapshotDoesNotExist() {
        FileSystemSnapshotStorage storage = new FileSystemSnapshotStorage(tempDir.toString());

        assertThat(storage.load("missing")).isEmpty();
    }

    @Test
    void should_list_saved_snapshots() {
        FileSystemSnapshotStorage storage = new FileSystemSnapshotStorage(tempDir.toString());

        storage.save(new Snapshot("id1", Instant.now(), "postgres", Map.of()));
        storage.save(new Snapshot("id2", Instant.now(), "postgres", Map.of()));

        var snapshots = storage.list();

        assertThat(snapshots).hasSize(2);

        // returns a List<T, RuntimeException> where T is the value returned from the
        // function inside extracting.
        assertThat(snapshots).extracting(Snapshot::id).contains("id1", "id2");
    }

    @Test
    void shouldCreateStorageDirectoryIfMissing() {
        Path storagePath = tempDir.resolve("missing").resolve("snapshots");

        new FileSystemSnapshotStorage(storagePath.toString());

        assertThat(Files.isDirectory(storagePath)).isTrue();
    }
}
