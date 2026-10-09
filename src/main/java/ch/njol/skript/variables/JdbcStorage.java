package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Pattern;

/**
 * Storage for Skript variables that uses SQL database.
 * <p>
 * This class is abstract and should be extended to implement specific SQL database storage.
 * <p>
 * This implementation does not synchronize the variables loaded on server with variables
 * from the connected database; it does not update with each transaction. It is efficient
 * local alternative for implementations such as {@link FlatFileStorage}.
 * <p>
 * The default implementation is SQLite/Postgres syntax, but implementations are expected
 * to override methods for supplying queries:
 * <ul>
 *     <li>{@link #createTableQuery()}</li>
 *     <li>{@link #readSingleQuery(Connection)}</li>
 *     <li>{@link #readListQuery(Connection)}</li>
 *     <li>{@link #writeSingleQuery(Connection)}</li>
 *     <li>{@link #writeMultipleQuery(Connection)}</li>
 *     <li>{@link #deleteSingleQuery(Connection)}</li>
 *     <li>{@link #deleteListQuery(Connection)}</li>
 * </ul>
 */
public abstract class JdbcStorage extends VariableStorage {

	private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,63}");

	protected static final String DEFAULT_TABLE_NAME = "skript_vars";

	public static final int MAX_VARIABLE_NAME_LENGTH = 380; // MySQL: 767 bytes max; cannot set max bytes, only max characters
	public static final int MAX_CLASS_CODENAME_LENGTH = 50; // checked when registering a class
	public static final int MAX_VALUE_SIZE = 10000;

	/**
	 * Name of the table where variables are being saved.
	 */
	protected String table = DEFAULT_TABLE_NAME;
	protected boolean legacyGuidColumn;

	/**
	 * Database source.
	 */
	protected @Nullable HikariDataSource database;

	/**
	 * The amount of variable changes written since the last full save.
	 *
	 * @see #requiredChangesForResave
	 */
	private final AtomicInteger changes = new AtomicInteger(0);

	/**
	 * Whether the storage is being saved now (written to a file).
	 */
	private final AtomicBoolean isSaving = new AtomicBoolean(false);

	/**
	 * Variables currently loaded in memory.
	 * <p>
	 * This map contains currently loaded variables by this storage.
	 * Once variable is loaded (either from database or set), it stays in this
	 * map until the storage is closed.
	 */
	private final VariablesMap variablesMap = new VariablesMap();

	/**
	 * Variables that have been modified since the last save (write buffer).
	 */
	private volatile VariablesMap dirty = new VariablesMap();

	private volatile @Nullable VariablesMap pendingDirty;
	private @Nullable VariablesMap pendingCleared;
	private final java.util.concurrent.locks.ReentrantLock saveLock = new java.util.concurrent.locks.ReentrantLock();

	/**
	 * Variables/Branches that have been deleted since the last save.
	 * <p>
	 * All objects in this map are of type {@link Marker}.
	 */
	private volatile VariablesMap cleared = new VariablesMap();

	/**
	 * Variables/Branches that have been loaded from the database to
	 * the {@link #variablesMap}.
	 * <p>
	 * All objects in this map are of type {@link Marker}.
	 */
	private final VariablesMap loaded = new VariablesMap();

	/**
	 * Represents a marker in a variables map.
	 */
	private static final class Marker {

		/**
		 * Whether this marker applies to the single variable value, e.g.: ({@code {this::node}}).
		 */
		volatile boolean single;

		/**
		 * Whether this marker applies to the variable children ({@code {this::node::*}}).
		 */
		volatile boolean branch;

		Marker() {
			this(false, false);
		}

		Marker(boolean single, boolean branch) {
			this.single = single;
			this.branch = branch;
		}

	}

	/**
	 * Executor used for scheduling the storage save.
	 */
	private final ExecutorService saveExecutor;

	/**
	 * Task for saving variables into the file.
	 */
	private @Nullable Task saveTask;

	/**
	 * Whether the storage has been closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Lock for synchronization of writing loaded variables into
	 * the database and disposing them to free heap.
	 */
	private final StampedLock lock = new StampedLock();

	protected JdbcStorage(SkriptAddon source, String type) {
		super(source, type);
		saveExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r, "JdbcStorage-Variable-Save-" + source.name() + "-" + type);
			thread.setDaemon(false); // finish save on shutdown
			return thread;
		});
	}

	/**
	 * Build a HikariConfig from the Skript config.sk SectionNode of this database.
	 *
	 * @param sectionNode The configuration section from the config.sk that defines this database.
	 * @return A HikariConfig implementation. Or null if failure.
	 */
	protected abstract @Nullable HikariConfig configuration(SectionNode sectionNode);
	/**
	 * @return SQL query to create the variables table if it does not exist
	 * <br><b>Required Columns:</b>
	 * <ul>
	 * <li><code>name</code>: Primary Key (Varchar/Text)</li>
	 * <li><code>type</code>: The serialization type (Varchar/Text)</li>
	 * <li><code>value</code>: The binary data (Blob)</li>
	 * </ul>
	 */
	// language=SQL
	protected String createTableQuery() {
		return "CREATE TABLE IF NOT EXISTS " + table + " (" +
			"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  PRIMARY KEY," +
			"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
			"value        BLOB(" + MAX_VALUE_SIZE + ")" +
			");";
	}

	/**
	 * @param connection connnection
	 * @return The SQL query to select a single variable's type and value by its name.
	 * <br>Expected Params: <code>name</code> (String)
	 */
	protected PreparedStatement readSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("SELECT type, value FROM " + table + " WHERE name = ?");
	}

	/**
	 * @param connection connnection
	 * @return The SQL query to select all variables (name, type, value) that start with a specific prefix.
	 * <br>Expected Params: <code>name_prefix%</code> (String) - usually used with LIKE
	 */
	protected PreparedStatement readListQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("SELECT `name`, `type`, `value` FROM " + table + " WHERE name LIKE ? ESCAPE '!'");
	}

	/**
	 * @param connection connnection
	 * @return The SQL query to insert or update (upsert) a single variable.
	 * <br>Expected Params: <code>name</code>, <code>type</code>, <code>value</code>
	 */
	protected PreparedStatement writeSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("INSERT INTO " + table + " (name, type, value" + legacyColumns()
			+ ") VALUES (?, ?, ?" + legacyValue() + ") ON CONFLICT(name) DO UPDATE SET "
			+ "type=excluded.type, value=excluded.value" + (legacyGuidColumn ? ", update_guid=excluded.update_guid" : ""));
	}

	/**
	 * @param connection connnection
	 * @return The SQL query used for JDBC batch writes.
	 * <br>Usually identical to {@link #writeSingleQuery(Connection)}
	 * <br>Expected Params: <code>name</code>, <code>type</code>, <code>value</code>
	 */
	protected PreparedStatement writeMultipleQuery(Connection connection) throws SQLException {
		return writeSingleQuery(connection);
	}

	/**
	 * @param connection connnection
	 * @return The SQL query to delete a single variable by name.
	 * <br>Expected Params: <code>name</code>
	 */
	protected PreparedStatement deleteSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("DELETE FROM " + table + " WHERE name = ?");
	}

	/**
	 * @param connection connnection
	 * @return The SQL query to delete multiple variables matching a prefix (list deletion).
	 * <br>Expected Params: <code>name_prefix%</code> (String) - usually used with LIKE
	 */
	protected PreparedStatement deleteListQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("DELETE FROM " + table + " WHERE name LIKE ? ESCAPE '!'");
	}

	protected final String legacyColumns() {
		return legacyGuidColumn ? ", update_guid" : "";
	}

	protected final String legacyValue() {
		return legacyGuidColumn ? ", '" + java.util.UUID.randomUUID() + "'" : "";
	}

	protected static String escapePrefix(String prefix) {
		return prefix.replace("!", "!!").replace("%", "!%").replace("_", "!_");
	}

	@Override
	protected boolean loadAbstract(SectionNode sectionNode) {
		table = sectionNode.get("table", DEFAULT_TABLE_NAME);
		if (!TABLE_NAME_PATTERN.matcher(table).matches()) {
			Skript.error("Invalid database table name: " + table);
			return false;
		}
		HikariConfig configuration = configuration(sectionNode);
		if (configuration == null)
			return false;

		SkriptLogger.setNode(null);

		try {
			database = new HikariDataSource(configuration);
		} catch (Exception exception) {
			Skript.error("Cannot connect to the database '" + getUserConfigurationName()
				+ "'! Please make sure that all settings are correct: " + exception.getLocalizedMessage());
			return false;
		}

		if (database.isClosed()) {
			Skript.error("Cannot connect to the database '" + getUserConfigurationName() + "'! Please make sure "
				+ "that all settings are correct.");
			return false;
		}

		// Create the table.
		try {
			try (Connection connection = database.getConnection()) {
				try (Statement statement = connection.createStatement()) {
					//noinspection SqlSourceToSinkFlow
					statement.execute(createTableQuery());
					try (ResultSet columns = statement.executeQuery("SELECT * FROM " + table + " WHERE 1 = 0")) {
						ResultSetMetaData metadata = columns.getMetaData();
						for (int i = 1; i <= metadata.getColumnCount(); i++) {
							if (metadata.getColumnName(i).equalsIgnoreCase("update_guid"))
								legacyGuidColumn = true;
						}
					}
				}
			}
		} catch (SQLException e) {
			Skript.error("Could not create the variables table '" + table + "' in the database '"
				+ getUserConfigurationName() + "': " + e.getLocalizedMessage());
			return false;
		}

		saveTask = new Task(Skript.getInstance(), saveTaskDelay, saveTaskPeriod, true) {
			@Override
			public void run() {
				if (changes.get() > 0 || pendingDirty != null || isBackupDue())
					saveAsync();
			}
		};

		return load(sectionNode);
	}

	@Override
	protected boolean load(SectionNode sectionNode) {
		return true;
	}

	@Override
	public void setVariable(String name, @Nullable Object value) {
		if (name.length() > MAX_VARIABLE_NAME_LENGTH) {
			Skript.error("Failed to set variable '" + name + "' due to it exceeding the max name length");
			return;
		}

		long stamp = lock.writeLock();
		try {
			boolean list = name.endsWith(Variable.SEPARATOR + "*");
			String markName = list ? name.substring(0, name.length() - 3) : name;
			Marker mark = (Marker) loaded.computeIfAbsent(markName, key -> new Marker());
			if (list)
				mark.branch = true;
			else
				mark.single = true;
			// update the read variables map
			variablesMap.setVariable(name, value);
			markBackupChanged();
			// update the writes variables map
			dirty.setVariable(name, value);

			// value is cleared, update the cleared variables map
			if (value == null) {
				boolean isList = name.endsWith(Variable.SEPARATOR + "*");
				if (isList) {
					// we clear parent; we can remove information about individual child clears
					cleared.setVariable(name, null);
					String parent = name.substring(0, name.length() - (Variable.SEPARATOR.length() + 1));
					Marker marker = (Marker) cleared.computeIfAbsent(parent, k -> new Marker());
					marker.branch = true;
				} else {
					// check if parent is already cleared; if so, no need to mark individual child
					String[] parts = Variables.splitVariableName(name);
					StringBuilder buffer = new StringBuilder();
					boolean parentCleared = false;
					for (int i = 0; i < parts.length - 1 /* we do not check self, only parents */; i++) {
						if (i > 0)
							buffer.append(Variable.SEPARATOR);
						buffer.append(parts[i]);
						var found = cleared.getVariable(buffer.toString());
						if (found instanceof Marker marker && marker.branch) {
							parentCleared = true;
							break;
						}
					}
					if (!parentCleared) { // no parent cleared, we clear the single variable
						Marker marker = (Marker) cleared.computeIfAbsent(name, k -> new Marker());
						marker.single = true;
					}
				}
			}
		} finally {
			lock.unlockWrite(stamp);
		}

		if (changes.incrementAndGet() >= requiredChangesForResave)
			saveAsync();
	}

	@Override
	@SuppressWarnings("OptionalAssignedToNull")
	public @Nullable Object getVariable(String name) {
		if (name.length() > MAX_VARIABLE_NAME_LENGTH) {
			Skript.error("Failed to get variable '" + name + "' due to it exceeding the max name length");
			return null;
		}

		Optional<Object> got = null;
		long stamp = lock.tryOptimisticRead();
		if (stamp != 0)
			got = getLoadedVariable(name);

		if (!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				got = getLoadedVariable(name);
			} finally {
				lock.unlockRead(stamp);
			}
		}

		if (got != null)
			return got.orElse(null);

		// Fetch and deserialize without holding a lock needed by the main thread.
		return loadFromDatabase(name);
	}

	/**
	 * Returns whether variable (single or list) was already loaded from the
	 * database connection in the past.
	 *
	 * @param name name of the variable
	 * @return whether it has already been loaded
	 */
	private boolean hasBeenLoaded(String name) {
		boolean isList = name.endsWith(Variable.SEPARATOR + "*");

		// single variable that is not set
		if (!isList && loaded.getVariable(name) instanceof Marker marker && marker.single)
			return true;

		// check if any parent list was loaded
		String[] parts = Variables.splitVariableName(name);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < parts.length - 1 /* we do not check self, only parents */; i++) {
			if (i > 0)
				buffer.append(Variable.SEPARATOR);
			buffer.append(parts[i]);
			var found = loaded.getVariable(buffer.toString());
			if (found instanceof Marker marker && marker.branch) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns value of a already loaded variable.
	 * <p>
	 * If the variable has been loaded from database before it will
	 * be returned as an optional (empty if it has no value set).
	 * If not {@code null} is returned.
	 *
	 * @param name name of the variable (single or list)
	 * @return variable value, empty if not set, {@code null} if not loaded
	 */
	private @Nullable Optional<Object> getLoadedVariable(String name) {
		Object value = variablesMap.getVariable(name);
		if (value != null && (!name.endsWith(Variable.SEPARATOR + "*") || hasBeenLoaded(name)))
			return Optional.of(value);
		//noinspection OptionalAssignedToNull
		return hasBeenLoaded(name) ? Optional.empty() : null;
	}

	/**
	 * Loads variable from a database (both single and list).
	 * <p>
	 * Is blocking if the deserialization of some values must by synchronized
	 * on the main thread.
	 *
	 * @param name name of the variable to load
	 * @return its value
	 */
	@Blocking
	private @Nullable Object loadFromDatabase(String name) {
		if (database == null || database.isClosed() || closed.get())
			return null;

		boolean isList = name.endsWith(Variable.SEPARATOR + "*");
		String param = isList ? escapePrefix(name.substring(0, name.length() - 1)) + "%" : name;

		Set<SerializedVariable> records = new HashSet<>();
		try (Connection conn = database.getConnection();
			 PreparedStatement stmt = isList ? readListQuery(conn) : readSingleQuery(conn)) {
			stmt.setString(1, param);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String key = isList ? rs.getString("name") : name;
					String type = rs.getString("type");
					byte[] data = rs.getBytes("value");
					if (type != null && data != null)
						records.add(new SerializedVariable(key, type, data));
				}
			}
		} catch (SQLException exception) {
			Skript.error("Error loading variable '" + name + "': " + exception.getLocalizedMessage());
			return null;
		}
		Map<String, Object> decoded = Classes.deserialize(records);
		if (decoded == null)
			return null;
		long stamp = lock.writeLock();
		try {
			// A write or delete that happened during the query takes precedence.
			if (!hasBeenLoaded(name)) {
				decoded.forEach((key, value) -> {
					if (!hasBeenLoaded(key))
						variablesMap.setVariable(key, value);
				});
				String actualName = isList ? name.substring(0, name.length() - 3) : name;
				Marker marker = (Marker) loaded.computeIfAbsent(actualName, key -> new Marker());
				if (isList)
					marker.branch = true;
				else
					marker.single = true;
			}
			return variablesMap.getVariable(name);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/**
	 * Calls the save executor to perform the rewrite of the CSV file.
	 */
	private void saveAsync() {
		if (closed.get())
			return;
		if (isSaving.compareAndSet(false, true)) {
			saveExecutor.execute(() -> {
				try {
					performSave();
				} finally {
					isSaving.set(false);
				}
			});
		}
	}

	/**
	 * Writes the uncommited changes to the database.
	 * <p>
	 * Is blocking if the serialization of some values must by synchronized
	 * on the main thread.
	 */
	private boolean performSave() {
		if (!saveLock.tryLock())
			return false;
		try {
			if (database == null)
				return false;
			if (changes.get() == 0 && pendingDirty == null) {
				try {
					backupIfDue();
					return true;
				} catch (java.io.IOException exception) {
					Skript.exception(exception, "Cannot back up variable database");
					return false;
				}
			}

			VariablesMap snapshotDirty;
			VariablesMap snapshotCleared;

			long stamp = lock.writeLock();
			try {
				if (pendingDirty == null) {
					pendingDirty = dirty;
					pendingCleared = cleared;
				} else {
					assert pendingCleared != null;
					for (var entry : cleared.getAll().entrySet()) {
						String name = entry.getKey();
						Marker current = (Marker) entry.getValue();
						Marker pending = (Marker) pendingCleared.computeIfAbsent(name, key -> new Marker());
						if (current.single) {
							pendingDirty.setVariable(name, null);
							pending.single = true;
						}
						if (current.branch) {
							pendingDirty.setVariable(name + Variable.SEPARATOR + "*", null);
							pending.branch = true;
						}
					}
					dirty.getAll().forEach(pendingDirty::setVariable);
				}
				dirty = new VariablesMap();
				cleared = new VariablesMap();
				changes.set(0);
				snapshotDirty = pendingDirty;
				snapshotCleared = pendingCleared;
			} finally {
				lock.unlockWrite(stamp);
			}
			assert snapshotCleared != null;
			Set<SerializedVariable> serialized = Variables.serialize(snapshotDirty.getAll());
			if (serialized == null)
				return false;
			// Never commit a partial batch when a serializer failed.
			if (serialized.size() != snapshotDirty.size())
				return false;

			try (Connection conn = database.getConnection()) {
				conn.setAutoCommit(false);

				try {
					beforeSave(conn);

					// process deletions
					if (!snapshotCleared.isEmpty()) {
						try (PreparedStatement deleteSingle = deleteSingleQuery(conn);
							 PreparedStatement deleteList = deleteListQuery(conn)) {

							Map<String, Object> clears = snapshotCleared.getAll();
							for (Map.Entry<String, Object> entry : clears.entrySet()) {
								String key = entry.getKey();
								Marker marker = (Marker) entry.getValue();
								if (marker.single) {
									deleteSingle.setString(1, key);
									deleteSingle.addBatch();
								}
								if (marker.branch) {
									deleteList.setString(1, escapePrefix(key + Variable.SEPARATOR) + "%");
									deleteList.addBatch();
								}
							}
							deleteSingle.executeBatch();
							deleteList.executeBatch();
						}
					}

					// process updates
					if (!snapshotDirty.isEmpty()) {
						try (PreparedStatement upsert = writeMultipleQuery(conn)) {

							for (SerializedVariable variable : serialized) {
								if (variable.value() == null) {
									try (PreparedStatement deletion = deleteSingleQuery(conn)) {
										deletion.setString(1, variable.name());
										deletion.executeUpdate();
									}
									continue;
								}

								String name = variable.name();
								String type = variable.value().type();
								byte[] data = variable.value().data();

								if (data.length > MAX_VALUE_SIZE) {
									throw new SQLException("Variable '" + name + "' exceeds the maximum data length");
								}

								upsert.setString(1, name);
								upsert.setString(2, type);
								upsert.setBytes(3, data);
								upsert.addBatch();
							}
							upsert.executeBatch();
						}
					}

					conn.commit();
				} catch (SQLException | RuntimeException exception) {
					try {
						conn.rollback();
					} catch (SQLException rollbackException) {
						exception.addSuppressed(rollbackException);
					}
					throw exception;
				}
				pendingDirty = null;
				pendingCleared = null;
				afterSave(conn);
			} catch (SQLException exception) {
				Skript.error("Failed to save variables to database: " + exception.getLocalizedMessage());
				return false;
			}
			// Release the save connection before the backup borrows one (SQLite's pool has one).
			try {
				backupIfDue();
			} catch (java.io.IOException exception) {
				Skript.error("Variables saved, but database backup failed: " + exception.getLocalizedMessage());
			}
			return true;
		} finally {
			saveLock.unlock();
		}
	}

	/**
	 * Runs before the variable changes save.
	 * <p>
	 * This is already a part of the transaction that saves the variable values
	 * into the database.
	 *
	 * @param conn connection
	 */
	protected void beforeSave(Connection conn) throws SQLException {
	}

	/**
	 * Runs after the variables are saved into memory.
	 * <p>
	 * This is after the transaction for the variable save has been committed.
	 *
	 * @param conn connection
	 */
	protected void afterSave(Connection conn) throws SQLException {
	}

	@Override
	public final void close() {
		if (!closed.compareAndSet(false, true))
			return;
		try {
			if (saveTask != null) {
				saveTask.cancel();
				saveTask = null;
			}
			// it can not finish the save anyway because Skript is disabled and
			// serialization will fail off main thread as it can not schedule
			// tasks to serialize such variables
			// we shutdown safely to avoid data corruption
			saveExecutor.shutdown();

			try {
				if (!saveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
					Skript.warning("Variable save thread took too long to shutdown. Final save might fail.");
					saveExecutor.shutdownNow();
					if (!saveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
						Skript.error("Variable save thread failed to shut down!");
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				saveExecutor.shutdownNow();
			}

			if (database != null) {
				Skript.info("Performing final variable save for '" + getUserConfigurationName() + "'");
				flush();
				Skript.info("Closing the database '" + getUserConfigurationName() + "'");
				try {
					closeDatabase();
				} catch (SQLException exception) {
					Skript.exception(exception, "Failed to close the database '" + getUserConfigurationName() + "'");
				}
			}
		} finally {
			releaseFile();
		}
	}

	/**
	 * Called when closing the database after Skript shutdown.
	 * <p>
	 * This method must close the database source.
	 */
	protected void closeDatabase() throws SQLException {
		if (database != null && !database.isClosed()) {
			database.close();
		}
	}

	@Override
	public boolean flush() {
		if (!performSave())
			return false;
		return performSave();
	}

	@Override
	public Set<String> getStoredVariableNames() throws java.io.IOException {
		Set<String> names = new HashSet<>();
		if (database == null)
			return names;
		try (Connection conn = database.getConnection(); Statement stmt = conn.createStatement();
			 ResultSet rows = stmt.executeQuery("SELECT name FROM " + table)) {
			while (rows.next())
				names.add(rows.getString(1));
		} catch (SQLException exception) {
			throw new java.io.IOException("Cannot enumerate stored variables", exception);
		}
		return names;
	}

	@Override
	protected void writeBackup(java.nio.file.Path target) throws java.io.IOException {
		if (database == null)
			throw new java.io.IOException("Database unavailable");
		try (Connection conn = database.getConnection(); Statement stmt = conn.createStatement();
			 ResultSet rows = stmt.executeQuery("SELECT `name`, `type`, `value` FROM " + table);
			 var output = backupOutput(target);
			 java.io.PrintWriter writer = new java.io.PrintWriter(output, false, FlatFileStorage.FILE_CHARSET)) {
			writer.println("# version: " + Skript.getVersion());
			while (rows.next()) {
				String type = rows.getString("type");
				byte[] data = rows.getBytes("value");
				if (type != null && data != null)
					FlatFileStorage.writeCSV(writer, rows.getString("name"), type, FlatFileStorage.encode(data));
			}
			if (writer.checkError())
				throw new java.io.IOException("Cannot write database backup");
			// Finish explicitly so gzip trailer errors propagate instead of being swallowed by PrintWriter.
			if (output instanceof java.util.zip.GZIPOutputStream gzip)
				gzip.finish();
		} catch (SQLException exception) {
			throw new java.io.IOException("Cannot back up database", exception);
		}
	}

	@Override
	public long loadedVariables() {
		return variablesMap.size();
	}

}
