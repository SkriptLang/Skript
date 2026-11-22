package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.VariableStorage;
import org.skriptlang.skript.variables.storage.VariableStorageManager;
import org.skriptlang.skript.variables.storage.flatfile.FlatFileStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests loading from {@link SQLiteStorage} through a {@link VariableStorageManager}, with rows written to the file
 * directly over JDBC, saving to it, and backing it up. Needs a server, since it logs through Skript
 * and the SQLite driver comes with Paper.
 */
public class SQLiteStorageTest {

	private static final String TEXT = "text";
	private static final String TABLE = JdbcStorage.DEFAULT_TABLE;

	private Path folder;
	private Path file;
	private final List<VariableStorageManager> managers = new ArrayList<>();

	/**
	 * An SQLite storage that says every table is the same one, so the same-table check can be tested.
	 * The real key includes the file, and the base class already refuses two storages on one file.
	 */
	static final class SameTableStorage extends SQLiteStorage {

		SameTableStorage(SkriptAddon registeringAddon, String type) {
			super(registeringAddon, type);
		}

		@Override
		protected String tableKey(String table) {
			return "the same table";
		}

	}

	/**
	 * An SQLite storage that can only save names up to 10 characters, like MySQL can only save up to 768.
	 */
	static final class ShortNameStorage extends SQLiteStorage {

		ShortNameStorage(SkriptAddon registeringAddon, String type) {
			super(registeringAddon, type);
		}

		@Override
		protected int maxNameLength() {
			return 10;
		}

	}

	/**
	 * An SQLite storage that can only save values up to 10 bytes, like MySQL can only save values that fit in a packet.
	 */
	static final class SmallValueStorage extends SQLiteStorage {

		SmallValueStorage(SkriptAddon registeringAddon, String type) {
			super(registeringAddon, type);
		}

		@Override
		protected long maxValueLength() {
			return 10;
		}

	}

	/**
	 * An SQLite storage that acts like it can be monitored, so the tombstones and pruning that databases shared between
	 * servers need can be tested without one.
	 */
	static final class MonitoredStorage extends SQLiteStorage {

		MonitoredStorage(SkriptAddon registeringAddon, String type) {
			super(registeringAddon, type);
		}

		@Override
		protected boolean supportsMonitoring() {
			return true;
		}

	}

	@Before
	public void setUp() throws IOException {
		folder = Files.createTempDirectory("skript-sqlite-test");
		file = folder.resolve("variables.sqlite");
	}

	@After
	public void tearDown() throws IOException {
		managers.forEach(this::close);
		try (Stream<Path> paths = Files.walk(folder)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
				Files.delete(path);
		}
	}

	// ---- helpers ----

	/**
	 * Loads the given databases into a new manager, with values stored as UTF-8 text.
	 *
	 * @param globals    Where the variables go
	 * @param successful Whether loading should work
	 * @param databases  The sections inside {@code databases:}, already indented by one tab
	 */
	private VariableStorageManager loadExpecting(VariableMap globals, boolean successful, String databases) {
		VariableStorageManager manager = new VariableStorageManager(globals,
			value -> value == null ? null : value(String.valueOf(value)),
			(type, data) -> type.equals(TEXT) ? new String(data, StandardCharsets.UTF_8) : null,
			Runnable::run);
		// a subclass of a registered type can't be registered too, so only register the one the config uses
		if (databases.contains("type: sametable")) {
			manager.register(null, SameTableStorage.class, SameTableStorage::new, "sametable");
		} else if (databases.contains("type: shortnames")) {
			manager.register(null, ShortNameStorage.class, ShortNameStorage::new, "shortnames");
		} else if (databases.contains("type: smallvalues")) {
			manager.register(null, SmallValueStorage.class, SmallValueStorage::new, "smallvalues");
		} else if (databases.contains("type: monitored")) {
			manager.register(null, MonitoredStorage.class, MonitoredStorage::new, "monitored");
		} else if (databases.contains("type: csv")) {
			manager.register(null, FlatFileStorage.class, FlatFileStorage::new, "csv");
		} else {
			manager.register(null, SQLiteStorage.class, SQLiteStorage::new, "sqlite");
		}
		String config = "databases:\n" + databases;
		SectionNode node;
		try {
			node = (SectionNode) new Config(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)),
				"test.sk", false, true, ":").getMainNode().get("databases");
		} catch (IOException e) {
			throw new UncheckedIOException(e); // can't happen when reading from memory
		}
		managers.add(manager);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertEquals(successful, manager.load(node));
		}
		return manager;
	}

	/**
	 * Loads {@link #file} with the default table into a new manager, and checks it worked.
	 */
	private VariableStorageManager load(VariableMap globals) {
		return loadExpecting(globals, true, database("default", "sqlite", file));
	}

	/**
	 * @return A database section for the config, with any extra lines after the file
	 */
	private static String database(String name, String type, Path file, String... extra) {
		StringBuilder section = new StringBuilder("\t" + name + ":\n\t\ttype: " + type
			+ "\n\t\tfile: " + file.toString().replace('\\', '/') + "\n");
		for (String line : extra)
			section.append("\t\t").append(line).append("\n");
		return section.toString();
	}

	private void close(VariableStorageManager manager) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			manager.close();
		}
	}

	/**
	 * Loads the file once and closes it again, so the tables exist and rows can be put in directly.
	 */
	private void createTables() {
		close(load(VariableMap.concurrent()));
	}

	/**
	 * Loads the file into a new manager, and returns what it holds.
	 */
	private Map<String, Object> loaded() {
		VariableMap globals = VariableMap.concurrent();
		load(globals);
		return globals.getAll();
	}

	private static SerializedVariable.Value value(String text) {
		return new SerializedVariable.Value(TEXT, text.getBytes(StandardCharsets.UTF_8));
	}

	private Connection open() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
	}

	/**
	 * Runs statements on the file directly.
	 */
	private void sql(String... statements) throws SQLException {
		try (Connection connection = open(); Statement statement = connection.createStatement()) {
			for (String sql : statements)
				statement.execute(sql);
		}
	}

	/**
	 * Puts a row into the table directly, as if a save wrote it.
	 *
	 * @param type The type, or null for a tombstone or a broken row
	 * @param data The value, or null for a tombstone
	 */
	private void insert(String name, String type, byte[] data) throws SQLException {
		try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO " + TABLE + " (name, type, value, version, server) VALUES (?, ?, ?, 1, 'test')")) {
			statement.setString(1, name);
			statement.setString(2, type);
			statement.setBytes(3, data);
			statement.executeUpdate();
		}
	}

	private void insert(String name, String text) throws SQLException {
		insert(name, TEXT, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * @return The first column of the first row the query gives, or null if it gives none
	 */
	private Object query(String sql) throws SQLException {
		try (Connection connection = open(); Statement statement = connection.createStatement();
			 ResultSet result = statement.executeQuery(sql)) {
			return result.next() ? result.getObject(1) : null;
		}
	}

	private List<String> names() throws SQLException {
		List<String> names = new ArrayList<>();
		try (Connection connection = open(); Statement statement = connection.createStatement();
			 ResultSet result = statement.executeQuery("SELECT name FROM " + TABLE + " ORDER BY name")) {
			while (result.next())
				names.add(result.getString(1));
		}
		return names;
	}

	private static JdbcStorage storage(VariableStorageManager manager) {
		return (JdbcStorage) manager.storages().get(0);
	}

	/**
	 * Loads the file and returns its storage, for saving batches to it directly.
	 */
	private JdbcStorage loadedStorage() {
		return storage(load(VariableMap.concurrent()));
	}

	/**
	 * Loads the file as a storage that keeps tombstones, and returns it for saving batches to it directly.
	 */
	private JdbcStorage loadedMonitoredStorage() {
		return storage(loadExpecting(VariableMap.concurrent(), true, database("default", "monitored", file)));
	}

	/**
	 * @return The name of the version index, if the table has one
	 */
	private @Nullable Object versionIndex() throws SQLException {
		return query("SELECT name FROM sqlite_master WHERE type = 'index' AND name = '" + TABLE + "_version'");
	}

	/**
	 * Saves a batch straight to the storage, like its saving thread does.
	 */
	private static void save(JdbcStorage storage, List<String> clears, SerializedVariable... changes) throws IOException {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			storage.save(new VariableStorage.Batch(clears, List.of(changes)));
		}
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

	/**
	 * @return The row with the given name, or null if there is none
	 */
	private @Nullable Row row(String name) throws SQLException {
		try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(
				"SELECT type, value, version, server FROM " + TABLE + " WHERE name = ?")) {
			statement.setString(1, name);
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next())
					return null;
				byte[] data = result.getBytes(2);
				return new Row(result.getString(1), data == null ? null : new String(data, StandardCharsets.UTF_8),
					result.getLong(3), result.getString(4));
			}
		}
	}

	/**
	 * Writes a backup of the storage, like its saving thread does, and returns it as text.
	 */
	private static String backup(JdbcStorage storage) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		storage.writeBackup(out);
		return out.toString(StandardCharsets.UTF_8);
	}

	/**
	 * @return The rows of a CSV file's text, without its header and blank lines
	 */
	private static List<String> csvRows(String text) {
		return text.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).map(String::strip).toList();
	}

	private static String csvRow(String name, String text) {
		StringBuilder row = new StringBuilder();
		CsvFormat.appendRow(row, name, value(text));
		return row.toString().strip();
	}

	private long counter() throws SQLException {
		return ((Number) query("SELECT version FROM " + TABLE + "_meta WHERE id = 0")).longValue();
	}

	/**
	 * Lets the storage's pruning think an hour has passed since it saw the given version,
	 * and that it last pruned long enough ago to prune again. Reaches into its private fields,
	 * since it times pruning with {@link System#nanoTime()}.
	 */
	private static void pretendAnHourPassedSince(JdbcStorage storage, long version) throws ReflectiveOperationException {
		long now = System.nanoTime();
		Class<?> sampleClass = Class.forName(JdbcStorage.class.getName() + "$Sample");
		Constructor<?> sample = sampleClass.getDeclaredConstructor(long.class, long.class);
		sample.setAccessible(true);
		Field samplesField = JdbcStorage.class.getDeclaredField("samples");
		samplesField.setAccessible(true);
		@SuppressWarnings("unchecked")
		ArrayDeque<Object> samples = (ArrayDeque<Object>) samplesField.get(storage);
		samples.clear();
		samples.add(sample.newInstance(now - TimeUnit.HOURS.toNanos(2), version));
		Field lastPrune = JdbcStorage.class.getDeclaredField("lastPruneNanos");
		lastPrune.setAccessible(true);
		lastPrune.setLong(storage, now - TimeUnit.HOURS.toNanos(1));
	}

	// ---- creating the tables ----

	@Test
	public void loadingAnEmptyFileCreatesTheTables() throws SQLException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		assertEquals(0, storage(manager).loadedVersion());
		close(manager);

		assertEquals(0L, ((Number) query("SELECT COUNT(*) FROM " + TABLE)).longValue());
		assertEquals(0L, ((Number) query("SELECT version FROM " + TABLE + "_meta WHERE id = 0")).longValue());
		assertNull("only polling needs the version index", versionIndex());
	}

	@Test
	public void aMonitoredTableGetsAVersionIndex() throws SQLException {
		close(loadExpecting(VariableMap.concurrent(), true, database("default", "monitored", file)));
		assertEquals(TABLE + "_version", versionIndex());
	}

	@Test
	public void anOldVersionIndexIsDropped() throws SQLException {
		close(loadExpecting(VariableMap.concurrent(), true, database("default", "monitored", file)));
		createTables();
		assertNull(versionIndex());
	}

	@Test
	public void walModeIsOn() throws SQLException {
		createTables(); // WAL is kept in the file, so a new connection sees it
		assertEquals("wal", query("PRAGMA journal_mode"));
	}

	@Test
	public void loadingReadsTheVersionCounter() throws SQLException {
		createTables();
		sql("UPDATE " + TABLE + "_meta SET version = 7 WHERE id = 0");
		assertEquals(7, storage(load(VariableMap.concurrent())).loadedVersion());
	}

	@Test
	public void loadingAgainKeepsTheTables() throws SQLException {
		createTables();
		insert("a", "1");
		createTables(); // CREATE ... IF NOT EXISTS, and the counter row isn't inserted twice
		assertEquals(List.of("a"), names());
		assertEquals(1L, ((Number) query("SELECT COUNT(*) FROM " + TABLE + "_meta")).longValue());
	}

	// ---- loading rows ----

	@Test
	public void rowsAreLoaded() throws SQLException {
		createTables();
		String big = "x".repeat(100_000);
		insert("a", "1");
		insert("list::1", "one");
		insert("list::sub::2", "two");
		insert("unicode::ä::名前", "wert");
		insert("time::11:04 am", "narrow no-break space");
		insert("big", big);

		assertEquals(Map.of("a", "1", "list::1", "one", "list::sub::2", "two", "unicode::ä::名前", "wert",
			"time::11:04 am", "narrow no-break space", "big", big), loaded());
	}

	@Test
	public void tombstonesArentLoaded() throws SQLException {
		createTables();
		insert("a", "1");
		insert("deleted", null, null);
		assertEquals(Map.of("a", "1"), loaded());
	}

	@Test
	public void badRowsAreSkippedAndKept() throws SQLException {
		createTables();
		insert("a", "1");
		insert("list::*", "a list can't hold a value");
		insert("no type", null, "a value without a type".getBytes(StandardCharsets.UTF_8));

		assertEquals(Map.of("a", "1"), loaded());
		// nothing is rewritten, so a later fix can still read them
		assertEquals(List.of("a", "list::*", "no type"), names());
	}

	@Test
	public void aTableWithOtherColumnsFailsTheLoad() throws SQLException {
		sql("CREATE TABLE " + TABLE + " (x INTEGER)", "INSERT INTO " + TABLE + " VALUES (1)");
		loadExpecting(VariableMap.concurrent(), false, database("default", "sqlite", file));
		// the foreign table is left alone
		assertEquals(1L, ((Number) query("SELECT x FROM " + TABLE)).longValue());
	}

	@Test
	public void aTableWithMissingColumnsFailsTheLoad() throws SQLException {
		// the index and counter can be made, but the column check fails
		sql("CREATE TABLE " + TABLE + " (name TEXT PRIMARY KEY, version INTEGER)");
		loadExpecting(VariableMap.concurrent(), false, database("default", "sqlite", file));
	}

	// ---- configuring ----

	@Test
	public void anInvalidTableNameFailsConfiguring() {
		loadExpecting(VariableMap.concurrent(), false, database("default", "sqlite", file, "table: 1abc"));
		loadExpecting(VariableMap.concurrent(), false, database("default", "sqlite", file, "table: no-dashes"));
		loadExpecting(VariableMap.concurrent(), false, database("default", "sqlite", file, "table: " + "a".repeat(59)));
	}

	@Test
	public void aLongTableNameWorks() throws SQLException {
		String table = "a".repeat(58);
		close(loadExpecting(VariableMap.concurrent(), true, database("default", "sqlite", file, "table: " + table)));
		assertEquals(0L, ((Number) query("SELECT version FROM " + table + "_meta WHERE id = 0")).longValue());
	}

	@Test
	public void twoDatabasesCantUseTheSameTable() {
		VariableStorageManager manager = loadExpecting(VariableMap.concurrent(), false,
			database("first", "sametable", file) + database("second", "sametable", folder.resolve("other.sqlite")));
		assertEquals(1, manager.storages().size());
	}

	@Test
	public void aTableIsFreedWhenItsStorageCloses() {
		close(loadExpecting(VariableMap.concurrent(), true, database("first", "sametable", file)));
		loadExpecting(VariableMap.concurrent(), true, database("second", "sametable", folder.resolve("other.sqlite")));
	}

	@Test
	public void aTableIsFreedWhenLoadingFails() throws SQLException {
		sql("CREATE TABLE " + TABLE + " (x INTEGER)");
		loadExpecting(VariableMap.concurrent(), false, database("first", "sametable", file));
		loadExpecting(VariableMap.concurrent(), true, database("second", "sametable", folder.resolve("other.sqlite")));
	}

	@Test
	public void differentFilesCanUseTheSameTableName() {
		loadExpecting(VariableMap.concurrent(), true,
			database("first", "sqlite", file) + database("second", "sqlite", folder.resolve("other.sqlite")));
	}

	// ---- saving ----

	@Test
	public void variablesSetThroughTheManagerLoadBack() {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("list::1", "one");
		manager.set("list::2", "two");
		manager.set("list::2", null);
		manager.set("gone::1", "x");
		manager.set("gone::*", null);
		close(manager); // saves what's left

		assertEquals(Map.of("a", "1", "list::1", "one"), loaded());
	}

	@Test
	public void anOverwriteKeepsTheNewValueWithANewerVersion() throws Exception {
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
	public void aDeletionRemovesTheRow() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "1"), change("b", "2"));
		save(storage, List.of(), deletion("a"));

		assertNull(row("a"));
		assertEquals("2", row("b").text());
	}

	@Test
	public void deletionsAndSetsInOneBatchBothApply() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "1"), change("b", "2"));
		save(storage, List.of(), deletion("a"), change("b", "new"), change("c", "3"), deletion("never set"));

		assertEquals(List.of("b", "c"), names());
		assertEquals("new", row("b").text());
	}

	@Test
	public void aDeletionLeavesATombstoneIfMonitored() throws Exception {
		JdbcStorage storage = loadedMonitoredStorage();
		save(storage, List.of(), change("a", "1"));
		save(storage, List.of(), deletion("a"));

		assertTrue(row("a").tombstone());
		close(managers.get(0));
		assertEquals(Map.of(), loaded());
	}

	@Test
	public void aClearOnlyRemovesItsList() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "own value"), change("a::b", "1"), change("a::b::c", "2"),
			change("ab::x", "3"), change("a:x", "4"));
		save(storage, List.of("a::"));

		assertNull(row("a::b"));
		assertNull(row("a::b::c"));
		assertEquals("own value", row("a").text());
		assertEquals("3", row("ab::x").text());
		assertEquals("4", row("a:x").text());
	}

	@Test
	public void underscoresInAClearedListArentWildcards() throws Exception {
		// LIKE 'my_list::%' would match myXlist too, since _ matches any character there
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("my_list::1", "1"), change("myXlist::1", "2"), change("MY_LIST::1", "3"));
		save(storage, List.of("my_list::"));

		assertNull(row("my_list::1"));
		assertEquals("2", row("myXlist::1").text());
		assertEquals("3", row("MY_LIST::1").text()); // and names are compared exactly, not ignoring case
	}

	@Test
	public void aSetAfterAClearInTheSameBatchSurvives() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a::1", "1"), change("a::2", "2"));
		save(storage, List.of("a::"), change("a::2", "new"));

		assertNull(row("a::1"));
		assertEquals("new", row("a::2").text());
	}

	@Test
	public void clearsApplyInOrder() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a::b::1", "1"), change("a::c", "2"));
		save(storage, List.of("a::b::", "a::"));

		assertNull(row("a::b::1"));
		assertNull(row("a::c"));
	}

	@Test
	public void aClearOnlyTombstonesItsListIfMonitored() throws Exception {
		JdbcStorage storage = loadedMonitoredStorage();
		save(storage, List.of(), change("a", "own value"), change("a::b", "1"), change("ab::x", "2"));
		save(storage, List.of("a::"));

		assertTrue(row("a::b").tombstone());
		assertEquals("own value", row("a").text());
		assertEquals("2", row("ab::x").text());
	}

	@Test
	public void aClearDoesntRestampTombstones() throws Exception {
		JdbcStorage storage = loadedMonitoredStorage();
		save(storage, List.of(), change("x::1", "1"));
		save(storage, List.of(), deletion("x::1"));
		long deleted = row("x::1").version();
		save(storage, List.of("x::"));

		assertEquals(deleted, row("x::1").version());
	}

	@Test
	public void savingTheSameBatchTwiceIsHarmless() throws Exception {
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
		assertEquals(List.of("a::2", "c"), names());
	}

	@Test
	public void eachSaveBumpsTheCounterAndStampsItsRows() throws Exception {
		JdbcStorage storage = loadedStorage();
		long before = counter();
		save(storage, List.of(), change("a", "1"));
		save(storage, List.of(), change("b", "2"));
		save(storage, List.of("c::"));

		assertEquals(before + 3, counter());
		assertEquals(before + 1, row("a").version());
		assertEquals(before + 2, row("b").version());
		assertEquals("nothing else reads the server, so it isn't saved", "", row("a").server());
	}

	@Test
	public void rowsGetTheServerIfMonitored() throws Exception {
		JdbcStorage storage = loadedMonitoredStorage();
		save(storage, List.of(), change("a", "1"));
		save(storage, List.of(), change("b", "2"), deletion("a"));

		Row b = row("b");
		assertEquals(36, b.server().length()); // a UUID
		assertEquals(b.server(), row("a").server()); // the same storage, tombstones included
	}

	@Test
	public void aBigBatchIsSavedInChunks() throws Exception {
		JdbcStorage storage = loadedStorage();
		int count = 2_500; // over two chunks of 1000
		SerializedVariable[] changes = new SerializedVariable[count];
		Map<String, Object> expected = new HashMap<>();
		for (int i = 0; i < count; i++) {
			changes[i] = change("big::" + i, String.valueOf(i));
			expected.put("big::" + i, String.valueOf(i));
		}
		save(storage, List.of(), changes);
		long version = counter();

		assertEquals((long) count, ((Number) query("SELECT COUNT(*) FROM " + TABLE + " WHERE version = " + version)).longValue());
		close(managers.get(0));
		assertEquals(expected, loaded());
	}

	@Test
	public void aNameThatCantBeSavedIsSkipped() throws Exception {
		VariableStorageManager manager = loadExpecting(VariableMap.concurrent(), true, database("default", "shortnames", file));
		JdbcStorage storage = storage(manager);
		save(storage, List.of(), change("short", "1"), change("far too long a name", "2"));
		save(storage, List.of(), change("later", "3")); // saving still works afterwards

		assertEquals("1", row("short").text());
		assertNull(row("far too long a name"));
		assertEquals("3", row("later").text());
	}

	@Test
	public void aValueThatCantBeSavedIsSkipped() throws Exception {
		VariableStorageManager manager = loadExpecting(VariableMap.concurrent(), true, database("default", "smallvalues", file));
		JdbcStorage storage = storage(manager);
		save(storage, List.of(), change("small", "0123456789"), change("big", "0123456789a"));
		save(storage, List.of(), change("later", "3")); // saving still works afterwards

		assertEquals("0123456789", row("small").text()); // exactly the limit is fine
		assertNull(row("big"));
		assertEquals("3", row("later").text());
	}

	@Test
	public void aValueThatGrowsTooBigKeepsItsLastSavedValue() throws Exception {
		VariableStorageManager manager = loadExpecting(VariableMap.concurrent(), true, database("default", "smallvalues", file));
		JdbcStorage storage = storage(manager);
		save(storage, List.of(), change("a", "small"));
		save(storage, List.of(), change("a", "far too big a value"));

		assertEquals("small", row("a").text());
	}

	@Test
	public void aDeletionIsntLimitedByValueLength() throws Exception {
		VariableStorageManager manager = loadExpecting(VariableMap.concurrent(), true, database("default", "smallvalues", file));
		JdbcStorage storage = storage(manager);
		save(storage, List.of(), change("a", "1"));
		save(storage, List.of(), deletion("a"));

		assertNull(row("a"));
	}

	@Test
	public void aFailedSaveSavesNothingAndCanBeTriedAgain() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "1"));
		long version = counter();
		sql("ALTER TABLE " + TABLE + " RENAME TO hidden"); // the storage can't find its table any more

		assertThrows(IOException.class, () -> save(storage, List.of(), change("a", "2")));
		assertEquals(version, counter()); // the counter's bump was rolled back with the rest

		sql("ALTER TABLE hidden RENAME TO " + TABLE);
		save(storage, List.of(), change("a", "2")); // a new connection, as after any failure
		assertEquals("2", row("a").text());
		assertEquals(version + 1, counter());
	}

	@Test
	public void pruningRemovesOnlyOldTombstones() throws Exception {
		JdbcStorage storage = loadedMonitoredStorage();
		save(storage, List.of(), change("old", "1"), change("recent", "2"), change("kept", "3"));
		save(storage, List.of(), deletion("old"));
		long oldDeletion = counter();
		save(storage, List.of(), deletion("recent"));

		// everything before the version after the old deletion is over an hour old
		pretendAnHourPassedSince(storage, oldDeletion + 1);
		save(storage, List.of(), change("trigger", "4"));

		assertNull(row("old"));
		assertTrue(row("recent").tombstone());
		assertEquals("3", row("kept").text()); // values are never pruned, however old
	}

	// ---- backups ----

	@Test
	public void aBackupHoldsOnlyTheVariablesALoadWouldLoad() throws Exception {
		createTables();
		insert("a", "1");
		insert("list::1", "one");
		insert("deleted", null, null); // a tombstone
		insert("broken", null, "x".getBytes(StandardCharsets.UTF_8)); // a value without a type
		insert("list::*", "a list can't hold a value");
		JdbcStorage storage = loadedStorage();

		String text = backup(storage);
		assertTrue(text.contains(CsvFormat.VERSION_HEADER));
		assertFalse("a backup is read whole", text.contains(CsvFormat.COMPACTED_HEADER));
		assertEquals(List.of(csvRow("a", "1"), csvRow("list::1", "one")),
			csvRows(text).stream().sorted().toList());
		assertEquals("the broken rows are still in the table", "x",
			new String((byte[]) query("SELECT value FROM " + TABLE + " WHERE name = 'broken'"), StandardCharsets.UTF_8));
	}

	@Test
	public void aBackupOfAnEmptyTableIsJustTheHeader() throws IOException {
		String text = backup(loadedStorage());
		assertTrue(text.contains(CsvFormat.VERSION_HEADER));
		assertEquals(List.of(), csvRows(text));
	}

	@Test
	public void aBackupLoadsAsACsvDatabase() throws IOException {
		JdbcStorage storage = loadedStorage();
		String big = "x".repeat(100_000); // over one chunk of the writer on its own
		save(storage, List.of(), change("a", "1"), change("list::1", "one"), change("list::2", "two"),
			change("gone", "x"), change("unicode::ä::名前", "wert"), change("big", big));
		save(storage, List.of(), deletion("gone"), deletion("list::2"));

		Path backup = folder.resolve("backup.csv");
		Files.writeString(backup, backup(storage));
		VariableMap globals = VariableMap.concurrent();
		loadExpecting(globals, true, database("restored", "csv", backup));
		assertEquals(Map.of("a", "1", "list::1", "one", "unicode::ä::名前", "wert", "big", big), globals.getAll());
	}

	@Test
	public void aBackupEndsItsReadTransaction() throws Exception {
		JdbcStorage storage = loadedStorage();
		save(storage, List.of(), change("a", "1"));
		backup(storage);

		// a read transaction left open would keep the WAL in use, so it couldn't be emptied
		assertEquals(0, ((Number) query("PRAGMA wal_checkpoint(TRUNCATE)")).intValue()); // 0: not blocked
		assertEquals(0, Files.size(folder.resolve(file.getFileName() + "-wal")));
		save(storage, List.of(), change("b", "2")); // and the storage still saves after
		assertEquals("2", row("b").text());
	}

}
