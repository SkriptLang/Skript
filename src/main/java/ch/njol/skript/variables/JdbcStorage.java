/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.variables;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.NonNullPair;
import ch.njol.util.SynchronizedReference;

public abstract class JdbcStorage extends VariableStorage {

	protected static final String DEFAULT_TABLE_NAME = "variables21";

	public static final int MAX_VARIABLE_NAME_LENGTH = 380; // MySQL: 767 bytes max; cannot set max bytes, only max characters
	public static final int MAX_CLASS_CODENAME_LENGTH = 50; // checked when registering a class
	public static final int MAX_VALUE_SIZE = 10000;

	/**
	 * Params: name, type, value
	 * <p>
	 * Writes a variable to the database
	 */
	@Nullable
	private PreparedStatement WRITE_QUERY;

	/**
	 * Params: name
	 * <p>
	 * Deletes a variable from the database
	 */
	@Nullable
	private PreparedStatement DELETE_QUERY;

	/**
	 * Params: rowID
	 * <p>
	 * Selects changed rows. values in order: {@value #SELECT_ORDER}
	 */
	@Nullable
	private PreparedStatement MONITOR_QUERY;

	/**
	 * Params: rowID
	 * <p>
	 * Deletes null variables from the database older than the given value
	 */
	@Nullable
	private PreparedStatement MONITOR_CLEAN_UP_QUERY;

	private final String createTableQuery;
	private String table;

	private final SynchronizedReference<HikariDataSource> database = new SynchronizedReference<>();

	private long monitor_interval;
	private boolean monitor;

	/**
	 * Creates a SQLStorage with a create table query.
	 * 
	 * @param name The name to be sent through this constructor when newInstance creates this class.
	 * @param createTableQuery The create table query to send to the SQL engine.
	 */
	public JdbcStorage(SkriptAddon source, String name, String createTableQuery) {
		super(source, name);
		this.createTableQuery = createTableQuery;
		this.table = "variables21";
	}

	public final String getTableName() {
		return table;
	}

	public final void setTableName(String tableName) {
		this.table = tableName;
	}

	/**
	 * Build a HikariConfig from the Skript config.sk SectionNode of this database.
	 * 
	 * @param config The configuration section from the config.sk that defines this database.
	 * @return A HikariConfig implementation. Or null if failure.
	 */
	@Nullable
	public abstract HikariConfig configuration(SectionNode config);

	/**
	 * The prepared statement for replacing with this SQL database.
	 * Format is string (name), string (type), bytes (value), string (rowid)
	 * 
	 * @return The string to be placed into a prepared statement for replacing.
	 */
	protected abstract String getReplaceQuery();

	/**
	 * The select statement that will insert 1. the last row ID, and 2. the generated uuid.
	 * So ensure this statement returns a selection for inputting those two values.
	 * Must have rowid and update_guid.
	 * 
	 * The first string will be the monitor select of the rowid and the guid, the second string
	 * will be the delete monitor query where it deletes that entry.
	 * 
	 * Return null if monitoring is disabled for this type.
	 * You only need to be monitoring when the database is external like MySQL.
	 * 
	 * @return The string to be used for selecting.
	 */
	@Nullable
	protected NonNullPair<String, String> getMonitorQueries() {
		return null;
	};

	/**
	 * Must select name, 
	 * 
	 * @return The query that will be used to select the elements.
	 */
	protected abstract String getSelectQuery();

	/**
	 * Construct a VariableResult from the SQL ResultSet based on the extending class getSelectQuery.
	 * ResultSet is the current iteration and a NonNullPair<Long, SerializedVariable> should be return, long represents the rowid.
	 * 
	 * Null if exception happened.
	 * 
	 * @param testOperation if the request is a test operation.
	 * @return a VariableResult from the SQL ResultSet based on your getSelectQuery.
	 */
	@Nullable
	protected abstract Function<@Nullable ResultSet, NonNullPair<Long, SerializedVariable>> get(boolean testOperation);

	private ResultSet query(HikariDataSource source, String query) throws SQLException {
		Statement statement = source.getConnection().createStatement();
	    if (statement.execute(query)) {
	    	return statement.getResultSet();
	    } else {
	    	int uc = statement.getUpdateCount();
	    	return source.getConnection().createStatement().executeQuery("SELECT " + uc);
	    }
	}

	private boolean prepareQueries() {
		synchronized (database) {
			HikariDataSource database = this.database.get();
			assert database != null;
			try {
				Connection connection = database.getConnection();
				try {
					if (WRITE_QUERY != null)
						WRITE_QUERY.close();
				} catch (SQLException e) {}
				WRITE_QUERY = connection.prepareStatement(getReplaceQuery());

				try {
					if (DELETE_QUERY != null)
						DELETE_QUERY.close();
				} catch (SQLException e) {}
				DELETE_QUERY = connection.prepareStatement("DELETE FROM " + getTableName() + " WHERE name = ?");

				try {
					if (MONITOR_QUERY != null)
						MONITOR_QUERY.close();
					if (MONITOR_CLEAN_UP_QUERY != null)
						MONITOR_CLEAN_UP_QUERY.close();
				} catch (SQLException e) {}
				@Nullable NonNullPair<String, String> monitorStatement = getMonitorQueries();
				if (monitorStatement != null) {
					MONITOR_QUERY = connection.prepareStatement(monitorStatement.getFirst());
					MONITOR_CLEAN_UP_QUERY = connection.prepareStatement(monitorStatement.getSecond());
				} else {
					monitor = false;
				}
			} catch (SQLException e) {
				Skript.exception(e, "Could not prepare queries for the database '" + databaseName + "': " + e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * Doesn't lock the database for reading (it's not used anywhere else, and locking while loading will interfere with loaded variables being deleted by
	 * {@link Variables#variableLoaded(String, Object, VariableStorage)}).
	 */
	@Override
	protected final boolean loadAbstract(SectionNode section) {
		synchronized (database) {
			Timespan monitor_interval = getValue(section, "monitor interval", Timespan.class);
			if (this.monitor = monitor_interval != null)
				this.monitor_interval = monitor_interval.getAs(TimePeriod.MILLISECOND);

			HikariConfig configuration = configuration(section);
			if (configuration == null)
				return false;

			Timespan commit_changes = getOptional(section, "commit changes", Timespan.class);
			if (commit_changes != null)
				enablePeriodicalCommits(configuration, commit_changes.getAs(TimePeriod.MILLISECOND));

			// Max lifetime is 30 minutes, idle lifetime is 10 minutes. This value has to be less than.
			configuration.setKeepaliveTime(TimeUnit.MINUTES.toMillis(5));

			SkriptLogger.setNode(null);

			HikariDataSource db = null;
			try {
				this.database.set(db = new HikariDataSource(configuration));
			} catch (Exception exception) { // MySQL can throw SQLSyntaxErrorException but not exposed from HikariDataSource.
				Skript.error("Cannot connect to the database '" + databaseName + "'! Please make sure that all settings are correct.");
				return false;
			}

			if (db == null || db.isClosed()) {
				Skript.error("Cannot connect to the database '" + databaseName + "'! Please make sure that all settings are correct.");
				return false;
			}
			if (createTableQuery == null || !createTableQuery.contains("%s")) {
				Skript.error("Could not create the variables table in the database. The query to create the variables table '" + table + "' in the database '" + databaseName + "' is null.");
				return false;
			}

			// Create the table.
			try {
				query(db, String.format(createTableQuery, table));
			} catch (SQLException e) {
				Skript.error("Could not create the variables table '" + table + "' in the database '" + databaseName + "': " + e.getLocalizedMessage() + ". " +
						"Please create the table yourself using the following query: " + String.format(createTableQuery, table).replace(",", ", ").replaceAll("\\s+", " "));
				return false;
			}

			// Build the queries.
			if (!prepareQueries())
				return false;

			// First loading.
			try {
				ResultSet result = query(db, getSelectQuery());
				assert result != null;
				try {
					loadVariables(result);
				} finally {
					result.close();
				}
			} catch (SQLException e) {
				sqlException(e);
				return false;
			}
			return load(section);
		}
	}

	/**
	 * Override this method to load a custom configuration reading.
	 */
	public boolean load(SectionNode n) {
		return true;
	}

	/**
	 * Doesn't lock the database
	 * {@link #save(String, String, byte[])} does that as values are inserted in the database.
	 */
	private void loadVariables(ResultSet result) throws SQLException {
		SQLException e = Task.callSync(new Callable<SQLException>() {
			@Override
			@Nullable
			public SQLException call() throws Exception {
				try {
					@Nullable Function<ResultSet, NonNullPair<Long, SerializedVariable>> handle = get(false);
					while (result.next()) {
						@Nullable NonNullPair<Long, SerializedVariable> returnPair = handle.apply(result);
						if (returnPair == null)
							continue;
						SerializedVariable variable = returnPair.getSecond();
						lastRowID = returnPair.getFirst();

						if (variable.getValue() == null) {
							Variables.variableLoaded(variable.getName(), null, JdbcStorage.this);
						} else {
							ClassInfo<?> c = Classes.getClassInfoNoError(variable.getType());
							if (c == null || c.getSerializer() == null) {
								Skript.error("Cannot load the variable {" + variable.getName() + "} from the database '" + databaseName + "', because the type '" + variable.getType() + "' cannot be recognised or cannot be stored in variables");
								continue;
							}
							Object object = Classes.deserialize(c, variable.getData());
							if (object == null) {
								Skript.error("Cannot load the variable {" + variable.getName() + "} from the database '" + databaseName + "', because it cannot be loaded as " + c.getName().withIndefiniteArticle());
								continue;
							}
							Variables.variableLoaded(variable.getName(), object, JdbcStorage.this);
						}
					}
				} catch (SQLException e) {
					return e;
				}
				return null;
			}
		});
		if (e != null)
			throw e;
	}

	private boolean committing;

	/**
	 * Start a committing thread. Use this in the {@link #configuration(SectionNode)} method.
	 * This changes the configuration from auto commiting to periodic commiting.
	 * 
	 * @param configuration The HikariConfig being represented from the SectionNode.
	 * @param delay The delay in milliseconds between transactions.
	 */
	protected void enablePeriodicalCommits(HikariConfig configuration, long delay) {
		if (committing)
			return;
		committing = true;
		configuration.setAutoCommit(false);
		Skript.newThread(() -> {
			long lastCommit = System.currentTimeMillis();
			while (!closed) {
				synchronized (database) {
					HikariDataSource database = this.database.get();
					try {
						if (database != null)
							database.getConnection().commit();
						lastCommit = System.currentTimeMillis();
					} catch (SQLException e) {
						sqlException(e);
					}
				}
				try {
					Thread.sleep(Math.max(0, lastCommit + delay - System.currentTimeMillis()));
				} catch (InterruptedException e) {}
			}
		}, "Skript database '" + databaseName + "' transaction committing thread").start();
	}

	@Override
	protected void allLoaded() {
		Skript.debug("Database " + databaseName + " loaded. Queue size = " + changesQueue.size());
		if (!monitor)
			return;
		Skript.newThread(() -> {
			try { // variables were just downloaded, no need to check for modifications straight away.
				Thread.sleep(monitor_interval);
			} catch (final InterruptedException e1) {}

			long lastWarning = Long.MIN_VALUE;
			int WARING_INTERVAL = 10;

			// Ignore printing error on first load due to possible large loading times.
			boolean ignoreFirstIteration = true;
			while (!closed) {
				long next = System.currentTimeMillis() + monitor_interval;
				checkDatabase();
				long now = System.currentTimeMillis();
				if (next < now && lastWarning + WARING_INTERVAL * 1000 < now) {
					if (ignoreFirstIteration) {
						ignoreFirstIteration = false;
					} else {
						Skript.warning("Cannot load variables from the database fast enough (loading took " + ((now - next + monitor_interval) / 1000.) + "s, monitor interval = " + (monitor_interval / 1000.) + "s). " +
								"Please increase your monitor interval or reduce usage of variables. " +
								"(this warning will be repeated at most once every " + WARING_INTERVAL + " seconds)");
						lastWarning = now;
					}
				}
				while (System.currentTimeMillis() < next) {
					try {
						Thread.sleep(next - System.currentTimeMillis());
					} catch (final InterruptedException e) {}
				}
			}
		}, "Skript database '" + databaseName + "' monitor thread").start();
	}

	@Override
	protected File getFile(String file) {
		return new File(file);
	}

	@Override
	protected boolean connect() {
		synchronized (database) {
			HikariDataSource database = this.database.get();
			if (database == null || database.isClosed()) {
				Skript.exception("Cannot reconnect to the database '" + databaseName + "'!");
				return false;
			}
			return true;
		}
	}

	@Override
	protected void disconnect() {
		synchronized (database) {
			HikariDataSource database = this.database.get();
			if (database != null)
				database.close();
		}
	}

	@Override
	public boolean save(String name, @Nullable String type, @Nullable byte[] value) {
		synchronized (database) {
			if (name.length() > MAX_VARIABLE_NAME_LENGTH)
				Skript.error("The name of the variable {" + name + "} is too long to be saved in a database (length: " + name.length() + ", maximum allowed: " + MAX_VARIABLE_NAME_LENGTH + ")! It will be truncated and won't be available under the same name again when loaded.");
			if (value != null && value.length > MAX_VALUE_SIZE)
				Skript.error("The variable {" + name + "} cannot be saved in the database as its value's size (" + value.length + ") exceeds the maximum allowed size of " + MAX_VALUE_SIZE + "! An attempt to save the variable will be made nonetheless.");
			try {
				if (type == null) {
					assert value == null;
					assert DELETE_QUERY != null;
					DELETE_QUERY.setString(1, name);
					DELETE_QUERY.executeUpdate();
				} else {
					int i = 1;
					assert WRITE_QUERY != null;
					WRITE_QUERY.setString(i++, name);
					WRITE_QUERY.setString(i++, type);
					WRITE_QUERY.setBytes(i++, value);
					WRITE_QUERY.executeUpdate();
				}
			} catch (SQLException e) {
				sqlException(e);
				return false;
			}
		}
		return true;
	}

	@Override
	public void close() {
		synchronized (database) {
			super.close();
			HikariDataSource database = this.database.get();
			if (database != null) {
				try {
					if (!database.isAutoCommit())
						database.getConnection().commit();
				} catch (SQLException e) {
					sqlException(e);
				}
				database.close();
				this.database.set(null);
			}
		}
	}

	private long lastRowID = -1;

	protected void checkDatabase() {
		if (!monitor || this.lastRowID == -1)
			return;
		try {
			long lastRowID; // local variable as this is used to clean the database below
			ResultSet result = null;
			try {
				synchronized (database) {
					if (closed || database.get() == null)
						return;
					lastRowID = this.lastRowID;
					assert MONITOR_QUERY != null;
					MONITOR_QUERY.setLong(1, lastRowID);
					MONITOR_QUERY.execute();
					result = MONITOR_QUERY.getResultSet();
					assert result != null;
					@Nullable HikariDataSource source = database.get();
					if (source != null && !source.isAutoCommit())
						source.getConnection().commit();
				}
				if (!closed)
					loadVariables(result);
			} finally {
				if (result != null)
					result.close();
			}

			if (!closed) { // Skript may have been disabled in the meantime // TODO not fixed
				new Task(Skript.getInstance(), (long) Math.ceil(2. * monitor_interval / 50) + 100, true) { // 2 times the interval + 5 seconds
					@Override
					public void run() {
						try {
							synchronized (database) {
								if (closed || database.get() == null)
									return;
								assert MONITOR_CLEAN_UP_QUERY != null;
								MONITOR_CLEAN_UP_QUERY.setLong(1, lastRowID);
								MONITOR_CLEAN_UP_QUERY.executeUpdate();
								@Nullable HikariDataSource source = database.get();
								if (source != null && !source.isAutoCommit())
									source.getConnection().commit();
							}
						} catch (SQLException e) {
							sqlException(e);
						}
					}
				};
			}
		} catch (SQLException e) {
			sqlException(e);
		}
	}

	NonNullPair<Long, SerializedVariable> executeTestQuery() throws SQLException {
		synchronized (database) {
			database.get().getConnection().commit();
		}
		ResultSet result = query(database.get(), getSelectQuery());
		return get(true).apply(result);
	}

	void sqlException(SQLException exception) {
		Skript.error("database error: " + exception.getLocalizedMessage());
		if (Skript.testing())
			exception.printStackTrace();
		prepareQueries(); // a query has to be recreated after an error
	}

}
