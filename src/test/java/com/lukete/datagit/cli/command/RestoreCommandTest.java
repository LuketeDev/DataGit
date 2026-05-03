package com.lukete.datagit.cli.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lukete.datagit.bootstrap.DataGitContext;
import com.lukete.datagit.bootstrap.DataGitContextProvider;
import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.cli.output.RestorePlanRenderer;
import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.exception.CliExecutionExceptionHandler;
import com.lukete.datagit.core.exception.InvalidCommandOptionsException;
import com.lukete.datagit.core.ports.DataSourceAdapter;
import com.lukete.datagit.core.ports.SnapshotStorage;
import com.lukete.datagit.core.service.ReferenceResolver;
import com.lukete.datagit.core.service.RestorePlanner;
import com.lukete.datagit.core.service.RestoreService;
import com.lukete.datagit.core.service.SnapshotService;

import picocli.CommandLine;

class RestoreCommandTest {

    @Test
    void checkoutWithoutFlagsShouldPrintPlanSkipRestoreSkipSafetySnapshotAndShowHint() {
        CommandFixture fixture = new CommandFixture(snapshot("snap-1"));

        int exitCode = fixture.execute("HEAD");

        assertThat(exitCode).isZero();
        assertThat(fixture.restoreService.restoreCalled).isFalse();
        assertThat(fixture.snapshotService.createSnapshotCalls).isZero();
        assertThat(fixture.combinedOutput())
                .contains("WARN")
                .contains("INFO")
                .contains("TIP")
                .contains("This operation will overwrite current database data")
                .contains("Target snapshot: snap-1")
                .contains("Source: postgres")
                .contains("Tables affected: 2")
                .contains("Rows to restore: 3")
                .contains("Run again with --yes to confirm:")
                .contains("datagit checkout HEAD --yes");
        assertNoUnexpectedLoggerOutput(fixture);
    }

    @Test
    void checkoutWithDryRunShouldPrintPlanAndSkipRestoreAndSafetySnapshot() {
        CommandFixture fixture = new CommandFixture(snapshot("snap-1"));

        int exitCode = fixture.execute("HEAD", "--dry-run");

        assertThat(exitCode).isZero();
        assertThat(fixture.restoreService.restoreCalled).isFalse();
        assertThat(fixture.snapshotService.createSnapshotCalls).isZero();
        assertThat(fixture.combinedOutput())
                .contains("WARN")
                .contains("INFO")
                .contains("OK")
                .contains("Target snapshot: snap-1")
                .contains("Dry run completed");
        assertNoUnexpectedLoggerOutput(fixture);
    }

    @Test
    void checkoutWithYesShouldCreateSafetySnapshotFirstRestoreAfterSnapshotAndPrintSuccess() {
        CommandFixture fixture = new CommandFixture(snapshot("snap-1"));

        int exitCode = fixture.execute("HEAD", "--yes");

        assertThat(exitCode).isZero();
        assertThat(fixture.snapshotService.createSnapshotCalls).isEqualTo(1);
        assertThat(fixture.restoreService.restoreCalls).isEqualTo(1);
        assertThat(fixture.restoreService.restoredSnapshot.id()).isEqualTo("snap-1");
        assertThat(fixture.callOrder).containsExactly("snapshot", "restore");
        assertThat(fixture.combinedOutput())
                .contains("WARN")
                .contains("INFO")
                .contains("OK")
                .contains("Creating safety snapshot before restore")
                .contains("Safety snapshot created: safety-1")
                .contains("Database restored from snapshot: snap-1");
        assertNoUnexpectedLoggerOutput(fixture);
    }

    @Test
    void checkoutWithYesAndDryRunShouldReturnInvalidCommandOptionsException() {
        CommandFixture fixture = new CommandFixture(snapshot("snap-1"));

        int exitCode = fixture.execute("HEAD", "--yes", "--dry-run");

        assertThat(exitCode).isNotZero();
        assertThat(fixture.err()).contains("Options --yes and --dry-run cannot be used together");
        assertThat(fixture.restoreService.restoreCalled).isFalse();
        assertThat(fixture.snapshotService.createSnapshotCalls).isZero();
    }

    @Test
    void checkoutWithYesAndDryRunShouldThrowInvalidCommandOptionsExceptionFromCommand() {
        CommandFixture fixture = new CommandFixture(snapshot("snap-1"));
        RestoreCommand command = fixture.restoreCommand();
        new CommandLine(command).parseArgs("HEAD", "--yes", "--dry-run");

        org.assertj.core.api.Assertions.assertThatThrownBy(command::run)
                .isInstanceOf(InvalidCommandOptionsException.class)
                .hasMessageContaining("--yes and --dry-run");
    }

    @Test
    void shouldResolveProvidedSnapshotReference() {
        CommandFixture fixture = new CommandFixture(snapshot("snap-1"), snapshot("snap-2"));

        int exitCode = fixture.executeAsSubcommand("checkout", "HEAD~1", "--yes");

        assertThat(exitCode).isZero();
        assertThat(fixture.restoreService.restoredSnapshot.id()).isEqualTo("snap-1");
    }

    @Test
    void resolverExceptionShouldPropagateThroughCliErrorHandling() {
        CommandFixture fixture = new CommandFixture();

        int exitCode = fixture.execute("missing", "--yes");

        assertThat(exitCode).isNotZero();
        assertThat(fixture.err())
                .contains("[!!]")
                .contains("No snapshots")
                .contains("datagit snapshot");
        assertThat(fixture.restoreService.restoreCalled).isFalse();
        assertThat(fixture.snapshotService.createSnapshotCalls).isZero();
    }

    @Test
    void snapshotWithZeroTablesShouldStillPrintPlanCorrectly() {
        CommandFixture fixture = new CommandFixture(snapshot("empty", Map.of()));

        int exitCode = fixture.execute("HEAD");

        assertThat(exitCode).isZero();
        assertThat(fixture.combinedOutput())
                .contains("WARN")
                .contains("INFO")
                .contains("TIP")
                .contains("Target snapshot: empty")
                .contains("Tables affected: 0")
                .contains("Rows to restore: 0")
                .contains("TABLE")
                .contains("ROWS");
        assertThat(fixture.restoreService.restoreCalled).isFalse();
        assertNoUnexpectedLoggerOutput(fixture);
    }

    @Test
    void snapshotWithLargeNumberOfTablesShouldStillRender() {
        CommandFixture fixture = new CommandFixture(snapshot("large", manyTables(40)));

        int exitCode = fixture.execute("HEAD", "--dry-run");

        assertThat(exitCode).isZero();
        assertThat(fixture.combinedOutput())
                .contains("WARN")
                .contains("INFO")
                .contains("Target snapshot: large")
                .contains("Tables affected: 40")
                .contains("Rows to restore: 40")
                .contains("table_001")
                .contains("table_040")
                .contains("Dry run completed");
        assertNoUnexpectedLoggerOutput(fixture);
    }

    private static void assertNoUnexpectedLoggerOutput(CommandFixture fixture) {
        assertThat(fixture.combinedOutput())
                .doesNotContain("SLF4J")
                .doesNotContain("Exception")
                .doesNotContain("at com.lukete");
    }

    private static Snapshot snapshot(String id) {
        return snapshot(id, defaultTables());
    }

    private static Snapshot snapshot(String id, Map<String, List<Map<String, Object>>> tables) {
        int offset = switch (id) {
            case "snap-1" -> 1;
            case "snap-2" -> 2;
            default -> 0;
        };
        return new Snapshot(
                id,
                Instant.parse("2026-04-08T10:00:00Z").plusSeconds(offset),
                "postgres",
                tables);
    }

    private static Map<String, List<Map<String, Object>>> defaultTables() {
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        tables.put("users", List.of(
                Map.of("id", 1, "name", "Lucas"),
                Map.of("id", 2, "name", "Maya")));
        tables.put("orders", List.of(Map.of("id", 10, "total", 42)));
        return tables;
    }

    private static Map<String, List<Map<String, Object>>> manyTables(int count) {
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        for (int i = 1; i <= count; i++) {
            tables.put(String.format("table_%03d", i), List.of(Map.of("id", i)));
        }
        return tables;
    }

    private static class CommandFixture {
        private final StringWriter out = new StringWriter();
        private final StringWriter err = new StringWriter();
        private final InMemorySnapshotStorage storage = new InMemorySnapshotStorage();
        private final List<String> callOrder = new ArrayList<>();
        private final CliPrinter printer = new CliPrinter(new PrintWriter(out, true), new PrintWriter(err, true));
        private final RecordingRestoreService restoreService = new RecordingRestoreService(callOrder);
        private final RecordingSnapshotService snapshotService = new RecordingSnapshotService(callOrder);

        private CommandFixture(Snapshot... snapshots) {
            for (Snapshot snapshot : snapshots) {
                storage.save(snapshot);
            }
        }

        private int execute(String... args) {
            return commandLine().execute(args);
        }

        private int executeAsSubcommand(String... args) {
            DataGitCommand rootCommand = new DataGitCommand();
            rootCommand.setPrinter(printer);

            CommandLine commandLine = new CommandLine(rootCommand);
            commandLine.addSubcommand("checkout", restoreCommand());
            commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(rootCommand));
            commandLine.setOut(new PrintWriter(out, true));
            commandLine.setErr(new PrintWriter(err, true));

            return commandLine.execute(args);
        }

        private CommandLine commandLine() {
            DataGitCommand rootCommand = new DataGitCommand();
            rootCommand.setPrinter(printer);

            CommandLine commandLine = new CommandLine(restoreCommand());
            commandLine.setExecutionExceptionHandler(new CliExecutionExceptionHandler(rootCommand));
            commandLine.setOut(new PrintWriter(out, true));
            commandLine.setErr(new PrintWriter(err, true));

            return commandLine;
        }

        private RestoreCommand restoreCommand() {
            ReferenceResolver resolver = new ReferenceResolver(storage);
            DataGitContext context = new FakeDataGitContext(
                    resolver,
                    restoreService,
                    snapshotService,
                    new RestorePlanner());
            RestorePlanRenderer renderer = new RestorePlanRenderer(printer);
            return new RestoreCommand(new FakeContextProvider(context), renderer, printer);
        }

        private String err() {
            return err.toString();
        }

        private String combinedOutput() {
            return out + err.toString();
        }
    }

    private static class FakeDataGitContext extends DataGitContext {
        private final SnapshotService snapshotService;
        private final RestorePlanner restorePlanner;

        private FakeDataGitContext(
                ReferenceResolver referenceResolver,
                RestoreService restoreService,
                SnapshotService snapshotService,
                RestorePlanner restorePlanner) {
            super(referenceResolver, restoreService);
            this.snapshotService = snapshotService;
            this.restorePlanner = restorePlanner;
        }

        @Override
        public SnapshotService getSnapshotService() {
            return snapshotService;
        }

        @Override
        public RestorePlanner getRestorePlanner() {
            return restorePlanner;
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

    private static class RecordingRestoreService extends RestoreService {
        private final List<String> callOrder;
        private boolean restoreCalled;
        private int restoreCalls;
        private Snapshot restoredSnapshot;

        private RecordingRestoreService(List<String> callOrder) {
            super(new NoopAdapter());
            this.callOrder = callOrder;
        }

        @Override
        public void restore(Snapshot snapshot) {
            restoreCalled = true;
            restoreCalls++;
            restoredSnapshot = snapshot;
            callOrder.add("restore");
        }
    }

    private static class RecordingSnapshotService extends SnapshotService {
        private final List<String> callOrder;
        private int createSnapshotCalls;

        private RecordingSnapshotService(List<String> callOrder) {
            super(null, null, null, null);
            this.callOrder = callOrder;
        }

        @Override
        public Snapshot createSnapshot() {
            createSnapshotCalls++;
            callOrder.add("snapshot");
            return new Snapshot("safety-1", Instant.parse("2026-04-08T12:00:00Z"), "postgres", Map.of());
        }
    }

    private static class NoopAdapter implements DataSourceAdapter {
        @Override
        public Snapshot extract() {
            return null;
        }

        @Override
        public void restore(Snapshot snapshot) {
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
