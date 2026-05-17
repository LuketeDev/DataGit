package com.lukete.datagit.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.adapters.connector.postgres.PostgresAdapter;
import com.lukete.datagit.adapters.storage.filesystem.FileSystemSnapshotStorage;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;
import com.lukete.datagit.core.service.*;

import lombok.Getter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DataGitContext {

        @Getter
        private final DataSourceAdapter adapter;
        @Getter
        private final SnapshotNormalizer snapshotNormalizer;
        @Getter
        private final JdbcValueNormalizer jdbcValueNormalizer;
        @Getter
        private final SnapshotStorage storage;
        @Getter
        private final DataGitConfig config;
        @Getter
        private final SnapshotService snapshotService;
        @Getter
        private final DiffService diffService;
        @Getter
        private final SchemaDiffService schemaDiffService;
        @Getter
        private final StatusService statusService;
        @Getter
        private final RestoreService restoreService;
        @Getter
        private final ReferenceResolver referenceResolver;
        @Getter
        private final RestorePlanner restorePlanner;
        @Getter
        private final DataSourceTransactionManager dataSourceTransactionManager;

        // public DataGitContext(ReferenceResolver referenceResolver, RestoreService
        // restoreService) {
        // this.adapter = null;
        // this.snapshotNormalizer = null;
        // this.storage = null;
        // this.config = null;
        // this.snapshotService = null;
        // this.diffService = null;
        // this.statusService = null;
        // this.referenceResolver = referenceResolver;
        // this.restoreService = restoreService;
        // this.restorePlanner = null;
        // this.dataSourceTransactionManager = null;
        // this.schemaDiffService = null;
        // }

        public DataGitContext(DataGitConfig config, JdbcTemplate jdbcTemplate,
                        DataSourceTransactionManager dataSourceTransactionManager) {
                this.config = config;

                // datasource
                var dataSource = new DriverManagerDataSource();
                dataSource.setUrl(buildJdbcUrl(config));
                dataSource.setUsername(config.getDatabaseConfig().getUsername());
                dataSource.setPassword(config.getDatabaseConfig().getPassword());

                // storage
                this.storage = new FileSystemSnapshotStorage(
                                config.getStorageConfig().getPath());

                // normalizer
                this.snapshotNormalizer = new SnapshotNormalizer();
                this.jdbcValueNormalizer = new DefaultJdbcValueNormalizer(new ObjectMapper());

                // adapter
                this.adapter = new PostgresAdapter(jdbcTemplate, dataSourceTransactionManager, jdbcValueNormalizer);

                // resolver
                this.referenceResolver = new ReferenceResolver(storage);

                // services
                this.diffService = new DiffService(snapshotNormalizer, config);
                this.schemaDiffService = new SchemaDiffService();

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

                this.restoreService = new RestoreService(adapter);
                this.restorePlanner = new RestorePlanner();
                this.dataSourceTransactionManager = dataSourceTransactionManager;
        }

        private String buildJdbcUrl(DataGitConfig config) {
                var db = config.getDatabaseConfig();

                return "jdbc:postgresql://"
                                + db.getHost() + ":"
                                + db.getPort() + "/"
                                + db.getName();
        }

}
