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
import org.skriptlang.skript.test.util.ExpectedExceptions;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.ManagerTests;
import org.skriptlang.skript.variables.storage.VariableStorage;
import org.skriptlang.skript.variables.storage.VariableStorageManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests monitoring a table for other servers' changes: {@link JdbcStorage}'s change log and its polls.
 * Two "servers" share one SQLite file, each with its own manager, variables and main thread, through
 * {@link SharedStorage}, which acts like a database several servers can use. SQLite and MySQL run the same
 * monitoring code, so this covers MySQL's too.
 * <p>
 * A storage polls at the end of each save cycle. The saving threads here wait an hour between cycles, so most tests
 * save with {@link Server#flush()}, which polls too, or call {@link JdbcStorage#poll()} themselves, and nothing depends
 * on timing. Reports are applied on each server's main thread, which here is a queue the test runs with
 * {@link Server#runTasks()}.
 */
public class JdbcStorageMonitorTest {

	private static final String TEXT = "text";
	private static final String TABLE = JdbcStorage.DEFAULT_TABLE;

	private static volatile Path sharedFile;
	// how often the next storage started saves; only one test changes it
	private static volatile long saveIntervalMillis = TimeUnit.HOURS.toMillis(1);

	private Path folder;
	private final List<Server> servers = new ArrayList<>();

	/**
	 * An SQLite storage that acts like a database other servers can share: it opens {@link #sharedFile} itself instead
	 * of claiming it, so two storages can use it at once.
	 */
	static final class SharedStorage extends ShareableSQLiteStorage {

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
		protected long saveIntervalMillis() {
			return saveIntervalMillis; // an hour unless a test changes it: only saves when a test saves or flushes
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
		 * Does a save cycle, as the saving thread would: drains the queue, saves it, then polls. What the poll reported
		 * waits on the main thread until {@link #runTasks()}.
		 *
		 * @return whether everything drained was saved
		 */
		boolean flush() {
			try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
				return storage().flush();
			}
		}

		/**
		 * Polls, then applies what the poll reported, as the main thread would.
		 *
		 * @return how many changes the poll reported
		 */
		int poll() {
			pollOnly();
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
				ManagerTests.runTickEnd(manager); // what a task left over once its time was up
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
		saveIntervalMillis = TimeUnit.HOURS.toMillis(1);
	}

	@After
	public void tearDown() throws IOException {
		servers.forEach(Server::close);
		saveIntervalMillis = TimeUnit.HOURS.toMillis(1);
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
	 * Starts a server on the shared table, monitoring it.
	 */
	private Server server() {
		Server server = new Server(false);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertTrue(server.manager.load(node(shared("monitor changes: true"))));
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
	 * Loads the databases into a new server, expecting it to fail or succeed, and returns the errors and warnings.
	 */
	private List<String> loadMessages(boolean successful, String databases) {
		Server server = new Server(databases.contains("type: sqlite"));
		try (RetainingLogHandler log = new RetainingLogHandler().start()) {
			assertEquals(successful, server.manager.load(node(databases)));
			return warnings(log);
		}
	}

	private static List<String> warnings(RetainingLogHandler log) {
		return log.getLog().stream()
			.filter(entry -> entry.getLevel().intValue() >= Level.WARNING.intValue())
			.map(LogEntry::getMessage)
			.toList();
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
	 * Saves a batch straight to the server's storage, as its saving thread would, without polling after.
	 */
	private static void save(Server server, List<String> clears, SerializedVariable... changes) throws IOException {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			server.storage().save(new VariableStorage.Batch(clears, List.of(changes)));
		}
	}

	private static void save(Server server, SerializedVariable... changes) throws IOException {
		save(server, List.of(), changes);
	}

	private static Connection open() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlite:" + sharedFile.toAbsolutePath());
	}

	private static void sql(String... statements) throws SQLException {
		try (Connection connection = open(); Statement statement = connection.createStatement()) {
			for (String sql : statements)
				statement.execute(sql);
		}
	}

	/**
	 * @return The first column of the first row the query gives, or null if it gives none
	 */
	private static Object query(String sql) throws SQLException {
		try (Connection connection = open(); Statement statement = connection.createStatement();
			 ResultSet result = statement.executeQuery(sql)) {
			return result.next() ? result.getObject(1) : null;
		}
	}

	/**
	 * Saves rows and their log entries directly, as another server's save would, under a new version.
	 *
	 * @param rows name, type and value of each row
	 */
	private static void saveAsAnotherServer(String[]... rows) throws SQLException {
		try (Connection connection = open()) {
			connection.setAutoCommit(false);
			long version;
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("UPDATE " + TABLE + "_meta SET version = version + 1 WHERE id = 0");
				try (ResultSet result = statement.executeQuery("SELECT version FROM " + TABLE + "_meta WHERE id = 0")) {
					result.next();
					version = result.getLong(1);
				}
			}
			try (PreparedStatement upsert = connection.prepareStatement("INSERT OR REPLACE INTO " + TABLE
					+ " (name, type, value) VALUES (?, ?, ?)");
			     PreparedStatement log = connection.prepareStatement("INSERT OR REPLACE INTO " + TABLE
					+ "_log (version, seq, name) VALUES (?, ?, ?)")) {
				int seq = 0;
				for (String[] row : rows) {
					upsert.setString(1, row[0]);
					upsert.setString(2, row[1]);
					upsert.setBytes(3, row[2].getBytes(StandardCharsets.UTF_8));
					upsert.executeUpdate();
					log.setLong(1, version);
					log.setInt(2, seq++);
					log.setString(3, row[0]);
					log.executeUpdate();
				}
			}
			connection.commit();
		}
	}

	// ---- configuring ----

	@Test
	public void monitoringIsOffByDefault() throws SQLException {
		Server server = new Server(false);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertTrue(server.manager.load(node(shared())));
		}
		assertNull("without monitoring, there's no change log",
			query("SELECT name FROM sqlite_master WHERE name = '" + TABLE + "_log'"));
	}

	@Test
	public void anInvalidMonitorChangesFailsConfiguring() {
		assertMentions(loadMessages(false, shared("monitor changes: lots")), "'monitor changes'");
	}

	@Test
	public void aStorageThatCantBeMonitoredWarnsAndDoesntMonitor() throws Exception {
		String sqlite = "\tdefault:\n\t\ttype: sqlite\n\t\tfile: " + folder.resolve("own.sqlite").toString().replace('\\', '/')
			+ "\n\t\tbackup interval: 0\n\t\tmonitor changes: true\n";
		assertMentions(loadMessages(true, sqlite), "can't monitor changes");
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
	public void aClearFromAnotherServerIsReportedAsOneClear() throws Exception {
		Server a = server();
		save(a, change("list::1", "1"), change("list::2", "2"), change("list::sub::3", "3"), change("listing", "kept"));
		Server b = server();

		save(a, List.of("list::"));
		assertEquals(1, b.poll());
		assertNull(b.get("list::1"));
		assertNull(b.get("list::2"));
		assertNull(b.get("list::sub::3"));
		assertEquals("kept", b.get("listing"));
	}

	@Test
	public void aSetAfterAClearInTheSameSaveSurvivesIt() throws Exception {
		Server a = server();
		save(a, change("list::1", "1"), change("list::2", "2"));
		Server b = server();

		save(a, List.of("list::"), change("list::2", "new"));
		assertEquals(2, b.poll());
		assertNull(b.get("list::1"));
		assertEquals("new", b.get("list::2"));
	}

	@Test
	public void aSetBeforeAClearIsClearedToo() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("list::1", "1"));
		save(a, List.of("list::"));

		b.poll();
		assertNull(b.get("list::1"));
	}

	@Test
	public void ownChangesArentReported() throws Exception {
		Server b = server();
		save(b, List.of("list::"), change("mine", "1"), deletion("gone"));

		assertEquals(0, b.poll());
	}

	@Test
	public void ownChangesBetweenAnotherServersArentReported() throws Exception {
		Server a = server();
		Server b = server();
		save(b, change("mine", "1"));
		save(a, change("theirs", "2"));
		save(b, change("mine too", "3"));

		assertEquals(1, b.poll());
		assertEquals("2", b.get("theirs"));
	}

	@Test
	public void anOwnChangeAnotherServerChangedIsReported() throws Exception {
		Server a = server();
		Server b = server();
		save(b, change("x", "1"));
		save(a, change("x", "2"));

		assertEquals(1, b.poll());
		assertEquals("2", b.get("x"));
	}

	@Test
	public void changesAreOnlyReportedOnce() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));

		assertEquals(1, b.poll());
		assertEquals(0, b.poll());
	}

	@Test
	public void changesAlreadyLoadedArentReported() throws Exception {
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
		save(a, deletion("x"));
		save(a, change("x", "3"));

		assertEquals(1, b.poll());
		assertEquals("3", b.get("x"));
	}

	@Test
	public void manyChangesInOneSaveAreAllReported() throws Exception {
		Server a = server();
		Server b = server();
		int count = 2_500; // more than one fetch, and more than one statement of log entries
		SerializedVariable[] changes = new SerializedVariable[count];
		for (int i = 0; i < count; i++)
			changes[i] = change("many::" + i, String.valueOf(i));
		save(a, changes);

		assertEquals(count, b.poll());
		for (int i = 0; i < count; i++)
			assertEquals(String.valueOf(i), b.get("many::" + i));
	}

	@Test
	public void manyChangesOverManySavesAreAllReported() throws Exception {
		Server a = server();
		Server b = server();
		for (int save = 0; save < 5; save++) {
			SerializedVariable[] changes = new SerializedVariable[700];
			for (int i = 0; i < changes.length; i++)
				changes[i] = change("s" + save + "::" + i, save + "/" + i);
			save(a, changes);
		}

		assertEquals(3_500, b.poll());
		assertEquals("4/699", b.get("s4::699"));
		assertEquals("0/0", b.get("s0::0"));
	}

	@Test
	public void anUnreadableTypeIsIgnored() throws Exception {
		Server b = server();
		saveAsAnotherServer(new String[] {"x", "a type from a missing addon", "?"}, new String[] {"y", TEXT, "2"});

		b.poll();
		assertNull(b.get("x"));
		assertEquals("2", b.get("y"));
	}

	// ---- local changes come first ----

	@Test
	public void theNextDrainWaitsUntilReportsAreApplied() throws Exception {
		Server a = server();
		Server b = server();
		assertTrue(b.storage().mayDrain());
		save(a, change("x", "1"));

		b.pollOnly();
		assertFalse(b.storage().mayDrain());
		b.runTasks();
		assertTrue(b.storage().mayDrain());
	}

	@Test
	public void aPollWithNothingToReportDoesntHoldUpDraining() {
		Server b = server();
		b.pollOnly();
		assertTrue(b.storage().mayDrain());
	}

	@Test
	public void aPendingLocalChangeBeatsAReport() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));
		b.manager.set("x", "local");

		b.poll(); // the report skips x, since b's change is newer and will be saved after a's
		assertEquals("local", b.get("x"));

		assertTrue(b.flush());
		assertEquals(1, a.poll());
		assertEquals("local", a.get("x")); // and both servers end up with it
	}

	@Test
	public void aPendingLocalChangeBeatsAClear() throws Exception {
		Server a = server();
		save(a, change("list::1", "1"), change("list::2", "2"));
		Server b = server();
		b.manager.set("list::2", "local");

		save(a, List.of("list::"));
		b.poll();
		assertNull(b.get("list::1"));
		assertEquals("local", b.get("list::2"));
	}

	@Test
	public void aPendingLocalClearBeatsAReport() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("list::1", "remote"));
		b.manager.set("list::*", null);

		b.poll();
		assertNull(b.get("list::1"));
	}

	@Test
	public void aChangeMadeWhileReportsWaitIsKept() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));

		b.pollOnly();
		b.manager.set("x", "newer"); // pending when the report is applied
		b.runTasks();

		assertEquals("newer", b.get("x"));
	}

	@Test
	public void aReportReadAfterALocalSaveStillApplies() throws Exception {
		Server a = server();
		Server b = server();
		b.manager.set("x", "local");
		assertTrue(b.flush());
		save(a, change("x", "later")); // another server's save after ours is newer, so it wins

		b.poll();

		assertEquals("later", b.get("x"));
	}

	// ---- polling after saving ----

	@Test
	public void aSaveCyclePollsAfterSaving() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "remote"));
		b.manager.set("y", "local");

		assertTrue(b.flush());
		assertEquals(1, b.runTasks());
		assertEquals("remote", b.get("x"));
		assertEquals("local", b.get("y"));
	}

	@Test
	public void aFailedSaveDoesntPoll() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		b.manager.set("y", "2");
		sql("ALTER TABLE " + TABLE + " RENAME TO hidden");

		try (RetainingLogHandler log = new RetainingLogHandler().start();
		     ExpectedExceptions exceptions = ExpectedExceptions.start()) {
			assertFalse(b.storage().flush());
			exceptions.assertReported("Could not save");
			assertFalse("polled: " + log.getLog(), warnings(log).stream().anyMatch(m -> m.contains("for changes")));
		}
		sql("ALTER TABLE hidden RENAME TO " + TABLE);
		assertTrue(b.flush()); // saves y, then polls
		assertEquals(1, b.runTasks());
		assertEquals("1", b.get("x"));
	}

	@Test
	public void theSavingThreadPollsByItself() throws Exception {
		Server a = server();
		saveIntervalMillis = 100;
		Server b = server();
		save(a, change("x", "1"));

		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (b.mainThread.isEmpty() && System.nanoTime() < deadline)
			Thread.onSpinWait();
		assertEquals(1, b.runTasks());
		assertEquals("1", b.get("x"));
	}

	// ---- failures and stopping ----

	@Test
	public void aFailedPollIsTriedAgain() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		sql("ALTER TABLE " + TABLE + "_log RENAME TO hidden");

		assertEquals(0, b.poll());
		assertTrue("a failed poll doesn't hold up draining", b.storage().mayDrain());
		sql("ALTER TABLE hidden RENAME TO " + TABLE + "_log");
		assertEquals(1, b.poll()); // a new connection, and the same entries
		assertEquals("1", b.get("x"));
	}

	@Test
	public void aServerThatFellBehindThePruneSaysSoOnce() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("missed", "1"));
		save(a, change("kept", "2"));
		// as if another server pruned the first save's entries before b read them
		sql("DELETE FROM " + TABLE + "_log WHERE name = 'missed'", "UPDATE " + TABLE + "_meta SET version = "
			+ query("SELECT version FROM " + TABLE + "_meta WHERE id = 0") + " WHERE id = 1");

		assertMentions(pollErrors(b), "Restart the server");
		b.runTasks();
		assertEquals("what's left is still reported", "2", b.get("kept"));
		save(a, change("later", "3"));
		assertEquals(List.of(), pollErrors(b));
	}

	@Test
	public void aServerThatKeptUpIsntWarnedAboutPrunedVersions() throws Exception {
		Server a = server();
		Server b = server();
		save(a, change("x", "1"));
		assertEquals(1, b.poll());
		sql("UPDATE " + TABLE + "_meta SET version = " + query("SELECT version FROM " + TABLE + "_meta WHERE id = 0")
			+ " + 1 WHERE id = 1"); // pruned below the next version: everything b has read already
		save(a, change("y", "2"));

		assertEquals(List.of(), pollErrors(b));
	}

	/**
	 * Polls, and returns the errors it printed.
	 */
	private static List<String> pollErrors(Server server) {
		try (RetainingLogHandler log = new RetainingLogHandler().start()) {
			server.storage().poll();
			return warnings(log);
		}
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
		assertTrue("nothing will apply reports any more, so draining isn't held up", storage.mayDrain());
	}

}
