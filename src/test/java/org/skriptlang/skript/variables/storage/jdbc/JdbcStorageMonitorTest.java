package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.VariableStorage;
import org.skriptlang.skript.variables.storage.VariableStorageManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests monitoring a table for other servers' changes: {@link JdbcStorage}'s monitor thread and its polls.
 * Two "servers" share one SQLite file, each with its own manager, variables and main thread, through
 * {@link SharedStorage}, which acts like a database several servers can use. SQLite and MySQL run the same
 * monitoring code, so this covers MySQL's too.
 * <p>
 * Most tests set a long monitor interval and call {@link JdbcStorage#poll()} themselves, so nothing depends on timing.
 * Reports are applied on each server's main thread, which here is a queue the test runs with {@link Server#runTasks()}.
 */
public class JdbcStorageMonitorTest {

	private static final String TEXT = "text";
	private static final String TABLE = JdbcStorage.DEFAULT_TABLE;
	private static final String MANUAL = "monitor interval: 1 hour"; // the thread waits first, so it never polls

	private static volatile Path sharedFile;

	private Path folder;
	private final List<Server> servers = new ArrayList<>();

	/**
	 * An SQLite storage that acts like a database other servers can share: it opens {@link #sharedFile} itself instead
	 * of claiming it, so two storages can use it at once, and it supports monitoring.
	 */
	static final class SharedStorage extends SQLiteStorage {

		SharedStorage(SkriptAddon registeringAddon, String type) {
			super(registeringAddon, type);
		}

		@Override
		protected boolean requiresFile() {
			return false;
		}

		@Override
		protected Connection connect() throws SQLException {
			return DriverManager.getConnection("jdbc:sqlite:" + sharedFile.toAbsolutePath());
		}

		@Override
		protected String tableKey(String table) {
			return "shared#" + System.identityHashCode(this); // each storage is another server, so never the same
		}

		@Override
		protected boolean supportsMonitoring() {
			return true;
		}

		@Override
		protected long saveIntervalMillis() {
			return TimeUnit.HOURS.toMillis(1); // only saves when a test saves or flushes, so it can stage races
		}

	}

	/**
	 * One server: its manager, its variables, and its main thread's queue of tasks.
	 */
	private final class Server {

		final VariableMap globals = VariableMap.concurrent();
		final Queue<Runnable> mainThread = new ConcurrentLinkedQueue<>();
		final VariableStorageManager manager = new VariableStorageManager(globals,
			value -> value == null ? null : value(String.valueOf(value)),
			(type, data) -> type.equals(TEXT) ? new String(data, StandardCharsets.UTF_8) : null,
			mainThread::add);

		/**
		 * @param plainSqlite whether to register plain SQLite instead of the shared storage. Only one of them can be,
		 *                    since a subclass of a registered type can't be registered too
		 */
		Server(boolean plainSqlite) {
			if (plainSqlite) {
				manager.register(null, SQLiteStorage.class, SQLiteStorage::new, "sqlite");
			} else {
				manager.register(null, SharedStorage.class, SharedStorage::new, "shared");
			}
			servers.add(this);
		}

		JdbcStorage storage() {
			return (JdbcStorage) manager.storages().get(0);
		}

		/**
		 * Polls without applying what it reported: the reports wait on the main thread until {@link #runTasks()}.
		 */
		void pollOnly() {
			try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
				storage().poll();
			}
		}

		/**
		 * Saves what's queued, as the saving thread would: drains the queue, then saves it.
		 */
		void flush() {
			try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
				assertTrue(storage().flush());
			}
		}

		/**
		 * Polls, then applies what the poll reported, as the main thread would.
		 *
		 * @return how many changes the poll reported
		 */
		int poll() {
			try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
				storage().poll();
			}
			return runTasks();
		}

		// how many reports the storage had made when runTasks last counted them
		private long counted;

		/**
		 * Runs the main thread's queued tasks, until none are left.
		 *
		 * @return how many changes the storage reported since the last call, which these tasks applied
		 */
		int runTasks() {
			try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
				Runnable task;
				while ((task = mainThread.poll()) != null)
					task.run();
			}
			long reported = storage().reportCount();
			int count = (int) (reported - counted);
			counted = reported;
			return count;
		}

		Object get(String name) {
			return globals.getVariable(name);
		}

		void close() {
			try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
				manager.close();
			}
		}

	}

	@Before
	public void setUp() throws IOException {
		folder = Files.createTempDirectory("skript-monitor-test");
		sharedFile = folder.resolve("shared.sqlite");
	}

	@After
	public void tearDown() throws IOException {
		servers.forEach(Server::close);
		try (Stream<Path> paths = Files.walk(folder)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
				Files.delete(path);
		}
	}

	// ---- helpers ----

	private static SectionNode node(String databases) {
		String config = "databases:\n" + databases;
		try {
			return (SectionNode) new Config(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)),
				"test.sk", false, true, ":").getMainNode().get("databases");
		} catch (IOException e) {
			throw new UncheckedIOException(e); // can't happen when reading from memory
		}
	}

	/**
	 * @return The shared database's section, with the given extra lines
	 */
	private static String shared(String... lines) {
		StringBuilder section = new StringBuilder("\tdefault:\n\t\ttype: shared\n\t\tbackup interval: 0\n");
		for (String line : lines)
			section.append("\t\t").append(line).append("\n");
		return section.toString();
	}

	/**
	 * Starts a server on the shared table, monitoring it, with the given extra lines.
	 */
	private Server server(String... lines) {
		Server server = new Server(false);
		List<String> all = new ArrayList<>(List.of("monitor changes: true"));
		all.addAll(List.of(lines));
		if (all.stream().noneMatch(line -> line.startsWith("monitor interval")))
			all.add(MANUAL);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertTrue(server.manager.load(node(shared(all.toArray(String[]::new)))));
		}
		awaitSavingIdle(server.storage());
		return server;
	}

	/**
	 * Waits until the storage's saving thread has finished its first cycle, which runs as soon as it starts, and is
	 * waiting out its hour. After that it only saves when a test makes it. It waits with the storage as its blocker.
	 */
	private static void awaitSavingIdle(JdbcStorage storage) {
		String name = "Skript variable storage " + storage.getName() + " saving thread";
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (System.nanoTime() < deadline) {
			for (Thread thread : Thread.getAllStackTraces().keySet()) {
				if (thread.getName().equals(name) && LockSupport.getBlocker(thread) == storage)
					return;
			}
			Thread.onSpinWait();
		}
		throw new AssertionError("The saving thread never went idle");
	}

	/**
	 * @return How many names the storage remembers having reported. Reads its private map.
	 */
	private static int inFlight(Server server) throws ReflectiveOperationException {
		Field field = JdbcStorage.class.getDeclaredField("inFlight");
		field.setAccessible(true);
		Map<?, ?> inFlight = (Map<?, ?>) field.get(server.storage());
		synchronized (inFlight) {
			return inFlight.size();
		}
	}

	/**
	 * Loads the databases into a new server, expecting it to fail or succeed, and returns the errors and warnings.
	 */
	private List<String> loadMessages(boolean successful, String databases) {
		Server server = new Server(databases.contains("type: sqlite"));
		try (RetainingLogHandler log = new RetainingLogHandler().start()) {
			assertEquals(successful, server.manager.load(node(databases)));
			return log.getLog().stream()
				.filter(entry -> entry.getLevel().intValue() >= Level.WARNING.intValue())
				.map(LogEntry::getMessage)
				.toList();
		}
	}

	private static void assertMentions(List<String> messages, String part) {
		assertTrue("nothing mentions '" + part + "': " + messages, messages.stream().anyMatch(m -> m.contains(part)));
	}

	private static SerializedVariable.Value value(String text) {
		return new SerializedVariable.Value(TEXT, text.getBytes(StandardCharsets.UTF_8));
	}

	private static SerializedVariable change(String name, String text) {
		return new SerializedVariable(name, value(text));
	}

	private static SerializedVariable deletion(String name) {
		return new SerializedVariable(name, null);
	}

	/**
	 * Saves a batch straight to the server's storage, as its saving thread would.
	 */
	private static void save(Server server, List<String> clears, SerializedVariable... changes) throws IOException {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			server.storage().save(new VariableStorage.Batch(clears, List.of(changes)));
		}
	}

	private static void save(Server server, SerializedVariable... changes) throws IOException {
		save(server, List.of(), changes);
	}

	private static void sql(String... statements) throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sharedFile.toAbsolutePath());
			 Statement statement = connection.createStatement()) {
			for (String sql : statements)
				statement.execute(sql);
		}
	}

	/**
	 * Puts rows into the table directly, as another server's save would, under a new version.
	 *
	 * @param rows name, type and value of each row, where a null type or value makes a tombstone or a broken row
	 */
	private static void insertAsAnotherServer(String[]... rows) throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sharedFile.toAbsolutePath())) {
			connection.setAutoCommit(false);
			long version;
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("UPDATE " + TABLE + "_meta SET version = version + 1 WHERE id = 0");
				var result = statement.executeQuery("SELECT version FROM " + TABLE + "_meta WHERE id = 0");
				result.next();
				version = result.getLong(1);
			}
			try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO " + TABLE
					+ " (name, type, value, version, server) VALUES (?, ?, ?, ?, 'another server')")) {
				for (String[] row : rows) {
					statement.setString(1, row[0]);
					statement.setString(2, row[1]);
					statement.setBytes(3, row[2] == null ? null : row[2].getBytes(StandardCharsets.UTF_8));
					statement.setLong(4, version);
					statement.executeUpdate();
				}
			}
			connection.commit();
		}
	}

	/**
	 * @return how many monitor threads of storages named {@code default} are running. Every test's databases are named
	 * that, so tests that count threads only start one server
	 */
	private static long monitorThreads() {
		String name = "Skript variable storage default monitor thread";
		return Thread.getAllStackTraces().keySet().stream()
			.filter(thread -> thread.getName().equals(name) && thread.isAlive())
			.count();
	}

	// ---- configuring ----

	@Test
	public void monitoringIsOffByDefault() {
		Server server = new Server(false);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertTrue(server.manager.load(node(shared())));
		}
		assertEquals(0, monitorThreads());
	}

	@Test
	public void monitoringStartsTheThread() {
		server();
		assertEquals(1, monitorThreads());
	}

	@Test
	public void anInvalidMonitorChangesFailsConfiguring() {
		assertMentions(loadMessages(false, shared("monitor changes: lots")), "'monitor changes'");
	}

	@Test
	public void anInvalidMonitorIntervalFailsConfiguring() {
		assertMentions(loadMessages(false, shared("monitor changes: true", "monitor interval: soon")), "'monitor interval'");
	}

	@Test
	public void aTooShortMonitorIntervalFailsConfiguring() {
		assertMentions(loadMessages(false, shared("monitor changes: true", "monitor interval: 10 milliseconds")),
			"at least 100 milliseconds");
	}

	@Test
	public void theMonitorIntervalIsIgnoredWithoutMonitoring() {
		assertTrue(loadMessages(true, shared("monitor interval: 10 milliseconds")).isEmpty());
	}

	@Test
	public void aStorageThatCantBeMonitoredWarnsAndDoesntMonitor() {
		String sqlite = "\tdefault:\n\t\ttype: sqlite\n\t\tfile: " + folder.resolve("own.sqlite").toString().replace('\\', '/')
			+ "\n\t\tbackup interval: 0\n\t\tmonitor changes: true\n";
		assertMentions(loadMessages(true, sqlite), "can't monitor changes");
		assertEquals(0, monitorThreads());
	}

	// ---- what a poll reports ----

	@Test
	public void aChangeFromAnotherServerIsReported() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));

		assertEquals(1, b.poll());
		assertEquals("1", b.get("x"));
	}

	@Test
	public void aDeletionFromAnotherServerIsReported() throws Exception {
		Server a = server();
		save(a, change("x", "1"), change("y", "2"));
		Server b = server();
		assertEquals("1", b.get("x")); // loaded

		save(a, deletion("x"));
		assertEquals(1, b.poll());
		assertNull(b.get("x"));
		assertEquals("2", b.get("y"));
	}

	@Test
	public void aClearFromAnotherServerIsReportedRowByRow() throws Exception {
		Server a = server();
		save(a, change("list::1", "1"), change("list::2", "2"), change("listing", "kept"));
		Server b = server();

		save(a, List.of("list::"));
		assertEquals(2, b.poll());
		assertNull(b.get("list::1"));
		assertNull(b.get("list::2"));
		assertEquals("kept", b.get("listing"));
	}

	@Test
	public void ownRowsArentReported() throws Exception {
		Server b = server();
		save(b, change("mine", "1"), deletion("gone"));

		assertEquals(0, b.poll());
	}

	@Test
	public void rowsAreOnlyReportedOnce() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));

		assertEquals(1, b.poll());
		assertEquals(0, b.poll());
	}

	@Test
	public void rowsAlreadyLoadedArentReported() throws Exception {
		Server a = server();
		save(a, change("x", "1"));
		Server b = server();

		assertEquals(0, b.poll());
	}

	@Test
	public void eachPollSeesNewerCommits() throws Exception {
		// a poll that left its read transaction open would keep seeing the same snapshot forever
		Server a = server();
		Server b = server();
		assertEquals(0, b.poll());

		save(a, change("x", "1"));
		assertEquals(1, b.poll());
		save(a, change("x", "2"));
		assertEquals(1, b.poll());
		assertEquals("2", b.get("x"));
	}

	@Test
	public void aNameChangedManyTimesIsReportedOnceWithItsLatestValue() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		save(a, change("x", "2"));
		save(a, change("x", "3"));

		assertEquals(1, b.poll());
		assertEquals("3", b.get("x"));
	}

	@Test
	public void manyRowsInOneSaveAreAllReported() throws Exception {
		Server a = server();
		Server b = server();
		int count = 2_500; // three pages of 1000
		SerializedVariable[] changes = new SerializedVariable[count];
		for (int i = 0; i < count; i++)
			changes[i] = change("many::" + i, String.valueOf(i));
		save(a, changes);

		assertEquals(count, b.poll());
		for (int i = 0; i < count; i++)
			assertEquals(String.valueOf(i), b.get("many::" + i));
	}

	@Test
	public void manyRowsOverManySavesAreAllReported() throws Exception {
		Server a = server();
		Server b = server();
		for (int save = 0; save < 5; save++) {
			SerializedVariable[] changes = new SerializedVariable[700]; // pages end in the middle of a save
			for (int i = 0; i < changes.length; i++)
				changes[i] = change("s" + save + "::" + i, save + "/" + i);
			save(a, changes);
		}

		assertEquals(3_500, b.poll());
		assertEquals("4/699", b.get("s4::699"));
		assertEquals("0/0", b.get("s0::0"));
	}

	// ---- bad rows ----

	@Test
	public void aBrokenRowIsntReportedAsADeletion() throws Exception {
		Server a = server();
		save(a, change("x", "1"));
		Server b = server();
		insertAsAnotherServer(new String[] {"x", TEXT, null}, new String[] {"y", TEXT, "2"});

		assertEquals(1, b.poll()); // only y
		assertEquals("1", b.get("x"));
		assertEquals("2", b.get("y"));
	}

	@Test
	public void aRowNamedLikeAListDoesntStopPolling() throws Exception {
		Server b = server();
		insertAsAnotherServer(new String[] {"x::*", null, null}, new String[] {"y", TEXT, "2"});

		assertEquals(1, b.poll()); // only y
		assertEquals("2", b.get("y"));
		insertAsAnotherServer(new String[] {"z", TEXT, "3"});
		assertEquals(1, b.poll()); // and the cursor moved past it
	}

	@Test
	public void anUnreadableTypeIsIgnored() throws Exception {
		Server b = server();
		insertAsAnotherServer(new String[] {"x", "a type from a missing addon", "?"});

		b.poll();
		assertNull(b.get("x"));
	}

	// ---- reports racing local saves ----

	@Test
	public void aReportAppliedAfterALocalSaveIsCorrected() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));
		b.manager.set("x", "local");

		b.pollOnly(); // reads remote; the report waits on the main thread
		b.flush(); // local leaves the queue and is saved, after remote
		b.runTasks(); // the report finds nothing pending, so it applies remote, then the re-report puts local back

		assertEquals("local", b.get("x"));
		b.close();
		servers.remove(b);
		assertEquals("local", server().get("x")); // and that's what the table holds
	}

	@Test
	public void aReportAppliedBetweenTheDrainAndTheSaveIsCorrected() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));
		b.globals.setVariable("x", "local"); // as if set and already drained, so not pending any more

		b.pollOnly();
		b.runTasks(); // the report applies remote before the drained batch is saved
		assertEquals("remote", b.get("x"));
		save(b, change("x", "local")); // the drained batch's save
		b.runTasks();

		assertEquals("local", b.get("x"));
	}

	@Test
	public void aClearRacingAReportIsCorrected() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("list::1", "remote"));
		b.manager.set("list::*", null);

		b.pollOnly();
		b.flush(); // the clear tombstones list::1, after remote
		b.runTasks();

		assertNull(b.get("list::1"));
	}

	@Test
	public void aNewerLocalChangeBeatsTheReReport() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));
		b.manager.set("x", "local");

		b.pollOnly();
		b.flush();
		b.manager.set("x", "newer"); // pending when the report and the re-report are applied
		b.runTasks();

		assertEquals("newer", b.get("x"));
	}

	@Test
	public void aReportReadAfterALocalSaveStillApplies() throws Exception {
		Server a = server();
		Server b = server();
		b.manager.set("x", "local");
		b.flush();
		save(a, change("x", "later")); // another server's save after ours is newer, so it wins

		b.poll();

		assertEquals("later", b.get("x"));
	}

	@Test
	public void appliedReportsAreForgottenAfterTheNextSaveCycle() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		b.poll();
		assertEquals(1, inFlight(b));

		b.manager.set("y", "2");
		b.flush();
		assertEquals(0, inFlight(b));
	}

	@Test
	public void aServerThatNeverSavesStillForgets() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		b.poll();

		b.flush(); // nothing to save: the cycle still ends
		assertEquals(0, inFlight(b));
	}

	@Test
	public void waitingReportsAreRememberedUntilApplied() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		b.pollOnly();
		b.flush();
		b.flush();
		assertEquals(1, inFlight(b)); // the main thread hasn't applied it yet

		b.runTasks();
		b.flush();
		assertEquals(0, inFlight(b));
	}

	@Test
	public void aSaveOutsideACycleForgetsNothing() throws Exception {
		// save() alone stands for a drained batch: the next drain hasn't happened, so a report applied now may race it
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		b.poll();

		save(b, change("y", "2"));
		assertEquals(1, inFlight(b));
	}

	@Test
	public void unrelatedSavesDontReReport() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));
		b.poll();

		b.manager.set("y", "mine");
		b.flush();
		assertEquals(0, b.runTasks());
	}

	// ---- failures and stopping ----

	@Test
	public void aFailedPollIsTriedAgain() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		sql("ALTER TABLE " + TABLE + " RENAME TO hidden");

		assertEquals(0, b.poll());
		sql("ALTER TABLE hidden RENAME TO " + TABLE);
		assertEquals(1, b.poll()); // a new connection, and the same rows
		assertEquals("1", b.get("x"));
	}

	@Test
	public void aPollAfterClosingDoesNothing() throws Exception {
		Server a = server();
		Server b = server();
		JdbcStorage storage = b.storage();
		b.close();
		servers.remove(b);
		save(a, change("x", "1"));

		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			storage.poll();
		}
		assertEquals(0, b.runTasks());
	}

	@Test
	public void closingStopsTheMonitorThread() {
		Server b = server();
		assertEquals(1, monitorThreads());
		b.close();
		servers.remove(b);

		assertEquals(0, monitorThreads()); // close() waits for the thread to end
	}

	@Test
	public void theMonitorThreadPollsByItself() throws Exception {
		Server a = server();
		Server b = server("monitor interval: 100 milliseconds");
		save(a, change("x", "1"));

		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (b.mainThread.isEmpty() && System.nanoTime() < deadline)
			Thread.onSpinWait();
		assertEquals(1, b.runTasks());
		assertEquals("1", b.get("x"));
	}

}
