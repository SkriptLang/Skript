package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.LoadListener;
import org.skriptlang.skript.variables.storage.LoadReport;
import org.skriptlang.skript.variables.storage.VariableStorage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Saves variables to a table in an SQL database, through JDBC.
 * <p>
 * Each variable is one row: its name, the codename of its type, and its serialized value. Next to it is a second table,
 * {@code <table>_meta}, with a single row holding a version counter. Every save is one transaction that bumps the
 * counter and stamps each row it writes with the new version. If the database
 * {@link #supportsMonitoring() supports monitoring} and this server monitors changes, the table is shared: each row
 * also gets this server's id, and deleting a variable or clearing a list doesn't remove its rows. Instead, they become
 * "tombstones", rows without a type or value, with the new version, and get pruned once they're old enough. That way,
 * other servers reading the table can ask what changed since some version, deletions included.
 * Otherwise, the table is this server's alone, so deleted rows are just removed and rows don't get a server id.
 * Every server sharing a table has to monitor it, or the others miss its deletions; see {@link #noticeOtherWriters}.
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
	private static final long MIN_MONITOR_INTERVAL_MILLIS = 100;

	private final String serverId = UUID.randomUUID().toString(); // an id to show what server wrote this line. Customizable in future?
	private boolean monitorable; // supportsMonitoring(), set while configuring
	private String rowServer = ""; // what goes in the server column: serverId if monitorable, otherwise nothing

	// the tables used by configured storages, as keys from tableKey, so two storages can't save to the same table
	private static final Set<String> claimedTables = ConcurrentHashMap.newKeySet();
	private @Nullable String claimedTable;

	private String table = DEFAULT_TABLE;
	private String metaTable = DEFAULT_TABLE + "_meta";

	private final ReentrantLock connectionLock = new ReentrantLock();
	private @Nullable Connection connection;
	private long lastUsedNanos;


	// monitoring stuff
	private long loadedVersion; // generation counter to help in polling for changes
	private boolean monitorChanges;
	private long monitorIntervalNanos;

	private static final int POLL_PAGE_SIZE = 1000;

	private volatile boolean monitoring;
	private @Nullable Thread monitorThread;
	private long pollCursor;            // the last version fully reported
	private long lastPollNanos;         // when a poll last succeeded, to notice falling too far behind
	private boolean pollFailing;        // so a failing database is reported once, not every second
	private boolean reportedBehind;


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
		// monitoring
		Boolean monitor = getConfigOptional(node, "monitor changes", Boolean.class, false);
		if (monitor == null)
			return false;
		if (monitor && !supportsMonitoring()) {
			Skript.warning("The database '" + getName() + "' of type '" + getType() + "' can't monitor changes, "
				+ "since no other server can use it, so 'monitor changes' is ignored");
			monitor = false;
		}
		monitorChanges = monitor;
		// the table is only shared, and only pays for tombstones, server ids and the version index, if this server monitors it
		this.monitorable = monitorChanges;
		this.rowServer = monitorable ? serverId : "";
		// get monitor frequency
		if (monitorChanges) {
			Timespan interval = getConfigOptional(node, "monitor interval", Timespan.class,
				new Timespan(Timespan.TimePeriod.SECOND, 1));
			if (interval == null)
				return false;
			long millis = interval.getAs(Timespan.TimePeriod.MILLISECOND);
			if (millis < MIN_MONITOR_INTERVAL_MILLIS) {
				Skript.error("The entry for 'monitor interval' in the database '" + getName() + "' must be at least "
					+ MIN_MONITOR_INTERVAL_MILLIS + " milliseconds, since each check asks the database");
				return false;
			}
			monitorIntervalNanos = TimeUnit.MILLISECONDS.toNanos(millis);
		}

		// configure connection with type-specific stuff
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

	/**
	 * Whether other servers can share this storage's table and watch it for changes. This is a fact about the type of
	 * database, not the {@code monitor changes} option: a server that doesn't monitor a shared table still has to keep
	 * its deletions as tombstones, so the servers that do monitor it can see them.
	 * <p>
	 * If false, deleted rows are removed right away, nothing is pruned, and rows are saved without a server id.
	 * The version counter is still bumped, since it costs one row per save.
	 * Called once, while configuring.
	 *
	 * @return Whether other servers could be reading this storage's table
	 */
	protected abstract boolean supportsMonitoring();

	/**
	 * @return Whether this storage keeps tombstones and indexes rows by version: only while it monitors changes,
	 * since only then is the table shared with other servers
	 */
	protected final boolean monitorable() {
		return monitorable;
	}

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
	 * Called after {@link #schemaSql} has run, before it's committed, for schema changes that depend on what's already
	 * there, such as adding an index the database can't add with {@code IF NOT EXISTS}.
	 */
	protected void schemaCreated(Connection connection) throws SQLException {}

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
			schemaCreated(connection);
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
			samples.addLast(new Sample(System.nanoTime(), loadedVersion));
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

	// ---------------- SAVING --------------------

	@Override
	protected final void save(Batch batch) throws IOException {
		connectionLock.lock();
		try {
			Connection connection = connection();
			try {
				// bumping the counter locks its row until we commit, so other servers' saves take their versions after ours
				// and commit after us too. That way versions are committed in order, and polling for newer ones never skips one.
				long version = nextVersion(connection);
				// make the changes
				clear(connection, batch.clears(), version);
				write(connection, batch.changes(), version);
				pruneIfDue(connection, version);
				// a whole batch is committed at once to prevent data fracturing.
				// if it fails, we can just try it again next time.
				connection.commit();
				noticeOtherWriters(version);
			} catch (SQLException | RuntimeException e) {
				rollbackQuietly(connection);
				throw e;
			}
			reReportRaced(batch); // committed; still under the lock, so no poll reads in between
		} catch (SQLException e) {
			dropConnection();
			throw new IOException("Couldn't save to the table '" + table + "': " + e.getMessage(), e);
		} finally {
			connectionLock.unlock();
		}
	}

	private long lastSavedVersion = -1; // the version of this server's last save, or -1 before the first. Saving thread only
	private boolean warnedOtherWriters;

	/**
	 * Warns, once, if another server saved to this table since this server's last save while this server doesn't monitor
	 * it. Each save bumps the counter by one, so a jump means someone else saved in between. That server shares the table,
	 * but this one removes deleted rows outright, so the others never see those deletions, and this one never sees their
	 * changes. Costs nothing: the version is read by every save anyway.
	 */
	private void noticeOtherWriters(long version) {
		long previous = lastSavedVersion < 0 ? loadedVersion : lastSavedVersion;
		lastSavedVersion = version;
		if (monitorable || warnedOtherWriters || version == previous + 1)
			return;
		warnedOtherWriters = true;
		Skript.warning("Another server also saves variables to the table '" + table + "' of the database '" + getName()
			+ "', but this server doesn't monitor changes. It removes deleted variables from the table right away, so the "
			+ "other servers won't see those deletions, and it won't see their changes. Turn on 'monitor changes' for every "
			+ "server that uses this table");
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
	 * Clears a subset of variable rows: turns them into tombstones if this storage is {@link #monitorable()},
	 * otherwise deletes them.
	 * We use name >= and < to clear a full subset, e.g. anything between "a::" and "a:;" covers the entire "a::*" list.
	 *
	 * @param connection The connection to use.
	 * @param prefixes   The prefixes to clear, e.g. `"a::"` for a clear meant to hit `{a::*}`.
	 * @param version    The new version to use in the cleared rows.
	 * @throws SQLException If the update fails.
	 */
	private void clear(Connection connection, List<String> prefixes, long version) throws SQLException {
		if (prefixes.isEmpty())
			return;
		if (!monitorable) {
			try (PreparedStatement statement = connection.prepareStatement(
				"DELETE FROM " + table + " WHERE name >= ? AND name < ?")) {
				for (String prefix : prefixes) { // in order
					statement.setString(1, prefix);
					statement.setString(2, CsvFormat.prefixEnd(prefix));
					statement.addBatch();
				}
				statement.executeBatch();
			}
			return;
		}
		try (PreparedStatement statement = connection.prepareStatement("UPDATE " + table
			+ " SET type = NULL, value = NULL, version = ?, server = ? WHERE name >= ? AND name < ? AND value IS NOT NULL")) {
			for (String prefix : prefixes) { // in order
				statement.setLong(1, version);
				statement.setString(2, rowServer);
				statement.setString(3, prefix);
				statement.setString(4, CsvFormat.prefixEnd(prefix));
				statement.addBatch();
			}
			statement.executeBatch();
		}
	}

	/**
	 * Write new variable changes to the database. Updates row version.
	 * Deletions become tombstones if this storage is {@link #monitorable()}, and are deleted otherwise.
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
		try (Upserter upserts = upsertRows() > 1 ? new MultiRowUpserter(connection, version) : new BatchUpserter(connection, version);
		     Batcher deletes = new Batcher(connection, "DELETE FROM " + table + " WHERE name = ?")) {
			for (SerializedVariable change : changes) {
				// name check
				if (change.name.length() > maxNameLength()) {
					skipUnsavable(change.name, "its name is longer than " + maxNameLength() + " characters");
					continue;
				}
				// value length check
				if (change.value != null && change.value.data.length > maxValueLength()) {
					skipUnsavable(change.name, "its value is " + change.value.data.length + " bytes, and it only accepts up to "
						+ maxValueLength());
					continue;
				}
				if (change.value == null && !monitorable) {
					PreparedStatement statement = deletes.statement();
					statement.setString(1, change.name);
					deletes.add();
					continue;
				}
				upserts.add(change.name, change.value);
			}
		}
	}

	/**
	 * Binds one upsert row's five parameters, starting after parameter {@code offset}.
	 */
	private void bindUpsert(PreparedStatement statement, int offset, String name, SerializedVariable.@Nullable Value value,
			long version) throws SQLException {
		statement.setString(offset + 1, name);
		if (value == null) { // a tombstone: both null
			statement.setNull(offset + 2, Types.VARCHAR);
			statement.setNull(offset + 3, Types.BLOB);
		} else {
			statement.setString(offset + 2, value.type);
			statement.setBytes(offset + 3, value.data);
		}
		statement.setLong(offset + 4, version);
		statement.setString(offset + 5, rowServer);
	}

	/**
	 * @return At most how many bytes a row adds to a multi-row upsert statement, which the driver sends as text:
	 * up to 3 UTF-8 bytes per character, doubled when escaped, for the name and type, 2 per byte for the value's hex,
	 * and 100 for the version, the server id, quotes and commas
	 */
	private static long upsertBytes(String name, SerializedVariable.@Nullable Value value) {
		long bytes = 6L * name.length() + 100;
		if (value != null)
			bytes += 6L * value.type.length() + 2L * value.data.length;
		return bytes;
	}

	/**
	 * Sends upserts in whatever way suits the driver. Closing it sends what's left.
	 */
	private interface Upserter extends AutoCloseable {

		/**
		 * Adds one row: a tombstone if {@code value} is null.
		 */
		void add(String name, SerializedVariable.@Nullable Value value) throws SQLException;

		@Override
		void close() throws SQLException;

	}

	/**
	 * Sends each row with {@link PreparedStatement#addBatch()}, for drivers whose batching is cheap.
	 */
	private final class BatchUpserter implements Upserter {

		private final Batcher batcher;
		private final long version;

		BatchUpserter(Connection connection, long version) {
			batcher = new Batcher(connection, upsertSql(table, 1));
			this.version = version;
		}

		@Override
		public void add(String name, SerializedVariable.@Nullable Value value) throws SQLException {
			bindUpsert(batcher.statement(), 0, name, value, version);
			batcher.add();
		}

		@Override
		public void close() throws SQLException {
			batcher.close();
		}

	}

	/**
	 * Collects rows and sends them as one statement of up to {@link #upsertRows()} rows, sooner if the next row would make
	 * the statement bigger than {@link #maxUpsertBytes()}. A row is always sent, even alone over the limit, so this can't
	 * get stuck. A full statement is prepared once per save and reused; shorter ones, at the end or cut short by size,
	 * are prepared when sent.
	 */
	private final class MultiRowUpserter implements Upserter {

		private record Row(String name, SerializedVariable.@Nullable Value value) {}

		private final Connection connection;
		private final long version;
		private final int maxRows = upsertRows();
		private final long maxBytes = maxUpsertBytes();
		private final List<Row> rows = new ArrayList<>();
		private long bytes;
		private @Nullable PreparedStatement full;

		MultiRowUpserter(Connection connection, long version) {
			this.connection = connection;
			this.version = version;
		}

		@Override
		public void add(String name, SerializedVariable.@Nullable Value value) throws SQLException {
			long size = upsertBytes(name, value);
			if (!rows.isEmpty() && bytes + size > maxBytes)
				send();
			rows.add(new Row(name, value));
			bytes += size;
			if (rows.size() == maxRows)
				send();
		}

		private void send() throws SQLException {
			if (rows.size() == maxRows) {
				if (full == null)
					full = connection.prepareStatement(upsertSql(table, maxRows));
				bindAndRun(full);
			} else {
				try (PreparedStatement statement = connection.prepareStatement(upsertSql(table, rows.size()))) {
					bindAndRun(statement);
				}
			}
			rows.clear();
			bytes = 0;
		}

		private void bindAndRun(PreparedStatement statement) throws SQLException {
			int offset = 0;
			for (Row row : rows) {
				bindUpsert(statement, offset, row.name(), row.value(), version);
				offset += 5;
			}
			statement.executeUpdate();
		}

		@Override
		public void close() throws SQLException {
			try {
				if (!rows.isEmpty())
					send();
			} finally {
				if (full != null)
					full.close();
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
	 * @param rows  How many rows the statement inserts, each bound as name, type, value, version and server, in that order.
	 *              Only more than 1 if {@link #upsertRows()} is
	 * @return An insert of that many rows that replaces any existing rows with the same names
	 */
	protected abstract String upsertSql(String table, int rows);

	/**
	 * @return {@code rows} groups of the five upsert parameters, such as {@code (?, ?, ?, ?, ?), (?, ?, ?, ?, ?)}
	 */
	protected static String upsertValues(int rows) {
		StringBuilder values = new StringBuilder(rows * 17);
		for (int i = 0; i < rows; i++)
			values.append(i == 0 ? "(?, ?, ?, ?, ?)" : ", (?, ?, ?, ?, ?)");
		return values.toString();
	}

	/**
	 * @return How many rows one upsert statement may insert. 1, the default, sends rows with {@link PreparedStatement#addBatch()}.
	 * More builds statements of that many rows, for drivers whose batching is expensive
	 */
	protected int upsertRows() {
		return 1;
	}

	/**
	 * @return The most bytes one upsert statement may take, as the driver sends it, if {@link #upsertRows()} is more than 1.
	 * A row's size is estimated generously, from its name, type and value. Defaults to no limit
	 */
	protected long maxUpsertBytes() {
		return Long.MAX_VALUE;
	}

	private final Set<String> reportedUnsavable = new HashSet<>();

	private void skipUnsavable(String name, String reason) {
		if (reportedUnsavable.add(name))
			Skript.error("The variable '" + name + "' can't be saved to the database '" + getName() + "', since " + reason
				+ ". It's kept until the server stops");
	}

	// ---------------- MONITORING ---------------

	@Override
	protected final void receivingStarted() {
		if (!monitorChanges)
			return;
		pollCursor = loadedVersion;
		lastPollNanos = System.nanoTime();
		monitoring = true;
		// start a thread that repeatedly polls for changes
		Thread thread = new Thread(this::monitorLoop, "Skript variable storage " + getName() + " monitor thread");
		thread.setDaemon(true);
		monitorThread = thread;
		thread.start();
	}

	/**
	 * Checks for changes every {@link #monitorIntervalNanos}.
	 */
	private void monitorLoop() {
		while (monitoring) {
			LockSupport.parkNanos(this, monitorIntervalNanos);
			if (!monitoring)
				break;
			try {
				poll();
			} catch (Throwable t) { // never let the thread die, or nothing is reported again
				Skript.exception(t, "Unexpected error while checking the database '" + getName() + "' for changes");
			}
		}
	}

	/**
	 * Reports the rows other servers saved since the last poll, page by page.
	 * Package-private, so tests can poll when they choose instead of waiting for the thread.
	 */
	@VisibleForTesting
	void poll() {
		if (!monitoring)
			return; // closed: don't open a new connection
		long poll = ++pollNumber;
		long target;
		try {
			target = withConnection(this::readVersion); // every version up to here has committed
		} catch (SQLException e) {
			pollFailed(e);
			return;
		}
		// early exit if we're still at latest version
		if (target <= pollCursor) {
			pollSucceeded();
			return;
		}
		long afterVersion = pollCursor;
		@Nullable String afterName = null;   // null for the first page: everything after the cursor's version
		while (monitoring) {
			List<PolledRow> page;
			// read a page from the db, keeping any rows that were newer than our version
			try {
				long v = afterVersion;
				String n = afterName;
				page = withConnection(connection -> {
					// get the changes
					List<PolledRow> rows = readPage(connection, v, n, target);
					List<String> reported = new ArrayList<>();
					synchronized (inFlight) {
						// report changes and mark them as inflight
						for (PolledRow row : rows) {
							if (row.broken())
								continue;
							inFlight.put(row.name(), new Flight(poll, -1));
							reportUpdated(row.name(), row.value(), ReportPolicy.KEEP_LOCAL);
							reported.add(row.name());
						}
					}
					// add barrier task to the queue so they are marked as applied in the inflight map
					if (!reported.isEmpty())
						runAfterReports(() -> markApplied(reported, poll));
					return rows;
				});
			} catch (SQLException e) {
				pollFailed(e);
				return; // the cursor hasn't moved, so the next poll tries the same rows again
			}
			if (page.size() < POLL_PAGE_SIZE)
				break;
			PolledRow last = page.getLast();
			afterVersion = last.version;
			afterName = last.name; // update to make sure we don't read anything twice
		}
		if (!monitoring)
			return; // stopping: leave the cursor, nothing reported now is applied anyway
		pollCursor = target;
		pollSucceeded();
	}

	/**
	 * Do the actual polling query.
	 *
	 * @param connection   The connection to use
	 * @param afterVersion The baseline version, so only report things newer than this
	 * @param afterName    Optional, for limiting where to start reading from.
	 * @param target       The latest version, just read, so that we skip anything that's still being committed.
	 * @return Any rows that have been written since afterVersion, by someone other than us.
	 * @throws SQLException If any statement fails.
	 */
	private List<PolledRow> readPage(Connection connection, long afterVersion, @Nullable String afterName, long target)
		throws SQLException {
		// order by version, then name, so we report any changes in order they were applied
		String sql = "SELECT name, type, value, version FROM " + table + " WHERE version <= ? AND server <> ? AND "
			+ (afterName == null ? "version > ?" : "(version > ? OR (version = ? AND name > ?))")
			+ " ORDER BY version, name LIMIT " + POLL_PAGE_SIZE;
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, target);
			statement.setString(2, serverId);
			statement.setLong(3, afterVersion);
			if (afterName != null) {
				statement.setLong(4, afterVersion);
				statement.setString(5, afterName);
			}
			List<PolledRow> rows = new ArrayList<>();
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					String name = result.getString(1);
					String type = result.getString(2);
					byte[] data = result.getBytes(3);
					long version = result.getLong(4);
					if (name.endsWith("::*")) {
						// no server saves a list as one variable, mark as broken
						rows.add(new PolledRow(name, null, version, true));
					} else if (type == null && data == null) {
						rows.add(new PolledRow(name, null, version, false)); // a tombstone: deleted
					} else if (type != null && data != null) {
						rows.add(new PolledRow(name, new SerializedVariable.Value(type, data), version, false));
					} else {
						// broken row; still count it for paging, but don't report it
						rows.add(new PolledRow(name, null, version, true));
					}
				}
			}
			return rows;
		}
	}

	private record PolledRow(String name, SerializedVariable.@Nullable Value value, long version, boolean broken) {}

	private interface ConnectionTask<T> {
		T run(Connection connection) throws SQLException;
	}

	/**
	 * Runs a read under the connection lock and ends its transaction afterwards, so the next read sees newer commits.
	 */
	private <T> T withConnection(ConnectionTask<T> task) throws SQLException {
		connectionLock.lock();
		try {
			Connection connection = connection();
			try {
				T result = task.run(connection);
				connection.commit(); // REPEATABLE READ keeps a snapshot until the transaction ends, and we don't want to see that forever, so end the tx
				return result;
			} catch (SQLException | RuntimeException e) {
				rollbackQuietly(connection);
				throw e;
			}
		} catch (SQLException e) {
			dropConnection();
			throw e;
		} finally {
			connectionLock.unlock();
		}
	}

	private void pollFailed(SQLException e) {
		if (!pollFailing) {
			pollFailing = true;
			Skript.error("Couldn't check the database '" + getName() + "' for changes from other servers: "
				+ e.getMessage() + ". Retrying every " + TimeUnit.NANOSECONDS.toMillis(monitorIntervalNanos) + " ms");
		}
		if (!reportedBehind && System.nanoTime() - lastPollNanos > TOMBSTONE_RETENTION_NANOS) {
			reportedBehind = true;
			Skript.error("The database '" + getName() + "' hasn't been checked for over an hour, so deletions made by "
				+ "other servers may have been permanently missed. Restart the server to reload its variables");
		}
	}

	private void pollSucceeded() {
		if (pollFailing) {
			pollFailing = false;
			Skript.info("Checking the database '" + getName() + "' for changes works again");
		}
		lastPollNanos = System.nanoTime();
	}

	// --------- IN-FLIGHT RACE HANDLING ---------

	/**
	 * Where a reported name is: waiting on the main thread (appliedEpoch -1), or applied during a save epoch.
	 * poll identifies which poll reported it, since a later poll can report the same name again.
	 */
	private record Flight(long poll, long appliedEpoch) {}

	// names reported by recent polls; see reReportRaced. Guarded by itself for the epoch logic.
	private final Map<String, Flight> inFlight = new HashMap<>();
	private long saveEpoch;   // counts finished save cycles; guarded by inFlight
	private long pollNumber;  // only the monitor thread touches it

	/**
	 * Helper to mark inflight names as fully applied.
	 *
	 * @param names The names to mark as applied
	 * @param poll  Which poll did this.
	 */
	private void markApplied(List<String> names, long poll) {
		synchronized (inFlight) {
			for (String name : names) {
				Flight flight = inFlight.get(name);
				if (flight != null && flight.poll() == poll) // a newer poll's mark is left alone
					inFlight.put(name, new Flight(poll, saveEpoch));
			}
		}
	}

	/**
	 * Fixes reports that raced this save. A report read before this save committed holds an older value than this save's.
	 * If it was applied after this batch was drained, nothing was pending to stop it, so memory got the older value.
	 * Re-reporting what was saved, after that report, puts memory back to what the table holds. KEEP_LOCAL skips the
	 * re-report if a newer local change is pending, which will be saved after this anyway.
	 * <p>
	 * The batch was drained some time after the previous save cycle ended, so "applied since the previous cycle ended"
	 * covers "applied since this batch was drained", plus a little extra, which re-reports a value memory already has.
	 */
	private void reReportRaced(Batch batch) {
		if (!monitorChanges)
			return;
		synchronized (inFlight) {
			if (!inFlight.isEmpty()) {
				// for each change in the batch, check if there's a inflight reported change already.
				// If so, we need to requeue this change to overwrite the inflight change.
				Set<String> changed = new HashSet<>();
				for (SerializedVariable change : batch.changes()) {
					changed.add(change.name);
					Flight flight = inFlight.get(change.name);
					if (flight != null && raced(flight))
						reportUpdated(change.name, change.value, ReportPolicy.KEEP_LOCAL);
				}
				for (String prefix : batch.clears()) {
					for (Map.Entry<String, Flight> entry : inFlight.entrySet()) {
						String name = entry.getKey();
						if (name.startsWith(prefix) && !changed.contains(name) && raced(entry.getValue()))
							reportUpdated(name, null, ReportPolicy.KEEP_LOCAL);
					}
				}
			}
		}
	}

	/**
	 * Starts a new save epoch, and forgets every report applied before it. Nothing drained is left unsaved here, so the
	 * next batch is drained after now, and a report applied before now can't have raced it: its local change was still
	 * pending when the report was applied, so KEEP_LOCAL kept it. Reports still waiting on the main thread are kept.
	 * Runs every cycle, saved or not, so a server that never saves still forgets what it was sent.
	 */
	@Override
	protected final void saveCycleEnded() {
		if (!monitorChanges)
			return;
		synchronized (inFlight) {
			saveEpoch++;
			if (!inFlight.isEmpty())
				inFlight.values().removeIf(flight -> flight.appliedEpoch() >= 0);
		}
	}

	// helper to check if there was a race
	private boolean raced(Flight flight) {
		return flight.appliedEpoch() < 0           // still waiting: it will be applied after this save, so follow it
			|| flight.appliedEpoch() == saveEpoch; // applied since the previous cycle ended, maybe after this drain
	}

	// ---------------- PRUNING ------------------

	private static final long TOMBSTONE_RETENTION_NANOS = TimeUnit.HOURS.toNanos(1);
	private static final long PRUNE_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(5);
	private static final long SAMPLE_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);

	// for tracking version ages
	private record Sample(long nanos, long version) {}

	private final ArrayDeque<Sample> samples = new ArrayDeque<>(); // oldest first
	private long lastPruneNanos = System.nanoTime();

	/**
	 * Removes all the stale tombstones from this storage's SQL table
	 *
	 * @param connection The connection to use.
	 * @param version    The new version.
	 * @throws SQLException If any statement fails.
	 */
	private void pruneIfDue(Connection connection, long version) throws SQLException {
		if (!monitorable) // no tombstones to prune
			return;
		long now = System.nanoTime();
		// add a new sample if we haven't taken one in a minute
		if (samples.isEmpty() || now - samples.peekLast().nanos() >= SAMPLE_INTERVAL_NANOS)
			samples.addLast(new Sample(now, version));
		if (now - lastPruneNanos < PRUNE_INTERVAL_NANOS)
			return;
		lastPruneNanos = now;
		// find the newest hour-plus-old sample, so we know what version to prune up to.
		// any older ones can be thrown out, they're useless
		Sample cutoff = null;
		while (!samples.isEmpty() && now - samples.peekFirst().nanos() >= TOMBSTONE_RETENTION_NANOS)
			cutoff = samples.pollFirst(); // keep removing the oldest
		if (cutoff == null)
			return;
		samples.addFirst(cutoff); // keep it, so the next prune still has a cutoff
		try (PreparedStatement statement = connection.prepareStatement(
			"DELETE FROM " + table + " WHERE value IS NULL AND version < ?")) {
			statement.setLong(1, cutoff.version());
			statement.executeUpdate();
		}
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
				readRows(connection, "value IS NOT NULL", statement -> {}, (name, value) -> {
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
		monitoring = false;
		Thread thread = monitorThread;
		if (thread != null) {
			LockSupport.unpark(thread);
			try {
				thread.join(10_000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			monitorThread = null;
		}
		// actually close now
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

	/**
	 * @return The longest name that can be stored. Defaults to int max.
	 */
	protected int maxNameLength() {
		return Integer.MAX_VALUE;
	}

	/**
	 * @return The longest value, in bytes, that can be saved in one row. Defaults to no limit.
	 */
	protected long maxValueLength() {
		return Long.MAX_VALUE;
	}

}
