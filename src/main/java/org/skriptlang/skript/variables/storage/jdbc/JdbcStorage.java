package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.storage.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Saves variables to a table in an SQL database, through JDBC.
 * <p>
 * Each variable is one row: its name, the codename of its type, and its serialized value. Next to it is a second table,
 * {@code <table>_meta}, with a single row holding a version counter. Every save is one transaction that bumps the
 * counter and stamps each row it writes with the new version. Deleting a variable or clearing a list removes its rows.
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
	private static final int BATCH_SIZE = 1000;

	// the tables used by configured storages, as keys from tableKey, so two storages can't save to the same table
	private static final Set<String> claimedTables = ConcurrentHashMap.newKeySet();
	private @Nullable String claimedTable;

	private String table = DEFAULT_TABLE;
	private String metaTable = DEFAULT_TABLE + "_meta";

	private final ReentrantLock connectionLock = new ReentrantLock();
	private @Nullable Connection connection;
	private long lastUsedNanos;

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
		if (!configureConnection(node))
			return false;

		// only claim once we're sure everything else is valid.
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

	// -------------- SCHEMA / TABLE SETUP -----------------

	/**
	 * Generates the statements that create Skript's tables if they don't already exist.
	 * This must create a main table named {@link #table} with a name, type, value and version column,
	 * and a meta table with an id and a version column, if and only if they do not already exist.
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
				statement.executeQuery("SELECT name, type, value, version FROM " + table + " WHERE 1 = 0").close();
			} catch (SQLException e) {
				throw new IOException("The table '" + table + "' already exists but can't be read by this version of Skript. "
					+ "Pick another 'table' for the database '" + getName() + "'", e);
			}
		}
		connection.commit();
	}

	// ---------------- LOADING -------------------

	@Override
	protected final void load(LoadListener listener) throws IOException {
		connectionLock.lock();
		try {
			Connection connection = connection();
			createSchema(connection);
			LoadReport problems = new LoadReport();
			readRows(connection, listener, problems);
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
	 * Reads every variable's row, and passes each readable one to the listener.
	 */
	private void readRows(Connection connection, LoadListener listener, LoadReport problems) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT name, type, value FROM " + table)) {
			statement.setFetchSize(loadFetchSize());
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

	// ---------------- SAVING --------------------

	@Override
	protected final void save(Batch batch) throws IOException {
		connectionLock.lock();
		try {
			Connection connection = connection();
			try {
				// each save gets its own version, which every row it writes is stamped with
				long version = nextVersion(connection);
				// make the changes
				clear(connection, batch.clears());
				write(connection, batch.changes(), version);
				// a whole batch is committed at once to prevent data fracturing.
				// if it fails, we can just try it again next time.
				connection.commit();
			} catch (SQLException | RuntimeException e) {
				rollbackQuietly(connection);
				throw e;
			}
		} catch (SQLException e) {
			dropConnection();
			throw new IOException("Couldn't save to the table '" + table + "': " + e.getMessage(), e);
		} finally {
			connectionLock.unlock();
		}
	}

	/**
	 * Helper to increment and return the version value in the meta table.
	 *
	 * @param connection The connection to use
	 * @return The incremented version value
	 * @throws SQLException If accessing or updating the meta table failed.
	 */
	private long nextVersion(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			if (statement.executeUpdate("UPDATE " + metaTable + " SET version = version + 1 WHERE id = 0") != 1)
				throw new SQLException("The counter row of '" + metaTable + "' is missing");
			try (ResultSet result = statement.executeQuery("SELECT version FROM " + metaTable + " WHERE id = 0")) {
				result.next();
				return result.getLong(1);
			}
		}
	}

	/**
	 * Deletes the rows of the cleared lists.
	 * We use name >= and < to clear a full subset, e.g. anything between "a::" and "a:;" covers the entire "a::*" list.
	 *
	 * @param connection The connection to use.
	 * @param prefixes   The prefixes to clear, e.g. `"a::"` for a clear meant to hit `{a::*}`.
	 * @throws SQLException If the delete fails.
	 */
	private void clear(Connection connection, List<String> prefixes) throws SQLException {
		if (prefixes.isEmpty())
			return;
		try (PreparedStatement statement = connection.prepareStatement(
			"DELETE FROM " + table + " WHERE name >= ? AND name < ?")) {
			for (String prefix : prefixes) { // in order
				statement.setString(1, prefix);
				statement.setString(2, CsvFormat.prefixEnd(prefix));
				statement.addBatch();
			}
			statement.executeBatch();
		}
	}

	/**
	 * Write new variable changes to the database, stamping each written row with the save's version.
	 * Deleted variables have their rows removed.
	 * A batch has one change per name, so the deletions and the other changes can be sent as two separate batches.
	 *
	 * @param connection The connection to use.
	 * @param changes    The variables to update.
	 * @param version    The new version to use.
	 * @throws SQLException If any of the statements fail.
	 */
	private void write(Connection connection, Collection<SerializedVariable> changes, long version) throws SQLException {
		if (changes.isEmpty())
			return;
		try (Batcher upserts = new Batcher(connection, upsertSql(table));
		     Batcher deletes = new Batcher(connection, "DELETE FROM " + table + " WHERE name = ?")) {
			for (SerializedVariable change : changes) {
				// name check
				if (change.name.length() > maxNameLength()) {
					skipUnsavable(change.name);
					continue;
				}
				if (change.value == null) {
					PreparedStatement statement = deletes.statement();
					statement.setString(1, change.name);
					deletes.add();
					continue;
				}
				PreparedStatement statement = upserts.statement();
				statement.setString(1, change.name);
				statement.setString(2, change.value.type);
				statement.setBytes(3, change.value.data);
				statement.setLong(4, version);
				upserts.add();
			}
		}
	}

	/**
	 * A statement that's only prepared once it's first used, and sent in chunks of {@link #BATCH_SIZE},
	 * so a huge batch isn't held all at once. Closing it sends what's left.
	 */
	private static final class Batcher implements AutoCloseable {

		private final Connection connection;
		private final String sql;
		private @Nullable PreparedStatement statement;
		private int pending;

		Batcher(Connection connection, String sql) {
			this.connection = connection;
			this.sql = sql;
		}

		/**
		 * @return The statement, to bind the next row's parameters to
		 */
		PreparedStatement statement() throws SQLException {
			if (statement == null)
				statement = connection.prepareStatement(sql);
			return statement;
		}

		/**
		 * Adds the bound row to the batch, and sends the batch if it's full.
		 */
		void add() throws SQLException {
			assert statement != null;
			statement.addBatch();
			if (++pending == BATCH_SIZE) { // prevent statement from getting TOO long.
				statement.executeBatch();
				pending = 0;
			}
		}

		/**
		 * Sends what's left and closes the statement. If sending fails, the statement is still closed.
		 */
		@Override
		public void close() throws SQLException {
			if (statement == null)
				return;
			try (PreparedStatement statement = this.statement) {
				if (pending > 0)
					statement.executeBatch();
			}
		}

	}

	/**
	 * @param table The table to update
	 * @return A statement that updates or inserts a row in the table.
	 */
	protected abstract String upsertSql(String table);

	private final Set<String> reportedUnsavable = new HashSet<>();

	private void skipUnsavable(String name) {
		if (reportedUnsavable.add(name))
			Skript.error("The variable '" + name + "' can't be saved to the database '" + getName()
				+ "', since its name is longer than " + maxNameLength() + " characters. It's kept until the server stops");
	}

	// ---------------- BACKUPS -------------------

	@Override
	protected final boolean supportsBackups() {
		return true;
	}

	@Override
	protected final void writeBackup(OutputStream out) throws IOException {
		connectionLock.lock();
		try {
			Connection connection = connection();
			try {
				CsvFormat.Writer writer = CsvFormat.Writer.backup(out);
				// copy each row to the csv file
				readRows(connection, (name, value) -> {
					try {
						writer.row(name, value);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}, new LoadReport());
				writer.finish();
				connection.commit();
			} catch (SQLException | RuntimeException e) {
				rollbackQuietly(connection);
				throw e;
			}
		} catch (SQLException e) {
			dropConnection();
			throw new IOException("Couldn't back up the table '" + table + "': " + e.getMessage(), e);
		} catch (UncheckedIOException e) {
			throw e.getCause();
		} finally {
			connectionLock.unlock();
		}
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

	/**
	 * Rolls back the open transaction while ignoring any exceptions.
	 *
	 * @param connection The connection to roll back.
	 */
	private static void rollbackQuietly(Connection connection) {
		try {
			connection.rollback();
		} catch (SQLException ignored) {
			// it's being thrown away anyway
		}
	}

	// ---------------- MISC -----------------------

	/**
	 * @return the table name
	 */
	protected final String table() {
		return table;
	}

	/**
	 * @return The longest name that can be stored. Defaults to int max.
	 */
	protected int maxNameLength() {
		return Integer.MAX_VALUE;
	}

}
