package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.storage.LoadListener;
import org.skriptlang.skript.variables.storage.LoadReport;
import org.skriptlang.skript.variables.storage.VariableStorage;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Saves variables to a table in an SQL database, through JDBC.
 * <p>
 * Each variable is one row: its name, the codename of its type, and its serialized value. Next to it is a second table,
 * {@code <table>_meta}, with a single row holding a version counter. Every save is one transaction that bumps the
 * counter and stamps each row it writes with the new version and this server's id. Deleting a variable or clearing
 * a list doesn't remove its rows. Instead, they become "tombstones", rows without a type or value, with the new
 * version, and get pruned once they're old enough. That way, anything reading the table can ask what changed since
 * some version.
 * <p>
 * We only keep one connection per storage, since only one thread uses it at a time.
 * It's refreshed after each error encountered.
 * <p>
 * This class does everything that's the same for every database. Subclasses only fill in what differs,
 * like how to connect, the exact SQL for creating the tables, and how to tell whether two storages use the same table.
 * <p>
 * Two storages can't use the same table of the same database, since they'd load and clear each other's rows.
 * The table names passed to the SQL hooks are checked to be plain identifiers (letters, digits and underscores),
 * so they can be put into SQL text directly. Never put any other config value or variable name into SQL text;
 * bind it with a {@code ?} in a prepared statement instead.
 */
public abstract class JdbcStorage extends VariableStorage {

	public static final String DEFAULT_TABLE = "skript_variables";

	// 58 leaves room for "_meta" within MySQL's 64 limit
	private static final Pattern TABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,57}");
	private static final long IDLE_CHECK_NANOS = TimeUnit.SECONDS.toNanos(30);

	private final String serverId = UUID.randomUUID().toString(); // an id to show what server wrote this line. Customizable in future?

	// the tables used by configured storages, as keys from tableKey, so two storages can't save to the same table
	private static final Set<String> claimedTables = ConcurrentHashMap.newKeySet();
	private @Nullable String claimedTable;

	private String table = DEFAULT_TABLE;
	private String metaTable = DEFAULT_TABLE + "_meta";

	private final ReentrantLock connectionLock = new ReentrantLock();
	private @Nullable Connection connection;
	private long lastUsedNanos;

	private long loadedVersion; // generation counter to help in polling for changes

	/**
	 * @param registeringAddon the addon that registered this storage's type
	 * @param type             the type name the config picked this storage with, such as {@code sqlite}
	 */
	protected JdbcStorage(SkriptAddon registeringAddon, String type) {
		super(registeringAddon, type);
	}

	// -------------- CONFIGURATION -----------------

	@Override
	protected final boolean configure(SectionNode node) {
		String table = node.getValue("table");
		if (table == null)
			table = DEFAULT_TABLE;
		if (!TABLE_NAME.matcher(table).matches()) {
			Skript.error("The table '" + table + "' of the database '" + getName() + "' isn't a valid name: use up to 58 "
				+ "letters, digits and underscores, not starting with a digit");
			return false;
		}
		this.table = table;
		this.metaTable = table + "_meta";
		if (node.getValue("monitor changes") != null)
			// temporary warning, since monitoring is covered in the MySQL PR.
			Skript.warning("The database '" + getName() + "' of type '" + getType() + "' can't monitor changes, so 'monitor changes' is ignored");
		if (!configureConnection(node))
			return false;

		// only claim once we're sure everythign else is valid.
		String key = tableKey(table);
		if (!claimedTables.add(key)) {
			Skript.error("The table '" + table + "' of the database '" + getName() + "' is already used by another database. "
				+ "Give each database its own 'table'");
			return false;
		}
		claimedTable = key;
		return true;
	}

	/**
	 * Reads the connection specific config options, like host and port.
	 *
	 * @param node The config node for this database.
	 * @return Whether everything was parsed correctly.
	 */
	protected abstract boolean configureConnection(SectionNode node);

	/**
	 * Identifies a table of this storage's database, so that two storages can't save to the same table.
	 * Two storages get the same key exactly when they would use the same table,
	 * so the key must include where the database is, such as its file or its host, port and database name.
	 * Called once, after {@link #configureConnection(SectionNode)} succeeds.
	 *
	 * @param table The table's name, already checked to be valid
	 * @return A key that is equal for two storages only if they use the same table of the same database
	 */
	protected abstract String tableKey(String table);

	// ------------ CONNECTION ----------------

	/**
	 * Handles creating, setting up and closing the connection to the database.
	 *
	 * @return the open connection, opened or reopened if needed, with autocommit off
	 */
	private Connection connection() throws SQLException {
		assert connectionLock.isHeldByCurrentThread();
		long now = System.nanoTime();
		Connection connection = this.connection;
		if (connection != null && now - lastUsedNanos > IDLE_CHECK_NANOS && !connection.isValid(2)) {
			dropConnection();
			connection = null;
		}
		if (connection == null) {
			connection = connect();
			try {
				setUp(connection);              // autocommit still on: SQLite can't switch to WAL inside a transaction
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				closeQuietly(connection);
				throw e;
			}
			this.connection = connection;
		}
		lastUsedNanos = now;
		return connection;
	}

	/**
	 * Opens a new connection to the database, initially with autocommit on.
	 * The caller will set it up and disable autocommit later.
	 *
	 * @return A new connection to the database.
	 * @throws SQLException If the connection could not be established.
	 */
	protected abstract Connection connect() throws SQLException;

	/**
	 * Sets up a new connection, with autocommit initially on. This defaults to doing nothing.
	 *
	 * @param connection The connection to set up.
	 * @throws SQLException If the setup failed or the connection dropped.
	 */
	protected void setUp(Connection connection) throws SQLException {
	}

	/**
	 * Close the connection quietly if it is open. Must be done under the connectionLock.
	 * This should be called if a SQLException is ever encountered, so the next use gets a new
	 * connection.
	 */
	private void dropConnection() {
		assert connectionLock.isHeldByCurrentThread();
		Connection connection = this.connection;
		this.connection = null;
		if (connection != null)
			closeQuietly(connection);
	}

	/**
	 * Closes a connection while ignoring any exceptions.
	 *
	 * @param connection The connection to close.
	 */
	private static void closeQuietly(@NotNull Connection connection) {
		try {
			connection.close();
		} catch (SQLException ignored) {
			// it's being thrown away anyway
		}
	}

	// -------------- SCHEMA / TABLE SETUP -----------------

	/**
	 * Generates the statements that create Skript's tables and index if they don't already exist.
	 * This must create a main table named {@link #table} with a name, type, value, version, and server column,
	 * and a meta table with a version column, if and only if they do not already exist.
	 *
	 * @param table     The name of the table to create for variables
	 * @param metaTable The name of the table to create for metadata
	 * @return The list of statements to execute.
	 */
	protected abstract List<String> schemaSql(String table, String metaTable);

	/**
	 * Executes {@link #schemaSql(String, String)} statements and checks if the required main table columns exist.
	 *
	 * @param connection The connection to use to send the statements.
	 * @throws SQLException If anything goes wrong with the schema statements
	 * @throws IOException  If the resulting table does not have the required columns, or otherwise has issues.
	 */
	private void createSchema(Connection connection) throws SQLException, IOException {
		try (Statement statement = connection.createStatement()) {
			for (String sql : schemaSql(table, metaTable))
				statement.execute(sql);
			try {
				statement.executeQuery("SELECT name, type, value, version, server FROM " + table + " WHERE 1 = 0").close();
			} catch (SQLException e) {
				throw new IOException("The table '" + table + "' already exists but can't be read by this version of Skript. "
					+ "Pick another 'table' for the database '" + getName() + "'", e);
			}
		}
		connection.commit();
	}

	/**
	 * Gets the current version of the table.
	 *
	 * @param connection The connection to use.
	 * @return The version from the metadata for this storage.
	 * @throws SQLException If the row is not present.
	 */
	private long readVersion(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement();
		     ResultSet result = statement.executeQuery("SELECT version FROM " + metaTable + " WHERE id = 0")) {
			if (!result.next())
				throw new SQLException("The counter row of '" + metaTable + "' is missing");
			return result.getLong(1);
		}
	}

	// ---------------- LOADING -------------------

	@Override
	protected final void load(LoadListener listener) throws IOException {
		connectionLock.lock();
		try {
			Connection connection = connection();
			createSchema(connection);
			loadedVersion = readVersion(connection);
			LoadReport problems = new LoadReport();
			Binder unused = statement -> {
			}; // will be used later
			readRows(connection, "value IS NOT NULL", unused, listener, problems);
			connection.commit(); // ends the read transaction, so SQLite's WAL can be checkpointed
			problems.print("the table '" + table + "' of the database '" + getName() + "'", "these rows were skipped and left in the table");
		} catch (SQLException e) {
			dropConnection();
			throw new IOException("Couldn't read the variables from the table '" + table + "': " + e.getMessage(), e);
		} finally {
			connectionLock.unlock();
		}
	}

	/**
	 * Binds the parameters of a query's WHERE clause.
	 */
	@FunctionalInterface
	protected interface Binder {
		void bind(PreparedStatement statement) throws SQLException;
	}

	/**
	 * Reads the rows matching a condition and passes each readable one to the listener.
	 * The one place rows are read, so a future partial load is another caller with a range or exact names.
	 */
	private void readRows(Connection connection, String where, Binder binder, LoadListener listener,
	                      LoadReport problems) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT name, type, value FROM " + table + " WHERE " + where)) {
			statement.setFetchSize(loadFetchSize());
			binder.bind(statement);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					String name = rows.getString(1);
					String type = rows.getString(2);
					byte[] data = rows.getBytes(3);
					if (name == null || type == null || data == null) {
						problems.add(LoadReport.INVALID, name);
					} else if (name.endsWith("::*")) {
						problems.add(LoadReport.LIST_VALUES, name);
					} else {
						listener.loaded(name, new SerializedVariable.Value(type, data));
					}
				}
			}
		}
	}

	/**
	 * @return The fetch size for reading every row; MySQL needs a streaming value.
	 */
	protected int loadFetchSize() {
		return 1000;
	}

	// ---------------- CLOSING -------------------

	/**
	 * Closes the connection and frees this storage's table, so that another storage can use it.
	 * Called by Skript on shutdown and after a failed load, once saving has stopped.
	 */
	@Override
	public final void close() {
		connectionLock.lock();
		try {
			dropConnection();
		} finally {
			connectionLock.unlock();
		}
		if (claimedTable != null) {
			claimedTables.remove(claimedTable);
			claimedTable = null;
		}
	}

	// ---------------- MISC -----------------------

	/**
	 * @return the counter's value when loading started
	 */
	protected final long loadedVersion() {
		return loadedVersion;
	}

	/**
	 * @return the table name
	 */
	protected final String table() {
		return table;
	}

}
