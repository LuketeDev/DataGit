package com.lukete.datagit.cli.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.bootstrap.DataGitContext;
import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.CliExecutionExceptionHandler;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;
import com.lukete.datagit.core.service.ReferenceResolver;
import com.lukete.datagit.core.service.RestoreService;

import picocli.CommandLine;

class RestoreCommandTest {

    @Test
    void shouldResolveProvidedSnapshotReference() {
        CommandFixture fixture = new CommandFixture();
        fixture.storage.save(snapshot("snap-1", "2026-04-08T10:00:00Z"));
        fixture.storage.save(snapshot("snap-2", "2026-04-08T11:00:00Z"));

        int exitCode = fixture.executeAsSubcommand("checkout", "HEAD~1", "--yes");

        assertThat(exitCode).isZero();
        assertThat(fixture.adapter.restoredSnapshot.id()).isEqualTo("snap-1");
    }

    @Test
    void shouldPrintPlanAndSkipRestoreWhenConfirmationIsMissing() {
        CommandFixture fixture = new CommandFixture();
        fixture.storage.save(snapshot("snap-1", "2026-04-08T10:00:00Z"));

        int exitCode = fixture.execute("HEAD");

        assertThat(exitCode).isZero();
        assertThat(fixture.adapter.restoredSnapshot).isNull();
        assertThat(fixture.combinedOutput())
                .contains("This operation will overwrite current table data.")
                .contains("Target snapshot: snap-1")
                .contains("Source: postgres")
                .contains("Tables: 2")
                .contains("Run again with --yes to confirm:")
                .contains("datagit checkout HEAD --yes");
    }

    @Test
    void shouldRestoreAndPrintSuccessWhenConfirmed() {
        CommandFixture fixture = new CommandFixture();
        fixture.storage.save(snapshot("snap-1", "2026-04-08T10:00:00Z"));

        int exitCode = fixture.execute("HEAD", "--yes");

        assertThat(exitCode).isZero();
        assertThat(fixture.adapter.restoredSnapshot.id()).isEqualTo("snap-1");
        assertThat(fixture.out()).contains("Database restored from snapshot: snap-1");
    }

    @Test
    void shouldRenderResolverExceptionThroughCliExceptionHandler() {
        CommandFixture fixture = new CommandFixture();

        int exitCode = fixture.execute("missing", "--yes");

        assertThat(exitCode).isNotZero();
        assertThat(fixture.err())
                .contains("[!!]")
                .contains("No snapshots")
                .contains("datagit snapshot");
        assertThat(fixture.adapter.restoredSnapshot).isNull();
    }

    private static Snapshot snapshot(String id, String timestamp) {
        return new Snapshot(
                id,
                Instant.parse(timestamp),
                "postgres",
                Map.of(
                        "users", List.of(Map.of("id", 1, "name", "Lucas")),
                        "orders", List.of(Map.of("id", 10, "total", 42))));
    }

    private static class CommandFixture {
        private final StringWriter out = new StringWriter();
        private final StringWriter err = new StringWriter();
        private final InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        private final RecordingAdapter adapter = new RecordingAdapter();
        private final CliPrinter printer = new CliPrinter(new PrintWriter(out), new PrintWriter(err));

        private int execute(String... args) {
            return commandLine().execute(args);
        }

        private int executeAsSubcommand(String... args) {
            DataGitCommand rootCommand = new DataGitCommand();
            rootCommand.setPrinter(printer);

            CommandLine commandLine = new CommandLine(rootCommand);
            commandLine.addSubcommand("checkout", restoreCommand());
            commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(rootCommand));
            commandLine.setOut(new PrintWriter(out));
            commandLine.setErr(new PrintWriter(err));

            return commandLine.execute(args);
        }

        private CommandLine commandLine() {
            DataGitCommand rootCommand = new DataGitCommand();
            rootCommand.setPrinter(printer);

            CommandLine commandLine = new CommandLine(restoreCommand());
            commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(rootCommand));
            commandLine.setOut(new PrintWriter(out));
            commandLine.setErr(new PrintWriter(err));

            return commandLine;
        }

        private RestoreCommand restoreCommand() {
            ReferenceResolver resolver = new ReferenceResolver(storage);
            RestoreService restoreService = new RestoreService(adapter);
            DataGitContext context = new DataGitContext(resolver, restoreService);
            return new RestoreCommand(new FakeContextProvider(context), printer);
        }

        private String out() {
            return out.toString();
        }

        private String err() {
            return err.toString();
        }

        private String combinedOutput() {
            return out() + err();
        }
    }

    private static class FakeContextProvider extends DataGitContextProvider {
        private final DataGitContext context;

        private FakeContextProvider(DataGitContext context) {
            this.context = context;
        }

        @Override
        public DataGitContext get() {
            return context;
        }
    }

    private static class RecordingAdapter implements DataSourceAdapter {
        private Snapshot restoredSnapshot;

        @Override
        public Snapshot extract() {
            return null;
        }

        @Override
        public void restore(Snapshot snapshot) {
            this.restoredSnapshot = snapshot;
        }
    }

    private static class InMemorySnapshotStorage implements SnapshotStorage {
        private final List<Snapshot> snapshots = new ArrayList<>();

        @Override
        public void save(Snapshot snapshot) {
            snapshots.add(snapshot);
        }

        @Override
        public Optional<Snapshot> load(String id) {
            return snapshots.stream()
                    .filter(snapshot -> snapshot.id().equals(id))
                    .findFirst();
        }

        @Override
        public List<Snapshot> list() {
            return new ArrayList<>(snapshots);
        }
    }
}
