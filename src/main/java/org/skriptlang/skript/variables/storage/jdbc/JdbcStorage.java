package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Saves variables to a table in an SQL database, through JDBC.
 * <p>
 * Each variable is one row: its name, the codename of its type, and its serialized value. Next to it is a second table,
 * {@code <table>_meta}, with a single row holding a version counter. Every save is one transaction that bumps the
 * counter, so each save has its own version. Deleting a variable or clearing a list removes its rows.
 * <p>
 * If the database {@link #supportsMonitoring() supports monitoring} and this server monitors changes, the table is
 * shared, and each save also notes every name it changed and every list it cleared in a third table, {@code <table>_log},
 * under the save's version. The log keeps one entry per name, its latest, so it only grows with how many names changed
 * recently, not how often. Other servers read the log for what changed since the version they last read, deletions and
 * clears included. Entries are pruned once they're {@link LogPruner#RETENTION_NANOS 5 minutes} old, and the meta table's
 * second row notes the version below which they're gone, so a server that falls behind knows it missed some. Every server
 * sharing a table has to monitor it, or the others miss its changes; see {@link #noticeOtherWriters}.
 * <p>
 * A monitoring server reads the log at the end of each of its save cycles, on its saving thread, and doesn't start the
 * next cycle until the main thread has applied what it read; see {@link #mayDrain()}. So every local change is either
 * saved before the log is read, so what was read is newer, or still waiting to be saved when the read changes are
 * applied, so they skip it. Either way, this server never shows an older value than one it set itself.
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
	private static final int MAX_EXCLUDED_VERSIONS = 1000; // how many of its own versions a poll leaves out by number
	private static final int PRUNE_VERSIONS = 1000; // how many versions' log entries one prune statement removes
	private static final long SLOW_REPORTS_NANOS = TimeUnit.SECONDS.toNanos(10); // when waiting on reports is logged

	// the tables used by configured storages, as keys from tableKey, so two storages can't save to the same table
	private static final Set<String> claimedTables = ConcurrentHashMap.newKeySet();
	private @Nullable String claimedTable;

	private String table = DEFAULT_TABLE;
	private String metaTable = DEFAULT_TABLE + "_meta";
	private String logTable = DEFAULT_TABLE + "_log";

	private final ReentrantLock connectionLock = new ReentrantLock();
	private @Nullable Connection connection;
	private long lastUsedNanos;

	private long loadedVersion;    // the counter's value when loading, where polling starts
	private long lastSavedVersion; // the version of this server's last save, or loadedVersion before it saves. Saving thread only
	private boolean warnedOtherWriters;

	// monitoring
	private boolean monitorChanges;
	private volatile boolean monitoring;
	private LogPruner pruner = new LogPruner(System.nanoTime());
	// the versions of this server's saves that polling hasn't passed yet, oldest first, so a poll can leave out this
	// server's own entries. Guarded by the connection lock
	private final ArrayDeque<Long> ownVersions = new ArrayDeque<>();
	private long pollCursor;            // the last version fully reported
	private boolean pollFailing;        // so a failing database is reported once, not every second
	private boolean reportedBehind;     // so missing pruned changes is reported once
	// whether the main thread has applied everything the last poll reported, so the next save cycle may drain
	private volatile boolean reportsApplied = true;
	private long waitingSinceNanos;
	private boolean loggedSlowReports;
	private final AtomicLong reportCount = new AtomicLong(); // for tests

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
		this.logTable = table + "_log";
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
	 * table shared, so only then do saves add entries to the change log, which needs its own table.
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
	 * Generates the statements that create Skript's tables if they don't already exist.
	 * This must create a main table named {@code table} with a name, type and value column,
	 * and a meta table with an id and a version column, if and only if they do not already exist. The meta table needs
	 * two rows, both starting at version 0: id 0 holds the counter, and id 1 the version the change log is pruned below.
	 * If this server {@link #monitorsChanges() monitors changes}, it must also create a log table with a name, a version
	 * and a seq column, keyed by name, with an index on version and seq, so entries can be read in that order without
	 * sorting.
	 *
	 * @param table     The name of the table to create for variables
	 * @param metaTable The name of the table to create for metadata
	 * @param logTable  The name of the table to create for the change log, if monitoring
	 * @return The list of statements to execute.
	 */
	protected abstract List<String> schemaSql(String table, String metaTable, String logTable);

	/**
	 * Executes {@link #schemaSql} statements and checks if the required columns exist.
	 *
	 * @param connection The connection to use to send the statements.
	 * @throws SQLException If anything goes wrong with the schema statements
	 * @throws IOException  If the resulting tables do not have the required columns, or otherwise have issues.
	 */
	private void createSchema(Connection connection) throws SQLException, IOException {
		try (Statement statement = connection.createStatement()) {
			for (String sql : schemaSql(table, metaTable, logTable))
				statement.execute(sql);
			checkColumns(statement, table, "name, type, value");
			if (monitorChanges)
				checkColumns(statement, logTable, "name, version, seq");
		}
		connection.commit();
	}

	/**
	 * @throws IOException If the table can't be read with those columns
	 */
	private void checkColumns(Statement statement, String table, String columns) throws IOException {
		try {
			statement.executeQuery("SELECT " + columns + " FROM " + table + " WHERE 1 = 0").close();
		} catch (SQLException e) {
			throw new IOException("The table '" + table + "' already exists but can't be read by this version of Skript. "
				+ "Pick another 'table' for the database '" + getName() + "'", e);
		}
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

	/**
	 * @return The version the change log has been pruned below: entries of earlier versions are gone
	 */
	private long readPrunedBelow(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement();
		     ResultSet result = statement.executeQuery("SELECT version FROM " + metaTable + " WHERE id = 1")) {
			return result.next() ? result.getLong(1) : 0; // the schema adds the row, so it's only missing if removed by hand
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
			long version;
			try {
				// bumping the counter locks its row until we commit, so other servers' saves take their versions after ours
				// and commit after us too. That way versions are committed in order, and polling for newer ones never skips one.
				version = nextVersion(connection);
				// make the changes, clears first, and log them if monitoring
				try (RowWriter rows = new RowWriter(connection, table, count -> upsertSql(table, VALUE_COLUMNS, count),
					rowsPerStatement(), maxStatementBytes(),
					monitorChanges ? count -> upsertSql(logTable, LOG_COLUMNS, count) : null, version)) {
					clear(connection, rows, batch.clears());
					write(rows, batch.changes());
				}
				// a whole batch is committed at once to prevent data fracturing.
				// if it fails, we can just try it again next time.
				connection.commit();
				noticeOtherWriters(version);
			} catch (SQLException | RuntimeException e) {
				rollbackQuietly(connection);
				throw e;
			}
			if (monitorChanges) {
				// committed; still under the lock, so no poll reads in between. If the commit threw but went through anyway,
				// the version isn't noted, and the next poll reports this save's changes back, which only repeats their values
				ownVersions.addLast(version);
				pruneIfDue(connection, version); // its own transaction, after this one, so it holds up no other server's save
			}
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
	 * but this one doesn't log its changes, so the others never see them, and this one never sees theirs.
	 * Costs nothing: the version is read by every save anyway.
	 */
	private void noticeOtherWriters(long version) {
		long previous = lastSavedVersion;
		lastSavedVersion = version;
		if (monitorChanges || warnedOtherWriters || version == previous + 1)
			return;
		warnedOtherWriters = true;
		Skript.warning("Another server also saves variables to the table '" + table + "' of the database '" + getName()
			+ "', but this server doesn't monitor changes. It doesn't log its changes, so the other servers won't see them, "
			+ "and it won't see theirs. Turn on 'monitor changes' for every server that uses this table");
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
	 * Deletes the rows of the cleared lists, and logs each clear.
	 * We use name >= and < to clear a full subset, e.g. anything between "a::" and "a:;" covers the entire "a::*" list.
	 *
	 * @param connection The connection to use.
	 * @param rows       Where to log the clears.
	 * @param prefixes   The prefixes to clear, e.g. `"a::"` for a clear meant to hit `{a::*}`.
	 * @throws SQLException If the delete fails.
	 */
	private void clear(Connection connection, RowWriter rows, List<String> prefixes) throws SQLException {
		if (prefixes.isEmpty())
			return;
		try (PreparedStatement statement = connection.prepareStatement(
			"DELETE FROM " + table + " WHERE name >= ? AND name < ?")) {
			for (String prefix : prefixes) { // in order
				statement.setString(1, prefix);
				statement.setString(2, CsvFormat.prefixEnd(prefix));
				statement.addBatch();
				rows.cleared(prefix + "*");
			}
			statement.executeBatch();
		}
	}

	/**
	 * Write new variable changes to the database. Deleted variables have their rows removed.
	 * A batch has one change per name, so the deletions and the other changes can be sent separately.
	 *
	 * @param rows    Where to write the changes.
	 * @param changes The variables to update.
	 * @throws SQLException If any of the statements fail.
	 */
	private void write(RowWriter rows, Collection<SerializedVariable> changes) throws SQLException {
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
			if (change.value == null) {
				rows.delete(change.name);
			} else {
				rows.upsert(change.name, change.value);
			}
		}
	}

	private static final List<String> VALUE_COLUMNS = List.of("name", "type", "value");
	private static final List<String> LOG_COLUMNS = List.of("name", "version", "seq");

	/**
	 * Makes the statement that saves rows to the main table, as {@code name, type, value}, or to the change log, as
	 * {@code name, version, seq}. Either way, the first column is the key, {@code name}.
	 *
	 * @param table   The table to update
	 * @param columns The columns, in the order each row binds them, the key first
	 * @param rows    How many rows the statement inserts
	 * @return An insert of that many rows that, for a name that already has a row, sets that row's other columns instead
	 */
	protected abstract String upsertSql(String table, List<String> columns, int rows);

	/**
	 * @return {@code rows} groups of {@code columns} parameters, such as {@code (?, ?, ?), (?, ?, ?)}
	 */
	protected static String upsertValues(int columns, int rows) {
		String group = "(" + "?, ".repeat(columns - 1) + "?)";
		StringBuilder values = new StringBuilder(rows * (group.length() + 2));
		for (int i = 0; i < rows; i++)
			values.append(i == 0 ? "" : ", ").append(group);
		return values.toString();
	}

	/**
	 * @return How many rows one upsert, delete by name, or log upsert may cover: {@value #ROWS_PER_STATEMENT}, which keeps
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
		monitoring = true;
	}

	/**
	 * Polls for other servers' changes, once every change drained so far has been saved: the cycle's batch was
	 * committed before the log is read, so anything read is either another server's change after it, or
	 * an entry for a name whose value this server saved itself.
	 */
	@Override
	protected final void saveCycleEnded() {
		if (monitoring && isReceiving())
			poll();
	}

	/**
	 * Holds off the next drain until the main thread has applied what the last poll reported, so no local change is
	 * drained, and so stops blocking a report, before a report older than it is applied. Logs, once per wait, if the
	 * main thread takes more than {@link #SLOW_REPORTS_NANOS} to catch up, since saving waits for it.
	 */
	@Override
	protected final boolean mayDrain() {
		if (reportsApplied || !monitoring || !isReceiving()) { // once stopped, nothing will mark them applied
			waitingSinceNanos = 0;
			loggedSlowReports = false;
			return true;
		}
		long now = System.nanoTime();
		if (waitingSinceNanos == 0) {
			waitingSinceNanos = now;
		} else if (!loggedSlowReports && now - waitingSinceNanos > SLOW_REPORTS_NANOS) {
			loggedSlowReports = true;
			Skript.debug("Saving to the database '" + getName() + "' has waited " + TimeUnit.NANOSECONDS.toSeconds(now - waitingSinceNanos)
				+ " seconds for the main thread to apply changes from other servers");
		}
		return false;
	}

	/**
	 * Reports the changes other servers saved since the last poll. The log entries are read in one go, in the order they
	 * were saved, each with its name's current value, and each name is reported once, where its last entry was: what's
	 * reported is the value it has after everything read, so its earlier entries add nothing, and any clear of its list
	 * after them is still reported after it. Entries for a list are reported as clears. Nothing is reported unless the
	 * whole read succeeds, and then the next drain waits until the main thread has applied it.
	 * Package-private, so tests can poll when they choose instead of waiting for a save cycle.
	 */
	@VisibleForTesting
	void poll() {
		if (!monitoring)
			return; // closed: don't open a new connection
		long after = pollCursor;
		Map<String, SerializedVariable.@Nullable Value> changes = new LinkedHashMap<>();
		long target, prunedBelow;
		try {
			// one transaction, so the entries and values are read from the same snapshot as the counter: every version up
			// to it has committed, since saves commit in counter order, and anything later is left for the next poll
			long[] read = withConnection(connection -> {
				long latest = readVersion(connection);
				long pruned = readPrunedBelow(connection);
				if (latest > after)
					readLog(connection, after, latest, changes);
				return new long[] {latest, pruned};
			});
			target = read[0];
			prunedBelow = read[1];
		} catch (SQLException e) {
			pollFailed(e);
			return; // the cursor hasn't moved, so the next poll reads the same entries again
		}
		if (!monitoring)
			return; // stopping: leave the cursor, nothing reported now is applied anyway
		pollCursor = target;
		pollSucceeded();
		if (prunedBelow > after + 1 && !reportedBehind) {
			// versions after the cursor and below the mark were pruned before this server read them. What's left is still
			// reported, since it's newer than what was missed
			reportedBehind = true;
			Skript.error("Changes that other servers saved to the database '" + getName() + "' were removed from its change "
				+ "log before this server read them, since it fell over 5 minutes behind, so some of them were missed. "
				+ "Restart the server to reload its variables");
		}
		if (changes.isEmpty())
			return;
		reportsApplied = false; // before the task that sets it again is queued
		changes.forEach((name, value) -> {
			reportCount.incrementAndGet();
			// KEEP_LOCAL, since a pending local change is newer than what was read, and will be saved after it
			if (name.endsWith("::*")) {
				reportCleared(name, ReportPolicy.KEEP_LOCAL);
			} else {
				reportUpdated(name, value, ReportPolicy.KEEP_LOCAL);
			}
		});
		runAfterReports(() -> reportsApplied = true); // behind the reports, so after they're applied
	}

	/**
	 * Reads the log entries other servers saved after version {@code after}, up to {@code target}, into {@code changes}:
	 * each name with its current value, null for a deleted variable or a cleared list, in the order of its last entry.
	 * <p>
	 * This server's own entries are left out by their versions. If every version in the range is this server's,
	 * nothing is read at all.
	 *
	 * @param after  The last version already reported
	 * @param target The counter, read in the same transaction, so saves still being committed are left for the next poll
	 */
	private void readLog(Connection connection, long after, long target,
	                     Map<String, SerializedVariable.@Nullable Value> changes) throws SQLException {
		while (!ownVersions.isEmpty() && ownVersions.peekFirst() <= after)
			ownVersions.pollFirst(); // already passed
		StringJoiner own = new StringJoiner(", ", " AND l.version NOT IN (", ")");
		int owned = 0;
		for (long version : ownVersions) {
			if (version > target || owned == MAX_EXCLUDED_VERSIONS)
				break; // any left in are read back, which only repeats values memory has
			own.add(Long.toString(version)); // numbers this server made, so they're safe in the statement
			owned++;
		}
		if (owned == target - after)
			return; // every version in the range is this server's
		// in index order, so the database reads the entries in order without sorting them
		try (PreparedStatement statement = connection.prepareStatement("SELECT l.name, t.type, t.value FROM " + logTable
			+ " l LEFT JOIN " + table + " t ON t.name = l.name WHERE l.version > ? AND l.version <= ?"
			+ (owned == 0 ? "" : own.toString()) + " ORDER BY l.version, l.seq")) {
			statement.setFetchSize(loadFetchSize());
			statement.setLong(1, after);
			statement.setLong(2, target);
			try (ResultSet result = statement.executeQuery()) {
				while (monitoring && result.next()) {
					String name = result.getString(1);
					String type = result.getString(2);
					byte[] data = result.getBytes(3);
					if ((type == null) != (data == null))
						continue; // a type needs a value: a broken row, so skip it
					// a list's entry is a clear, and has no row; so does a deleted variable
					SerializedVariable.Value value = type == null ? null : new SerializedVariable.Value(type, data);
					changes.remove(name); // so it's put back at its last entry
					changes.put(name, value);
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
				+ e.getMessage() + ". Retrying after every save");
		}
	}

	private void pollSucceeded() {
		if (pollFailing) {
			pollFailing = false;
			Skript.info("Checking the database '" + getName() + "' for changes works again");
		}
	}

	/**
	 * @return How many changes this storage has reported so far, clears included
	 */
	@VisibleForTesting
	long reportCount() {
		return reportCount.get();
	}

	// ---------------- PRUNING ------------------

	/**
	 * Removes the log entries that are over 5 minutes old, about every minute; see {@link LogPruner}. Runs in its own
	 * transactions after a save commits, so a slow prune doesn't hold the counter's lock, which every other server's save
	 * waits for. A failure only warns, since the save went through, and the next prune tries again.
	 *
	 * @param connection The connection to use, with no transaction open
	 * @param version    The save's version
	 */
	private void pruneIfDue(Connection connection, long version) {
		long now = System.nanoTime();
		pruner.sample(now, version);
		long cutoff = pruner.dueCutoff(now);
		if (cutoff == LogPruner.NOT_DUE)
			return;
		try {
			prune(connection, cutoff);
		} catch (SQLException e) {
			rollbackQuietly(connection);
			dropConnection();
			Skript.warning("Couldn't remove old entries from the change log '" + logTable + "' of the database '"
				+ getName() + "': " + e.getMessage() + ". Trying again in a minute");
		}
	}

	/**
	 * Notes that the log is pruned below {@code cutoff}, then removes the log entries with versions below it,
	 * {@value #PRUNE_VERSIONS} versions per statement, each committed on its own, so no transaction holds many locks.
	 * The note is committed first, so a poll that finds entries gone always finds the note too. It's in the meta table's
	 * second row, not the counter's, since every save locks that one. The entries are indexed by version, so each
	 * statement only reads what it deletes, at the oldest end of the index, away from where saves add theirs.
	 */
	private void prune(Connection connection, long cutoff) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
			"UPDATE " + metaTable + " SET version = ? WHERE id = 1 AND version < ?")) {
			statement.setLong(1, cutoff);
			statement.setLong(2, cutoff); // another server may have pruned further already
			statement.executeUpdate();
		}
		connection.commit();
		long oldest;
		try (Statement statement = connection.createStatement();
		     ResultSet result = statement.executeQuery("SELECT MIN(version) FROM " + logTable)) {
			result.next();
			oldest = result.getLong(1);
			if (result.wasNull()) {
				connection.commit();
				return; // nothing logged
			}
		}
		try (PreparedStatement statement = connection.prepareStatement(
			"DELETE FROM " + logTable + " WHERE version >= ? AND version < ?")) {
			for (long from = oldest; from < cutoff; from += PRUNE_VERSIONS) {
				statement.setLong(1, from);
				statement.setLong(2, Math.min(from + PRUNE_VERSIONS, cutoff));
				statement.executeUpdate();
				connection.commit();
			}
		}
		connection.commit();
	}

	/**
	 * Replaces the pruner, so tests can choose its times.
	 */
	@VisibleForTesting
	void pruner(LogPruner pruner) {
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
	 * Stops monitoring, closes the connection and frees this storage's table, so that another storage can use it.
	 * Called by Skript on shutdown and after a failed load, once saving has stopped.
	 */
	@Override
	public final void close() {
		monitoring = false;
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
