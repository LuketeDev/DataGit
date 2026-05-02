package com.lukete.datagit.bootstrap;

import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.connector.postgres.PostgresAdapter;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;
import com.lukete.datagit.core.service.*;
import com.lukete.datagit.storage.filesystem.FileSystemSnapshotStorage;

import lombok.Getter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DataGitContext {

        private final DataSourceAdapter adapter;
        private final SnapshotNormalizer snapshotNormalizer;

        @Getter
        private final SnapshotStorage storage;
        @Getter
        private final DataGitConfig config;
        @Getter
        private final SnapshotService snapshotService;
        @Getter
        private final DiffService diffService;
        @Getter
        private final StatusService statusService;
        @Getter
        private final ReferenceResolver referenceResolver;

        public DataGitContext(DataGitConfig config) {
                this.config = config;

                // --- datasource
                var dataSource = new DriverManagerDataSource();
                dataSource.setUrl(buildJdbcUrl(config));
                dataSource.setUsername(config.getDatabaseConfig().getUsername());
                dataSource.setPassword(config.getDatabaseConfig().getPassword());

                var jdbcTemplate = new JdbcTemplate(dataSource);

                // adapter
                this.adapter = new PostgresAdapter(jdbcTemplate);

                // storage
                this.storage = new FileSystemSnapshotStorage(
                                config.getStorageConfig().getPath());

                // normalizer
                this.snapshotNormalizer = new SnapshotNormalizer();

                // resolver
                this.referenceResolver = new ReferenceResolver(storage);

                // services
                this.diffService = new DiffService(snapshotNormalizer, config);

                this.snapshotService = new SnapshotService(
                                adapter,
                                storage,
                                snapshotNormalizer,
                                config);

                this.statusService = new StatusService(
                                adapter,
                                referenceResolver,
                                diffService,
                                snapshotNormalizer,
                                config);
        }

        private String buildJdbcUrl(DataGitConfig config) {
                var db = config.getDatabaseConfig();

                return "jdbc:postgresql://"
                                + db.getHost() + ":"
                                + db.getPort() + "/"
                                + db.getName();
        }

}