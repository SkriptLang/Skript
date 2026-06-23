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

	protected static final String DEFAULT_TABLE_NAME = "variables21";

	public static final int MAX_VARIABLE_NAME_LENGTH = 380; // MySQL: 767 bytes max; cannot set max bytes, only max characters
	public static final int MAX_CLASS_CODENAME_LENGTH = 50; // checked when registering a class
	public static final int MAX_VALUE_SIZE = 10000;

	/**
	 * The delay for the save task.
	 */
	// TODO move to database configuration (this could be shared factory method in VariableStorage, as
	//  FlatFileStorage should also have those options)
	private static final long SAVE_TASK_DELAY = 5 * 60 * 20; // 5 minutes

	/**
	 * The period for the save task, how long (in ticks) between each save.
	 */
	// TODO move to database configuration
	private static final long SAVE_TASK_PERIOD = 5 * 60 * 20; // 5 minutes

	/**
	 * The amount of variable changes needed to save the variables into a file.
	 */
	// TODO move to database configuration
	private static final int REQUIRED_CHANGES_FOR_RESAVE = 1000;

	/**
	 * Name of the table where variables are being saved.
	 */
	// TODO move to database configuration, needs sanitization checks
	protected final String table = DEFAULT_TABLE_NAME;

	/**
	 * Database source.
	 */
	protected @Nullable HikariDataSource database;

	/**
	 * The amount of variable changes written since the last full save.
	 *
	 * @see #REQUIRED_CHANGES_FOR_RESAVE
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
		return connection.prepareStatement("SELECT name, type, value FROM " + table + " WHERE name LIKE ?");
	}

	/**
	 * @param connection connnection
	 * @return The SQL query to insert or update (upsert) a single variable.
	 * <br>Expected Params: <code>name</code>, <code>type</code>, <code>value</code>
	 */
	protected PreparedStatement writeSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement("INSERT INTO " + table + " (name, type, value) VALUES (?, ?, ?) " +
			"ON CONFLICT(name) DO UPDATE SET type=excluded.type, value=excluded.value");
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
		return connection.prepareStatement("DELETE FROM " + table + " WHERE name LIKE ?");
	}

	@Override
	protected boolean loadAbstract(SectionNode sectionNode) {
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
				Statement statement = connection.createStatement();
				//noinspection SqlSourceToSinkFlow
				statement.execute(createTableQuery());
			}
		} catch (SQLException e) {
			Skript.error("Could not create the variables table '" + table + "' in the database '"
				+ getUserConfigurationName() + "': " + e.getLocalizedMessage());
			return false;
		}

		saveTask = new Task(Skript.getInstance(), SAVE_TASK_DELAY, SAVE_TASK_PERIOD, true) {
			@Override
			public void run() {
				if (changes.get() > 0)
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

		long stamp = lock.readLock();
		try {
			// update the read variables map
			variablesMap.setVariable(name, value);
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
			lock.unlockRead(stamp);
		}

		if (changes.incrementAndGet() >= REQUIRED_CHANGES_FOR_RESAVE)
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

		// variable has not been loaded from the database yet
		stamp = lock.writeLock(); // TODO possible improvement? we lock the world here
		try {
			// check if loaded during the wait for the write lock
			if (hasBeenLoaded(name))
				return variablesMap.getVariable(name);
			// if not then load from the database
			return loadFromDatabase(name);
		} finally {
			lock.unlockWrite(stamp);
		}
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
		if (value != null)
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
		String param = isList ? name.substring(0, name.length() - 1) + "%" : name;

		Object result = null;

		try (Connection conn = database.getConnection();
			 PreparedStatement stmt = isList ? readListQuery(conn) : readSingleQuery(conn)) {

			stmt.setString(1, param);
			try (ResultSet rs = stmt.executeQuery()) {
				if (isList) {
					Set<SerializedVariable> variables = new HashSet<>();
					while (rs.next()) {
						String key = rs.getString("name");
						String type = rs.getString("type");
						byte[] data = rs.getBytes("value");
						variables.add(new SerializedVariable(key, type, data));
					}
					var deserialized = Classes.deserialize(variables);
					if (deserialized != null) {
						deserialized.forEach(variablesMap::setVariable);
						result = variablesMap.getVariable(name);
					}
				} else {
					if (rs.next()) {
						String type = rs.getString("type");
						byte[] data = rs.getBytes("value");
						result = Classes.deserialize(data, type);
						variablesMap.setVariable(name, result);
					}
				}
			}

			String actualName = isList
				? name.substring(0, name.length() - (Variable.SEPARATOR.length() + 1))
				: name;
			Marker marker = (Marker) loaded.computeIfAbsent(actualName, k -> new Marker());
			if (isList) {
				marker.branch = true;
			} else {
				marker.single = true;
			}

		} catch (SQLException exception) {
			Skript.error("Error loading variable '" + name + "': " + exception.getLocalizedMessage());
		}

		return result;
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
	private void performSave() {
		if (changes.get() == 0 || database == null)
			return;

		VariablesMap snapshotDirty;
		VariablesMap snapshotCleared;

		// swap; this essentially clears the uncommited change maps
		// TODO critical, this can cause data lost if the executor is shutdown on close
		//  as the final save will have empty maps but not everything finished saving.
		//  There must be a recover if the peformSave fails
		long stamp = lock.writeLock();
		try {
			snapshotDirty = dirty;
			snapshotCleared = cleared;

			dirty = new VariablesMap();
			cleared = new VariablesMap();
			changes.set(0);
		} finally {
			lock.unlockWrite(stamp);
		}

		if (snapshotDirty.isEmpty() && snapshotCleared.isEmpty())
			return;

		try (Connection conn = database.getConnection()) {
			conn.setAutoCommit(false);

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
							deleteList.setString(1, key + Variable.SEPARATOR + "%");
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
					Map<String, Object> updates = snapshotDirty.getAll();
					var serialized = Classes.serialize(updates);

					if (serialized == null) {
						if (Skript.debug()) {
							Skript.warning("Failed to save the variables off main thread, this may happen when Skript gets disabled.");
							Skript.warning("No data is lost, final save will run synchronously on the main thread.");
						}
						return;
					}

					for (SerializedVariable variable : serialized) {
						if (variable.value() == null)
							continue;

						String name = variable.name();
						String type = variable.value().type();
						byte[] data = variable.value().data();

						if (data.length > MAX_VALUE_SIZE) {
							Skript.error("Failed to save variable '" + name + "' due to it exceeding the max data length");
							continue;
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
			afterSave(conn);
		} catch (SQLException exception) {
			Skript.error("Failed to save variables to database: " + exception.getLocalizedMessage());
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
			performSave();
			Skript.info("Closing the database '" + getUserConfigurationName() + "'");
			try {
				closeDatabase();
			} catch (SQLException exception) {
				Skript.exception(exception, "Failed to close the database '" + getUserConfigurationName() + "'");
			}
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
	public long loadedVariables() {
		return variablesMap.size();
	}

}
