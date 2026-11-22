package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.VariableMap;
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
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests loading from {@link SQLiteStorage} through a {@link VariableStorageManager}, with rows written to the file
 * directly over JDBC. Saving comes with {@code save}. Needs a server, since it logs through Skript
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

	// ---- creating the tables ----

	@Test
	public void loadingAnEmptyFileCreatesTheTables() throws SQLException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		assertEquals(0, storage(manager).loadedVersion());
		close(manager);

		assertEquals(0L, ((Number) query("SELECT COUNT(*) FROM " + TABLE)).longValue());
		assertEquals(0L, ((Number) query("SELECT version FROM " + TABLE + "_meta WHERE id = 0")).longValue());
		assertEquals(TABLE + "_version",
			query("SELECT name FROM sqlite_master WHERE type = 'index' AND name = '" + TABLE + "_version'"));
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

}
