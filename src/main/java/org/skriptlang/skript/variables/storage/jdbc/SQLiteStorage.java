package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.config.SectionNode;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

/**
 * Saves variables to an SQLite database: a single file on this server, plus the {@code -wal} and {@code -shm} files
 * SQLite keeps next to it. The driver comes with Paper, so nothing has to be downloaded.
 * <p>
 * Each storage needs its own file. SQLite only lets one connection write to a file at a time, so two storages
 * sharing one would just take turns saving, and a damaged file would take both with it.
 * Since no other server can use the file either, nothing can monitor it, so deleted rows are removed right away
 * instead of being kept as tombstones, and there's no version index to keep up to date.
 * <p>
 * We turn on WAL mode, so saves append to a log instead of rewriting the file in place, and readers never block the
 * writer. With {@code synchronous=NORMAL}, a save survives the server process crashing, though maybe not a power cut,
 * which is the same promise the CSV storage makes. The busy timeout makes us wait a bit for anything else that has the
 * file open, like a database browser, instead of failing the save right away.
 */
public class SQLiteStorage extends JdbcStorage {
	/**
	 * @param registeringAddon the addon that registered this storage's type
	 * @param type             the type name the config picked this storage with, such as {@code sqlite}
	 */
	public SQLiteStorage(SkriptAddon registeringAddon, String type) {
		super(registeringAddon, type);
	}

	@Override
	protected boolean configureConnection(SectionNode node) {
		return true;
	}

	@Override
	protected Connection connect() throws SQLException {
		File file = getStorageFile();
		assert file != null;
		return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
	}

	@Override
	protected void setUp(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("PRAGMA journal_mode=WAL");
			statement.execute("PRAGMA synchronous=NORMAL");
			statement.execute("PRAGMA busy_timeout=5000");
		}
	}

	@Override
	protected List<String> schemaSql(String table, String metaTable) {
		return List.of(
			// make the main table (name, type, value, version, server)
			"CREATE TABLE IF NOT EXISTS " + table + " (name TEXT NOT NULL PRIMARY KEY, type TEXT, value BLOB, "
				+ "version INTEGER NOT NULL, server TEXT NOT NULL) WITHOUT ROWID",
			// the version index is only for polling, which needs monitoring. Without it, every write would update it for nothing
			monitorable()
				? "CREATE INDEX IF NOT EXISTS " + table + "_version ON " + table + " (version)"
				: "DROP INDEX IF EXISTS " + table + "_version",
			// make the meta table (overall version tracker, basically), with initial values 0,0
			"CREATE TABLE IF NOT EXISTS " + metaTable + " (id INTEGER NOT NULL PRIMARY KEY, version INTEGER NOT NULL)",
			"INSERT OR IGNORE INTO " + metaTable + " (id, version) VALUES (0, 0)");
	}

	@Override
	protected String upsertSql(String table, int rows) {
		return "INSERT INTO " + table + " (name, type, value, version, server) VALUES " + upsertValues(rows) + " "
			+ "ON CONFLICT(name) DO UPDATE SET "
			+ "type = excluded.type, value = excluded.value, version = excluded.version, server = excluded.server";
	}

	@Override
	protected String tableKey(String table) {
		File file = getStorageFile();
		assert file != null;
		return file.getPath() + "#" + table.toLowerCase(Locale.ROOT);
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	/**
	 * @return False, since only this server can use the file
	 */
	@Override
	protected boolean supportsMonitoring() {
		return false;
	}
}
