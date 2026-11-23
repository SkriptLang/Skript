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
import java.util.concurrent.atomic.AtomicLong;
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
	private static final int ROWS_PER_STATEMENT = 1000;
	private static final long MIN_MONITOR_INTERVAL_MILLIS = 100;

	private final String serverId = UUID.randomUUID().toString(); // marks the rows this server saves, so its polls skip them
	private String rowServer = ""; // what goes in the server column: serverId while monitoring changes, otherwise nothing

	// the tables used by configured storages, as keys from tableKey, so two storages can't save to the same table
	private static final Set<String> claimedTables = ConcurrentHashMap.newKeySet();
	private @Nullable String claimedTable;

	private String table = DEFAULT_TABLE;
	private String metaTable = DEFAULT_TABLE + "_meta";

	private final ReentrantLock connectionLock = new ReentrantLock();
	private @Nullable Connection connection;
	private long lastUsedNanos;

	private long loadedVersion;    // the counter's value when loading, where polling starts
	private TombstonePruner pruner = new TombstonePruner(System.nanoTime());
	private long lastSavedVersion; // the version of this server's last save, or loadedVersion before it saves. Saving thread only
	private boolean warnedOtherWriters;

	// monitoring
	private boolean monitorChanges;
	private long monitorIntervalNanos;

	private static final int REPORT_CHUNK = 1000; // how many reported names to mark as applied at once

	private volatile boolean monitoring;
	private @Nullable Thread monitorThread;
	private final ReportTracker tracker = new ReportTracker();
	private final AtomicLong reportCount = new AtomicLong(); // how many changes were reported, for tests
	private long pollNumber;            // counts polls, so the tracker can tell a newer report of a name from an older one
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
		this.rowServer = monitorChanges ? serverId : "";
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

		// only claim once everything else is valid
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
	 * Whether other servers can share this storage's table, which decides whether the {@code monitor changes} option is
	 * allowed. A type that can't be shared, like a file only this server opens, warns and ignores the option.
	 * Called once, while configuring.
	 *
	 * @return Whether other servers could be using this storage's table
	 */
	protected abstract boolean supportsMonitoring();

	/**
	 * Whether this server monitors the table for changes, as the {@code monitor changes} option says. Only then is the
	 * table shared, so only then are deletions kept as tombstones, rows saved with this server's id, and rows indexed by
	 * version for polling. Otherwise, deleted rows are removed right away and nothing is pruned.
	 *
	 * @return Whether this server monitors changes
	 */
	protected final boolean monitorsChanges() {
		return monitorChanges;
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
			lastSavedVersion = loadedVersion;
			pruner.sample(System.nanoTime(), loadedVersion);
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
	 * Reads every variable's row, skipping tombstones, and passes each readable one to the listener.
	 */
	private void readRows(Connection connection, LoadListener listener, LoadReport problems) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT name, type, value FROM " + table + " WHERE value IS NOT NULL")) {
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
			if (monitorChanges) // committed; still under the lock, so no poll reports anything in between
				tracker.forEachRaced(batch, this::report);
		} catch (SQLException e) {
			dropConnection();
			throw new IOException("Couldn't save to the table '" + table + "': " + e.getMessage(), e);
		} finally {
			connectionLock.unlock();
		}
	}

	/**
	 * Warns, once, if another server saved to this table since this server's last save while this server doesn't monitor
	 * it. Each save bumps the counter by one, so a jump means someone else saved in between. That server shares the table,
	 * but this one removes deleted rows outright, so the others never see those deletions, and this one never sees their
	 * changes. Costs nothing: the version is read by every save anyway.
	 */
	private void noticeOtherWriters(long version) {
		long previous = lastSavedVersion;
		lastSavedVersion = version;
		if (monitorChanges || warnedOtherWriters || version == previous + 1)
			return;
		warnedOtherWriters = true;
		Skript.warning("Another server also saves variables to the table '" + table + "' of the database '" + getName()
			+ "', but this server doesn't monitor changes. It removes deleted variables from the table right away, so the "
			+ "other servers won't see those deletions, and it won't see their changes. Turn on 'monitor changes' for every "
			+ "server that uses this table");
	}

	/**
	 * Bumps the counter in the meta table, which locks its row until this transaction ends.
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
	 * Clears a subset of variable rows: turns them into tombstones if this storage is {@link #monitorsChanges()},
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
		String sql = monitorChanges
			? "UPDATE " + table + " SET type = NULL, value = NULL, version = ?, server = ? "
				+ "WHERE name >= ? AND name < ? AND value IS NOT NULL"
			: "DELETE FROM " + table + " WHERE name >= ? AND name < ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			for (String prefix : prefixes) { // in order
				int parameter = 1;
				if (monitorChanges) {
					statement.setLong(parameter++, version);
					statement.setString(parameter++, rowServer);
				}
				statement.setString(parameter++, prefix);
				statement.setString(parameter, CsvFormat.prefixEnd(prefix));
				statement.addBatch();
			}
			statement.executeBatch();
		}
	}

	/**
	 * Write new variable changes to the database. Updates row version.
	 * Deletions become tombstones if this storage is {@link #monitorsChanges()}, and are deleted otherwise.
	 * A batch has one change per name, so the deletions and the other changes can be sent separately.
	 *
	 * @param connection The connection to use.
	 * @param changes    The variables to update.
	 * @param version    The new version to use.
	 * @throws SQLException If any of the statements fail.
	 */
	private void write(Connection connection, Collection<SerializedVariable> changes, long version) throws SQLException {
		if (changes.isEmpty())
			return;
		try (RowWriter rows = new RowWriter(connection, table, count -> upsertSql(table, count), rowsPerStatement(),
				maxStatementBytes(), version, rowServer)) {
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
				if (change.value == null && !monitorChanges) {
					rows.delete(change.name);
				} else {
					rows.upsert(change.name, change.value); // a tombstone if it's a deletion
				}
			}
		}
	}

	/**
	 * @param table The table to update
	 * @param rows  How many rows the statement inserts, each bound as name, type, value, version and server, in that order
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
	 * @return How many rows one upsert, or one delete by name, may cover: {@value #ROWS_PER_STATEMENT}, which keeps
	 * SQLite well under its parameter limit. Package-private, so tests can make statements split sooner
	 */
	@VisibleForTesting
	int rowsPerStatement() {
		return ROWS_PER_STATEMENT;
	}

	/**
	 * @return The most bytes one statement of several rows may take, as the driver sends it. A row's size is estimated
	 * generously, from its name, type and value. Defaults to no limit
	 */
	protected long maxStatementBytes() {
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
	 * Reports the rows other servers saved since the last poll, read in one go. Rows aren't sorted or paged: each name
	 * has one row, so the order doesn't matter, and sorting a big range for every page made a poll after a large save
	 * take minutes. The rows are streamed, with {@link #loadFetchSize()}, so they're never all held at once.
	 * Package-private, so tests can poll when they choose instead of waiting for the thread.
	 */
	@VisibleForTesting
	void poll() {
		if (!monitoring)
			return; // closed: don't open a new connection
		long poll = ++pollNumber;
		long after = pollCursor;
		long target;
		try {
			// one transaction, so the rows are read from the same snapshot as the counter: every version up to it has
			// committed, since saves commit in counter order, and anything later is left for the next poll
			target = withConnection(connection -> {
				long latest = readVersion(connection);
				if (latest > after)
					reportRows(connection, after, latest, poll);
				return latest;
			});
		} catch (SQLException e) {
			pollFailed(e);
			return; // the cursor hasn't moved, so the next poll reports the same rows again, which is harmless
		}
		if (!monitoring)
			return; // stopping: leave the cursor, nothing reported now is applied anyway
		pollCursor = target;
		pollSucceeded();
	}

	/**
	 * Reports every row another server saved after version {@code after}, up to {@code target}, and notes each name in
	 * the {@link ReportTracker}. Runs under the connection lock, so each name is reported either before or after any save
	 * of this server's commits, never during one.
	 *
	 * @param after  The last version already reported
	 * @param target The counter, read in the same transaction, so rows still being committed are left for the next poll
	 * @param poll   Which poll this is, for the tracker
	 */
	private void reportRows(Connection connection, long after, long target, long poll) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT name, type, value FROM " + table
				+ " WHERE version > ? AND version <= ? AND server <> ?")) {
			statement.setFetchSize(loadFetchSize());
			statement.setLong(1, after);
			statement.setLong(2, target);
			statement.setString(3, serverId);
			List<String> reported = new ArrayList<>();
			try (ResultSet result = statement.executeQuery()) {
				while (monitoring && result.next()) {
					String name = result.getString(1);
					String type = result.getString(2);
					byte[] data = result.getBytes(3);
					// no server saves a list as one variable, and a type needs a value: such a row is broken, so skip it
					if (name.endsWith("::*") || (type == null) != (data == null))
						continue;
					SerializedVariable.Value value = type == null ? null : new SerializedVariable.Value(type, data); // null: a tombstone
					tracker.reported(name, poll);
					report(name, value);
					reported.add(name);
					if (reported.size() == REPORT_CHUNK) {
						List<String> chunk = reported;
						runAfterReports(() -> tracker.applied(chunk, poll)); // behind the reports, so after they're applied
						reported = new ArrayList<>();
					}
				}
			} finally {
				// even if reading failed partway: the names reported so far must be marked, or they'd wait forever
				if (!reported.isEmpty()) {
					List<String> chunk = reported;
					runAfterReports(() -> tracker.applied(chunk, poll));
				}
			}
		}
	}

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
		if (!reportedBehind && System.nanoTime() - lastPollNanos > TombstonePruner.RETENTION_NANOS) {
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

	// --------- REPORTS AND RACES ---------

	/**
	 * Reports a change another server saved, or this server's own saved value again after a race; see
	 * {@link ReportTracker}. KEEP_LOCAL, since a pending local change is newer than either and will be saved after them.
	 */
	private void report(String name, SerializedVariable.@Nullable Value value) {
		reportCount.incrementAndGet();
		reportUpdated(name, value, ReportPolicy.KEEP_LOCAL);
	}

	/**
	 * @return How many changes this storage has reported so far, including re-reports after races
	 */
	@VisibleForTesting
	long reportCount() {
		return reportCount.get();
	}

	/**
	 * @return What this storage remembers of its reports, for tests
	 */
	@VisibleForTesting
	ReportTracker tracker() {
		return tracker;
	}

	@Override
	protected final void saveCycleEnded() {
		if (monitorChanges)
			tracker.saveCycleEnded();
	}

	// ---------------- PRUNING ------------------

	/**
	 * Removes the tombstones that are over an hour old, every few minutes; see {@link TombstonePruner}.
	 *
	 * @param connection The connection to use, in the save's transaction
	 * @param version    The save's version
	 */
	private void pruneIfDue(Connection connection, long version) throws SQLException {
		if (!monitorChanges) // no tombstones to prune
			return;
		long now = System.nanoTime();
		pruner.sample(now, version);
		long cutoff = pruner.dueCutoff(now);
		if (cutoff == TombstonePruner.NOT_DUE)
			return;
		try (PreparedStatement statement = connection.prepareStatement(
			"DELETE FROM " + table + " WHERE value IS NULL AND version < ?")) {
			statement.setLong(1, cutoff);
			statement.executeUpdate();
		}
	}

	/**
	 * Replaces the pruner, so tests can choose its times.
	 */
	@VisibleForTesting
	void pruner(TombstonePruner pruner) {
		this.pruner = pruner;
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
		// then the connection
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
	 * @return The counter's value when loading started
	 */
	@VisibleForTesting
	long loadedVersion() {
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
