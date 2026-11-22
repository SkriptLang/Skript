package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.VariableStorage;
import org.skriptlang.skript.variables.storage.VariableStorageManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import static org.junit.Assert.*;

/**
 * Tests {@link MySQLStorage}. The configuration tests run anywhere. The rest need a MySQL or MariaDB server, and are
 * skipped unless the environment variable {@code SKRIPT_TEST_MYSQL_HOST} names one, with
 * {@code SKRIPT_TEST_MYSQL_PORT} (default 3306), {@code SKRIPT_TEST_MYSQL_DATABASE}, {@code SKRIPT_TEST_MYSQL_USER}
 * and {@code SKRIPT_TEST_MYSQL_PASSWORD}. The user needs to be able to create and drop tables in the database.
 * They use their own table, {@value #TABLE}, and drop it before and after each test.
 * <p>
 * The logic shared with SQLite (clears, tombstones, pruning, backups) is tested more fully by
 * {@link SQLiteStorageTest}. These test what depends on MySQL: the schema, how names compare, the upsert,
 * streaming the load, and the limits on names and values.
 */
public class MySQLStorageTest {

	private static final String TEXT = "text";
	private static final String TABLE = "skript_test_variables";

	private static final @Nullable String HOST = System.getenv("SKRIPT_TEST_MYSQL_HOST");
	private static final String PORT = env("SKRIPT_TEST_MYSQL_PORT", "3306");
	private static final String DATABASE = env("SKRIPT_TEST_MYSQL_DATABASE", "skript");
	private static final String USER = env("SKRIPT_TEST_MYSQL_USER", "root");
	private static final String PASSWORD = env("SKRIPT_TEST_MYSQL_PASSWORD", "");

	private final List<VariableStorageManager> managers = new ArrayList<>();

	private static String env(String name, String fallback) {
		String value = System.getenv(name);
		return value == null ? fallback : value;
	}

	@Before
	public void setUp() throws SQLException {
		if (HOST != null)
			dropTables();
	}

	@After
	public void tearDown() throws SQLException {
		managers.forEach(this::close);
		if (HOST != null)
			dropTables();
	}

	// ---- helpers ----

	private static void needsServer() {
		Assume.assumeTrue("SKRIPT_TEST_MYSQL_HOST isn't set", HOST != null);
	}

	private static Connection open() throws SQLException {
		Properties properties = new Properties();
		properties.setProperty("user", USER);
		properties.setProperty("password", PASSWORD);
		properties.setProperty("characterEncoding", "UTF-8");
		return DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE, properties);
	}

	private static void dropTables() throws SQLException {
		sql("DROP TABLE IF EXISTS " + TABLE + ", " + TABLE + "_meta");
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
	private static @Nullable Object query(String sql, Object... parameters) throws SQLException {
		try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int i = 0; i < parameters.length; i++)
				statement.setObject(i + 1, parameters[i]);
			try (ResultSet result = statement.executeQuery()) {
				return result.next() ? result.getObject(1) : null;
			}
		}
	}

	private static long count(String where, Object... parameters) throws SQLException {
		return ((Number) query("SELECT COUNT(*) FROM " + TABLE + " WHERE " + where, parameters)).longValue();
	}

	/**
	 * @return A database section for the config, pointing at the test server unless the lines override it
	 */
	private static String database(String name, String... lines) {
		Map<String, String> options = new HashMap<>(Map.of(
			"type", "mysql", "host", HOST == null ? "127.0.0.1" : HOST, "port", PORT, "database", DATABASE,
			"user", USER, "password", PASSWORD, "table", TABLE, "backup interval", "0"));
		for (String line : lines) {
			String[] parts = line.split(": ", 2);
			if (parts.length == 1) {
				options.remove(parts[0]);
			} else {
				options.put(parts[0], parts[1]);
			}
		}
		StringBuilder section = new StringBuilder("\t" + name + ":\n");
		options.forEach((key, value) -> section.append("\t\t").append(key).append(": ").append(value).append("\n"));
		return section.toString();
	}

	private static SectionNode node(String databases) {
		String config = "databases:\n" + databases;
		try {
			return (SectionNode) new Config(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)),
				"test.sk", false, true, ":").getMainNode().get("databases");
		} catch (IOException e) {
			throw new UncheckedIOException(e); // can't happen when reading from memory
		}
	}

	private VariableStorageManager manager(VariableMap globals) {
		VariableStorageManager manager = new VariableStorageManager(globals,
			value -> value == null ? null : value(String.valueOf(value)),
			(type, data) -> type.equals(TEXT) ? new String(data, StandardCharsets.UTF_8) : null,
			Runnable::run);
		manager.register(null, MySQLStorage.class, MySQLStorage::new, "mysql", "mariadb");
		managers.add(manager);
		return manager;
	}

	/**
	 * Loads the databases into a new manager, and checks whether that worked.
	 */
	private VariableStorageManager loadExpecting(VariableMap globals, boolean successful, String databases) {
		VariableStorageManager manager = manager(globals);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertEquals(successful, manager.load(node(databases)));
		}
		return manager;
	}

	/**
	 * Loads the databases into a new manager, expecting it to fail, and returns the errors it printed.
	 */
	private List<String> loadErrors(String databases) {
		VariableStorageManager manager = manager(VariableMap.concurrent());
		try (RetainingLogHandler log = new RetainingLogHandler().start()) {
			assertFalse(manager.load(node(databases)));
			// getLog, unlike getErrors, marks the log as handled, so closing it doesn't warn
			return log.getLog().stream()
				.filter(entry -> entry.getLevel().intValue() >= Level.SEVERE.intValue())
				.map(LogEntry::getMessage)
				.toList();
		}
	}

	private static void assertMentions(List<String> errors, String part) {
		assertTrue("no error mentions '" + part + "': " + errors, errors.stream().anyMatch(error -> error.contains(part)));
	}

	private JdbcStorage loadedStorage() {
		return storage(loadExpecting(VariableMap.concurrent(), true, database("default")));
	}

	private static JdbcStorage storage(VariableStorageManager manager) {
		return (JdbcStorage) manager.storages().get(0);
	}

	private Map<String, Object> loaded() {
		VariableMap globals = VariableMap.concurrent();
		loadExpecting(globals, true, database("default"));
		return globals.getAll();
	}

	private void close(VariableStorageManager manager) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			manager.close();
		}
	}

	private void closeAll() {
		managers.forEach(this::close);
		managers.clear();
	}

	private static void save(JdbcStorage storage, List<String> clears, SerializedVariable... changes) throws IOException {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			storage.save(new VariableStorage.Batch(clears, List.of(changes)));
		}
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
	 * A row as it is in the table.
	 *
	 * @param text The value as text, or null for a tombstone
	 */
	private record Row(@Nullable String type, @Nullable String text, long version, String server) {

		boolean tombstone() {
			return type == null && text == null;
		}

	}

	private static @Nullable Row row(String name) throws SQLException {
		try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(
				"SELECT type, value, version, server FROM " + TABLE + " WHERE name = ?")) {
			statement.setString(1, name);
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next())
					return null;
				byte[] data = result.getBytes(2);
				Row row = new Row(result.getString(1), data == null ? null : new String(data, StandardCharsets.UTF_8),
					result.getLong(3), result.getString(4));
				assertFalse("the name matched more than one row", result.next());
				return row;
			}
		}
	}

	private static long counter() throws SQLException {
		return ((Number) query("SELECT version FROM " + TABLE + "_meta WHERE id = 0")).longValue();
	}

	// ---- configuring, no server needed ----

	@Test
	public void aMissingHostFailsConfiguring() {
		assertMentions(loadErrors(database("default", "host")), "'host'");
	}

	@Test
	public void aMissingDatabaseFailsConfiguring() {
		assertMentions(loadErrors(database("default", "database")), "'database'");
	}

	@Test
	public void aMissingUserFailsConfiguring() {
		assertMentions(loadErrors(database("default", "user")), "'user'");
	}

	@Test
	public void aPortOutOfRangeFailsConfiguring() {
		assertMentions(loadErrors(database("default", "port: 70000")), "between 1 and 65535");
		assertMentions(loadErrors(database("default", "port: 0")), "between 1 and 65535");
	}

	@Test
	public void aPortThatIsntANumberFailsConfiguring() {
		assertMentions(loadErrors(database("default", "port: lots")), "'port'");
	}

	@Test
	public void twoDatabasesCantUseTheSameTable() {
		// the same table, with the host and table written in another case. Nothing listens on port 1, so the first
		// fails to connect right away too, but only the second is refused for its table
		List<String> errors = loadErrors(database("first", "host: localhost", "port: 1")
			+ database("second", "host: LOCALHOST", "port: 1", "table: " + TABLE.toUpperCase()));
		assertMentions(errors, "already used by another database");
	}

	@Test
	public void theSameTableNameInAnotherDatabaseIsFine() {
		// nothing listens on port 1, so both fail to connect, but neither is refused for its table
		List<String> errors = loadErrors(database("first", "host: localhost", "port: 1")
			+ database("second", "host: localhost", "port: 1", "database: other"));
		assertTrue(errors.toString(), errors.stream().noneMatch(error -> error.contains("already used")));
	}

	// ---- the schema ----

	@Test
	public void loadingCreatesTheTables() throws SQLException {
		needsServer();
		loadedStorage();

		assertEquals(0L, count("1 = 1"));
		assertEquals(0L, counter());
	}

	@Test
	public void namesUseABinaryCollationThatDoesntPad() throws SQLException {
		needsServer();
		loadedStorage();

		Object collation = query("SELECT COLLATION_NAME FROM information_schema.COLUMNS "
			+ "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'name'", TABLE);
		assertTrue(String.valueOf(collation), Set.of("utf8mb4_0900_bin", "utf8mb4_nopad_bin").contains(collation));
		assertEquals("NO PAD", query("SELECT PAD_ATTRIBUTE FROM information_schema.COLLATIONS WHERE COLLATION_NAME = ?",
			collation));
	}

	@Test
	public void theTableHasAVersionIndex() throws SQLException {
		needsServer();
		loadedStorage();

		assertEquals("version", query("SELECT COLUMN_NAME FROM information_schema.STATISTICS "
			+ "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = 'version_index'", TABLE));
	}

	@Test
	public void loadingAgainKeepsTheTablesAndCounter() throws Exception {
		needsServer();
		save(loadedStorage(), List.of(), change("a", "1"));
		long version = counter();
		closeAll();

		assertEquals(Map.of("a", "1"), loaded());
		assertEquals(version, counter());
	}

	@Test
	public void aTableWithOtherColumnsFailsTheLoad() throws SQLException {
		needsServer();
		sql("CREATE TABLE " + TABLE + " (id INT PRIMARY KEY, data TEXT)");

		loadExpecting(VariableMap.concurrent(), false, database("default"));
	}

	// ---- saving and loading ----

	@Test
	public void namesAreComparedExactly() throws Exception {
		needsServer();
		save(loadedStorage(), List.of(), change("a", "plain"), change("a ", "trailing space"), change("A", "upper"),
			change("ä", "umlaut"), change("ä", "combining umlaut"));
		closeAll();

		assertEquals(Map.of("a", "plain", "a ", "trailing space", "A", "upper", "ä", "umlaut",
			"ä", "combining umlaut"), loaded());
	}

	@Test
	public void unicodeNamesAndValuesRoundTrip() throws Exception {
		needsServer();
		String name = "emoji::😀::中文";
		String text = "𝄞 music ✓";
		save(loadedStorage(), List.of(), change(name, text));
		closeAll();

		assertEquals(Map.of(name, text), loaded());
	}

	@Test
	public void bigValuesRoundTrip() throws Exception {
		needsServer();
		char[] chars = new char[200_000]; // over a plain BLOB's 64 KB
		Arrays.fill(chars, 'x');
		String text = new String(chars);
		save(loadedStorage(), List.of(), change("big", text));
		closeAll();

		assertEquals(Map.of("big", text), loaded());
	}

	@Test
	public void anOverwriteKeepsTheNewValueWithANewerVersion() throws Exception {
		needsServer();
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "old"));
		long first = row("a").version();
		save(storage, List.of(), change("a", "new"));

		Row row = row("a");
		assertEquals("new", row.text());
		assertEquals(TEXT, row.type());
		assertTrue(row.version() > first);
	}

	@Test
	public void aDeletionLeavesATombstoneWithTheServer() throws Exception {
		needsServer();
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "1"));
		save(storage, List.of(), deletion("a"));

		Row row = row("a");
		assertTrue(row.tombstone());
		assertEquals(36, row.server().length()); // a UUID
		closeAll();
		assertEquals(Map.of(), loaded());
	}

	@Test
	public void aClearOnlyTombstonesItsList() throws Exception {
		needsServer();
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "own value"), change("a::b", "1"), change("a::b::c", "2"),
			change("ab::x", "3"), change("a:x", "4"), change("A::b", "5"), change("a::😀", "6"));
		save(storage, List.of("a::"));

		assertTrue(row("a::b").tombstone());
		assertTrue(row("a::b::c").tombstone());
		assertTrue(row("a::😀").tombstone());
		assertEquals("own value", row("a").text());
		assertEquals("3", row("ab::x").text());
		assertEquals("4", row("a:x").text());
		assertEquals("5", row("A::b").text());
	}

	@Test
	public void underscoresAndPercentsInAClearedListArentWildcards() throws Exception {
		needsServer();
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("my_list::1", "1"), change("myXlist::1", "2"), change("50%::1", "3"),
			change("50x::1", "4"));
		save(storage, List.of("my_list::", "50%::"));

		assertTrue(row("my_list::1").tombstone());
		assertTrue(row("50%::1").tombstone());
		assertEquals("2", row("myXlist::1").text());
		assertEquals("4", row("50x::1").text());
	}

	@Test
	public void savingTheSameBatchTwiceIsHarmless() throws Exception {
		needsServer();
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a::1", "1"), change("b", "2"));
		List<String> clears = List.of("a::");
		SerializedVariable[] changes = {change("a::2", "two"), deletion("b"), change("c", "3")};
		save(storage, clears, changes);
		long version = counter();
		save(storage, clears, changes);

		assertEquals(version + 1, counter());
		assertEquals("two", row("a::2").text());
		assertEquals("3", row("c").text());
		closeAll();
		assertEquals(Map.of("a::2", "two", "c", "3"), loaded());
	}

	@Test
	public void manyRowsAreSavedInChunksAndStreamedBack() throws Exception {
		needsServer();
		int count = 5_500; // several chunks of 1000, and enough rows that streaming the load matters
		SerializedVariable[] changes = new SerializedVariable[count];
		Map<String, Object> expected = new HashMap<>();
		for (int i = 0; i < count; i++) {
			changes[i] = change("many::" + i, String.valueOf(i));
			expected.put("many::" + i, String.valueOf(i));
		}
		save(loadedStorage(), List.of(), changes);

		assertEquals((long) count, count("version = ?", counter()));
		closeAll();
		assertEquals(expected, loaded());
	}

	// ---- limits ----

	@Test
	public void aNameOfTheLongestLengthIsSaved() throws Exception {
		needsServer();
		String longest = "中".repeat(768); // 768 characters, 2304 bytes
		String tooLong = "x".repeat(769);
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change(longest, "1"), change(tooLong, "2"));
		save(storage, List.of(), change("later", "3")); // saving still works afterwards

		assertEquals("1", row(longest).text());
		assertEquals(0L, count("name = ?", tooLong));
		assertEquals("3", row("later").text());
	}

	@Test
	public void theValueLimitFollowsTheServersPacketSize() throws Exception {
		needsServer();
		long packet = ((Number) query("SELECT @@max_allowed_packet")).longValue();
		JdbcStorage storage = loadedStorage();
		long limit = storage.maxValueLength();
		assertEquals((packet - 16 * 1024) / 2, limit);
		Assume.assumeTrue("the server's max_allowed_packet is too big to test the limit quickly", limit <= 40 << 20);

		// a value right at the limit fits in one packet, so the headroom is enough
		byte[] largest = new byte[(int) limit];
		Arrays.fill(largest, (byte) 0xFF);
		save(storage, List.of(), new SerializedVariable("largest", new SerializedVariable.Value(TEXT, largest)),
			change("small", "1"));
		assertEquals(limit, ((Number) query("SELECT LENGTH(value) FROM " + TABLE + " WHERE name = 'largest'")).longValue());

		// one more byte is skipped, without failing the rest of the batch or later saves
		byte[] tooBig = new byte[(int) limit + 1];
		save(storage, List.of(), new SerializedVariable("largest", new SerializedVariable.Value(TEXT, tooBig)),
			change("small", "2"));
		save(storage, List.of(), change("later", "3"));
		assertEquals("the last value that fit is kept", limit,
			((Number) query("SELECT LENGTH(value) FROM " + TABLE + " WHERE name = 'largest'")).longValue());
		assertEquals("2", row("small").text());
		assertEquals("3", row("later").text());
	}

	@Test
	public void variablesSetThroughTheManagerLoadBack() {
		needsServer();
		VariableStorageManager manager = loadExpecting(VariableMap.concurrent(), true, database("default"));
		manager.set("a", "1");
		manager.set("list::1", "one");
		manager.set("list::2", "two");
		manager.set("list::2", null);
		manager.set("gone::1", "x");
		manager.set("gone::*", null);
		closeAll(); // saves what's left

		assertEquals(Map.of("a", "1", "list::1", "one"), loaded());
	}

	@Test
	public void theMariadbAliasWorks() throws SQLException {
		needsServer();
		loadExpecting(VariableMap.concurrent(), true, database("default", "type: mariadb"));

		assertEquals(0L, count("1 = 1"));
	}

}
