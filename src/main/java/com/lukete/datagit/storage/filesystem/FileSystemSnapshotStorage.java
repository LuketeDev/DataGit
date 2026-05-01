package com.lukete.datagit.storage.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.ports.SnapshotStorage;

/**
 * Stores snapshots as JSON files in a local directory.
 */
public class FileSystemSnapshotStorage implements SnapshotStorage {
    private final File baseDir;
    private final ObjectMapper mapper = new ObjectMapper();
    private String extension = ".json";

    /**
     * Creates a filesystem-backed snapshot storage rooted at the provided path.
     *
     * @param path the directory where snapshots should be stored
     */
    public FileSystemSnapshotStorage(String path) {
        this.baseDir = new File(path);
        mapper.registerModule(new JavaTimeModule());
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void save(Snapshot snapshot) {
        // Use StringBuilder instead of concatenation for optimizations
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(snapshot.id());
        stringBuilder.append(extension);

        File file = new File(baseDir, stringBuilder.toString());

        try {
            mapper.writeValue(file, snapshot);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save snapshot: ", e);
        }
    }

    /**
     * Loads a snapshot from the corresponding JSON file when it exists.
     *
     * @param id the snapshot identifier
     * @return the loaded snapshot, or an empty optional when no file matches the
     *         identifier
     */
    @Override
    public Optional<Snapshot> load(String id) {
        File file = new File(baseDir, id + extension);

        if (!file.exists())
            return Optional.empty();

        try {
            return Optional.of(mapper.readValue(file, Snapshot.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load snapshot: ", e);
        }
    }

    /**
     * Reads all stored snapshot files from the base directory.
     *
     * @return the list of snapshots currently persisted in the filesystem
     */
    @Override
    public List<Snapshot> list() {
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(extension));

        if (files == null)
            return List.of();

        Snapshot snapshot;
        List<Snapshot> snapshots = new ArrayList<>();

        for (File file : files) {
            try {
                snapshot = mapper.readValue(file, Snapshot.class);
                snapshots.add(new Snapshot(snapshot.id(), snapshot.timestamp(), snapshot.source(), snapshot.tables()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to read snapshot file: ", e);
            }
        }
        return snapshots;
    }

}
