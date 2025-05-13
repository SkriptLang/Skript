package ch.njol.skript.variables;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.util.SynchronizedReference;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.DatabaseException;
import lib.PatPeter.SQLibrary.SQLibrary;

/**
 * TODO create a metadata table to store some properties (e.g. Skript version, Yggdrasil version) -- but what if some variables cannot be converted? move them to a different table?
 * TODO create my own database connector or find a better one
 *
 * @author Peter Güttinger
 */
public abstract class SQLStorage extends VariablesStorage {

	public final static int MAX_VARIABLE_NAME_LENGTH = 380, // MySQL: 767 bytes max; cannot set max bytes, only max characters
			MAX_CLASS_CODENAME_LENGTH = 50, // checked when registering a class
			MAX_VALUE_SIZE = 10000;

	private final static String SELECT_ORDER = "name, type, value, rowid";

	private final static String OLD_TABLE_NAME = "variables";

	@Nullable
	private String formattedCreateQuery;
	private final String createTableQuery;
	private String tableName;

	final SynchronizedReference<Database> db = new SynchronizedReference<>(null);

	private boolean monitor = false;
	long monitor_interval;

	private final static String guid = UUID.randomUUID().toString();

	/**
	 * The delay between transactions in milliseconds.
	 */
	private final static long TRANSACTION_DELAY = 500;

	/**
	 * Creates a SQLStorage with a create table query.
	 * 
	 * @param type The database type i.e. CSV.
	 * @param createTableQuery The create table query to send to the SQL engine.
	 */
	public SQLStorage(String type, String createTableQuery) {
		super(type);
		this.createTableQuery = createTableQuery;
		this.tableName = "variables21";
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Initializes an SQL database with the user provided configuration section for loading the database.
	 * 
	 * @param config The configuration from the config.sk that defines this database.
	 * @return A Database implementation from SQLibrary.
	 */
	@Nullable
	public abstract Database initialize(SectionNode config);

	/**
	 * Retrieve the create query with the tableName in it
	 * @return the create query with the tableName in it (%s -> tableName)
	 */
	@Nullable
	public String getFormattedCreateQuery() {
		if (formattedCreateQuery == null)
			formattedCreateQuery = String.format(createTableQuery, tableName);
		return formattedCreateQuery;
	}

	/**
	 * Doesn't lock the database for reading (it's not used anywhere else, and locking while loading will interfere with loaded variables being deleted by
	 * {@link Variables#variableLoaded(String, Object, VariablesStorage)}).
	 */
	@Override
	protected boolean load_i(SectionNode n) {
		synchronized (db) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin("SQLibrary");
			if (plugin == null || !(plugin instanceof SQLibrary)) {
				Skript.error("You need the plugin SQLibrary in order to use a database with Skript. You can download the latest version from https://dev.bukkit.org/projects/sqlibrary/files/");
				return false;
			}

			final Boolean monitor_changes = getValue(n, "monitor changes", Boolean.class);
			final Timespan monitor_interval = getValue(n, "monitor interval", Timespan.class);
			if (monitor_changes == null || monitor_interval == null)
				return false;
			monitor = monitor_changes;
			this.monitor_interval = monitor_interval.getAs(Timespan.TimePeriod.MILLISECOND);

			final Database db;
			try {
				Database database = initialize(n);
				if (database == null)
					return false;
				this.db.set(db = database);
			} catch (final RuntimeException e) {
				if (e instanceof DatabaseException) {// not in a catch clause to not produce a ClassNotFoundException when this class is loaded and SQLibrary is not present
					Skript.error(e.getLocalizedMessage());
					return false;
				}
				throw e;
			}

			SkriptLogger.setNode(null);

			if (!connect(true))
				return false;

			try {
				final boolean hasOldTable = db.isTable(OLD_TABLE_NAME);
				final boolean hadNewTable = db.isTable(getTableName());

				if (getFormattedCreateQuery() == null){
					Skript.error("Could not create the variables table in the database. The query to create the variables table '" + tableName + "' in the database '" + getUserConfigurationName() + "' is null.");
					return false;
				}

				try {
					db.query(getFormattedCreateQuery());
				} catch (final SQLException e) {
					Skript.error("Could not create the variables table '" + tableName + "' in the database '" + getUserConfigurationName() + "': " + e.getLocalizedMessage() + ". "
							+ "Please create the table yourself using the following query: " + String.format(createTableQuery, tableName).replace(",", ", ").replaceAll("\\s+", " "));
					return false;
				}

				if (!prepareQueries()) {
					return false;
				}
				
				// old
				// Table name support was added after the verison that used the legacy database format
				if (hasOldTable && !tableName.equals("variables")) {
					final ResultSet r1 = db.query("SELECT " + SELECT_ORDER + " FROM " + OLD_TABLE_NAME);
					assert r1 != null;
					try {
						oldLoadVariables(r1, hadNewTable);
					} finally {
						r1.close();
					}
				}

				// new
				final ResultSet r2 = db.query("SELECT " + SELECT_ORDER + " FROM " + getTableName());
				assert r2 != null;
				try {
					loadVariables(r2);
				} finally {
					r2.close();
				}

				// store old variables in new table and delete the old table
				if (hasOldTable) {
					if (!hadNewTable) {
						Skript.info("[2.1] Updating the database '" + getUserConfigurationName() + "' to the new format...");
						try {
							Variables.getReadLock().lock();
							for (final Entry<String, Object> v : Variables.getVariablesHashMap().entrySet()) {
								if (accept(v.getKey())) {// only one database was possible, so only checking this database is correct
									@SuppressWarnings("null")
									final SerializedVariable var = Variables.serialize(v.getKey(), v.getValue());
									final SerializedVariable.Value d = var.value;
									save(var.name, d == null ? null : d.type, d == null ? null : d.data);
								}
							}
							Skript.info("Updated and transferred " + Variables.getVariablesHashMap().size() + " variables to the new table.");
						} finally {
							Variables.getReadLock().unlock();
						}
					}
					db.query("DELETE FROM " + OLD_TABLE_NAME + " WHERE value IS NULL");
					db.query("DELETE FROM old USING " + OLD_TABLE_NAME + " AS old, " + getTableName() + " AS new WHERE old.name = new.name");
					final ResultSet r = db.query("SELECT * FROM " + OLD_TABLE_NAME + " LIMIT 1");
					try {
						if (r.next()) {// i.e. the old table is not empty
							Skript.error("Could not successfully convert & transfer all variables to the new table in the database '" + getUserConfigurationName() + "'. "
									+ "Variables that could not be transferred are left in the old table and Skript will reattempt to transfer them whenever it starts until the old table is empty or is manually deleted. "
									+ "Please note that variables recreated by scripts will count as converted and will be removed from the old table on the next restart.");
						} else {
							boolean error = false;
							try {
								disconnect(); // prevents SQLITE_LOCKED error
								connect();
								db.query("DROP TABLE " + OLD_TABLE_NAME);
							} catch (final SQLException e) {
								Skript.error("There was an error deleting the old variables table from the database '" + getUserConfigurationName() + "', please delete it yourself: " + e.getLocalizedMessage());
								error = true;
							}
							if (!error)
								Skript.info("Successfully deleted the old variables table from the database '" + getUserConfigurationName() + "'.");
							if (!hadNewTable)
								Skript.info("Database '" + getUserConfigurationName() + "' successfully updated.");
						}
					} finally {
						r.close();
					}
				}
			} catch (final SQLException e) {
				sqlException(e);
				return false;
			}

			// periodically executes queries to keep the collection alive
			Skript.newThread(new Runnable() {
				@Override
				public void run() {
					while (!closed) {
						synchronized (SQLStorage.this.db) {
							try {
								final Database db = SQLStorage.this.db.get();
								if (db != null)
									db.query("SELECT * FROM " + getTableName() + " LIMIT 1");
							} catch (final SQLException e) {}
						}
						try {
							Thread.sleep(1000 * 10);
						} catch (final InterruptedException e) {}
					}
				}
			}, "Skript database '" + getUserConfigurationName() + "' connection keep-alive thread").start();

			return true;
		}
	}

	@Override
	protected void allLoaded() {
		Skript.debug("Database " + getUserConfigurationName() + " loaded. Queue size = " + changesQueue.size());

		// start committing thread. Its first execution will also commit the first batch of changed variables.
		Skript.newThread(new Runnable() {
			@Override
			public void run() {
				long lastCommit;
				while (!closed) {
					synchronized (db) {
						final Database db = SQLStorage.this.db.get();
						try {
							if (db != null)
								db.getConnection().commit();
						} catch (final SQLException e) {
							sqlException(e);
						}
						lastCommit = System.currentTimeMillis();
					}
					try {
						Thread.sleep(Math.max(0, lastCommit + TRANSACTION_DELAY - System.currentTimeMillis()));
					} catch (final InterruptedException e) {}
				}
			}
		}, "Skript database '" + getUserConfigurationName() + "' transaction committing thread").start();

		if (monitor) {
			Skript.newThread(new Runnable() {
				@Override
				public void run() {
					try { // variables were just downloaded, not need to check for modifications straight away
						Thread.sleep(monitor_interval);
					} catch (final InterruptedException e1) {}

					long lastWarning = Long.MIN_VALUE;
					final int WARING_INTERVAL = 10;

					while (!closed) {
						final long next = System.currentTimeMillis() + monitor_interval;
						checkDatabase();
						final long now = System.currentTimeMillis();
						if (next < now && lastWarning + WARING_INTERVAL * 1000 < now) {
							// TODO don't print this message when Skript loads (because scripts are loaded after variables and take some time)
							Skript.warning("Cannot load variables from the database fast enough (loading took " + ((now - next + monitor_interval) / 1000.) + "s, monitor interval = " + (monitor_interval / 1000.) + "s). " +
									"Please increase your monitor interval or reduce usage of variables. " +
									"(this warning will be repeated at most once every " + WARING_INTERVAL + " seconds)");
							lastWarning = now;
						}
						while (System.currentTimeMillis() < next) {
							try {
								Thread.sleep(next - System.currentTimeMillis());
							} catch (final InterruptedException e) {}
						}
					}
				}
			}, "Skript database '" + getUserConfigurationName() + "' monitor thread").start();
		}

	}

	@Override
	protected File getFile(String file) {
		if (!file.endsWith(".db"))
			file = file + ".db"; // required by SQLibrary
		return new File(file);
	}

	@Override
	protected boolean connect() {
		return connect(false);
	}

	private final boolean connect(final boolean first) {
		synchronized (db) {
			// isConnected doesn't work in SQLite
//			if (db.isConnected())
//				return;
			final Database db = this.db.get();
			if (db == null || !db.open()) {
				if (first)
					Skript.error("Cannot connect to the database '" + getUserConfigurationName() + "'! Please make sure that all settings are correct");// + (type == Type.MYSQL ? " and that the database software is running" : "") + ".");
				else
					Skript.exception("Cannot reconnect to the database '" + getUserConfigurationName() + "'!");
				return false;
			}
			try {
				db.getConnection().setAutoCommit(false);
			} catch (final SQLException e) {
				sqlException(e);
				return false;
			}
			return true;
		}
	}

	/**
	 * (Re)creates prepared statements as they get closed as well when closing the connection
	 *
	 * @return
	 */
	private boolean prepareQueries() {
		synchronized (db) {
			final Database db = this.db.get();
			assert db != null;
			try {
				try {
					if (writeQuery != null)
						writeQuery.close();
				} catch (final SQLException e) {}
				writeQuery = db.prepare("REPLACE INTO " + getTableName() + " (name, type, value, update_guid) VALUES (?, ?, ?, ?)");

				try {
					if (deleteQuery != null)
						deleteQuery.close();
				} catch (final SQLException e) {}
				deleteQuery = db.prepare("DELETE FROM " + getTableName() + " WHERE name = ?");

				try {
					if (monitorQuery != null)
						monitorQuery.close();
				} catch (final SQLException e) {}
				monitorQuery = db.prepare("SELECT " + SELECT_ORDER + " FROM " + getTableName() + " WHERE rowid > ? AND update_guid != ?");
				try {
					if (monitorCleanUpQuery != null)
						monitorCleanUpQuery.close();
				} catch (final SQLException e) {}
				monitorCleanUpQuery = db.prepare("DELETE FROM " + getTableName() + " WHERE value IS NULL AND rowid < ?");
			} catch (final SQLException e) {
				Skript.exception(e, "Could not prepare queries for the database '" + getUserConfigurationName() + "': " + e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	@Override
	protected void disconnect() {
		synchronized (db) {
			final Database db = this.db.get();
//			if (!db.isConnected())
//				return;
			if (db != null)
				db.close();
		}
	}

	/**
	 * Params: name, type, value, GUID
	 * <p>
	 * Writes a variable to the database
	 */
	@Nullable
	private PreparedStatement writeQuery;
	/**
	 * Params: name
	 * <p>
	 * Deletes a variable from the database
	 */
	@Nullable
	private PreparedStatement deleteQuery;
	/**
	 * Params: rowID, GUID
	 * <p>
	 * Selects changed rows. values in order: {@value #SELECT_ORDER}
	 */
	@Nullable
	private PreparedStatement monitorQuery;
	/**
	 * Params: rowID
	 * <p>
	 * Deletes null variables from the database older than the given value
	 */
	@Nullable
	PreparedStatement monitorCleanUpQuery;

	@Override
	protected boolean save(final String name, final @Nullable String type, final @Nullable byte[] value) {
		synchronized (db) {
			// REMIND get the actual maximum size from the database
			if (name.length() > MAX_VARIABLE_NAME_LENGTH)
				Skript.error("The name of the variable {" + name + "} is too long to be saved in a database (length: " + name.length() + ", maximum allowed: " + MAX_VARIABLE_NAME_LENGTH + ")! It will be truncated and won't bet available under the same name again when loaded.");
			if (value != null && value.length > MAX_VALUE_SIZE)
				Skript.error("The variable {" + name + "} cannot be saved in the database as its value's size (" + value.length + ") exceeds the maximum allowed size of " + MAX_VALUE_SIZE + "! An attempt to save the variable will be made nonetheless.");
			try {
				if (type == null) {
					assert value == null;
					final PreparedStatement deleteQuery = this.deleteQuery;
					assert deleteQuery != null;
					deleteQuery.setString(1, name);
					deleteQuery.executeUpdate();
				} else {
					int i = 1;
					final PreparedStatement writeQuery = this.writeQuery;
					assert writeQuery != null;
					writeQuery.setString(i++, name);
					writeQuery.setString(i++, type);
					writeQuery.setBytes(i++, value); // SQLite desn't support setBlob
					writeQuery.setString(i++, guid);
					writeQuery.executeUpdate();
				}
			} catch (final SQLException e) {
				sqlException(e);
				return false;
			}
		}
		return true;
	}

	@Override
	public void close() {
		synchronized (db) {
			super.close();
			final Database db = this.db.get();
			if (db != null) {
				try {
					db.getConnection().commit();
				} catch (final SQLException e) {
					sqlException(e);
				}
				db.close();
				this.db.set(null);
			}
		}
	}

	long lastRowID = -1;

	protected void checkDatabase() {
		try {
			final long lastRowID; // local variable as this is used to clean the database below
			ResultSet r = null;
			try {
				synchronized (db) {
					if (closed || db.get() == null)
						return;
					lastRowID = this.lastRowID;
					final PreparedStatement monitorQuery = this.monitorQuery;
					assert monitorQuery != null;
					monitorQuery.setLong(1, lastRowID);
					monitorQuery.setString(2, guid);
					monitorQuery.execute();
					r = monitorQuery.getResultSet();
					assert r != null;
				}
				if (!closed)
					loadVariables(r);
			} finally {
				if (r != null)
					r.close();
			}

			if (!closed) { // Skript may have been disabled in the meantime // TODO not fixed
				new Task(Skript.getInstance(), (long) Math.ceil(2. * monitor_interval / 50) + 100, true) { // 2 times the interval + 5 seconds
					@Override
					public void run() {
						try {
							synchronized (db) {
								if (closed || db.get() == null)
									return;
								final PreparedStatement monitorCleanUpQuery = SQLStorage.this.monitorCleanUpQuery;
								assert monitorCleanUpQuery != null;
								monitorCleanUpQuery.setLong(1, lastRowID);
								monitorCleanUpQuery.executeUpdate();
							}
						} catch (final SQLException e) {
							sqlException(e);
						}
					}
				};
			}
		} catch (final SQLException e) {
			sqlException(e);
		}
	}

//	private final static class VariableInfo {
//		final String name;
//		final byte[] value;
//		final ClassInfo<?> ci;
//
//		public VariableInfo(final String name, final byte[] value, final ClassInfo<?> ci) {
//			this.name = name;
//			this.value = value;
//			this.ci = ci;
//		}
//	}

//	final static LinkedList<VariableInfo> syncDeserializing = new LinkedList<VariableInfo>();

	/**
	 * Doesn't lock the database - {@link #save(String, String, byte[])} does that // what?
	 */
	private void loadVariables(final ResultSet r) throws SQLException {
//		assert !Thread.holdsLock(db);
//		synchronized (syncDeserializing) {

		final SQLException e = Task.callSync(new Callable<SQLException>() {
			@Override
			@Nullable
			public SQLException call() throws Exception {
				try {
					while (r.next()) {
						int i = 1;
						final String name = r.getString(i++);
						if (name == null) {
							Skript.error("Variable with NULL name found in the database '" + getUserConfigurationName() + "', ignoring it");
							continue;
						}
						final String type = r.getString(i++);
						final byte[] value = r.getBytes(i++); // Blob not supported by SQLite
						lastRowID = r.getLong(i++);
						if (value == null) {
							Variables.variableLoaded(name, null, SQLStorage.this);
						} else {
							final ClassInfo<?> c = Classes.getClassInfoNoError(type);
							@SuppressWarnings("unused")
							Serializer<?> s;
							if (c == null || (s = c.getSerializer()) == null) {
								Skript.error("Cannot load the variable {" + name + "} from the database '" + getUserConfigurationName() + "', because the type '" + type + "' cannot be recognised or cannot be stored in variables");
								continue;
							}
//					if (s.mustSyncDeserialization()) {
//						syncDeserializing.add(new VariableInfo(name, value, c));
//					} else {
							final Object d = Classes.deserialize(c, value);
							if (d == null) {
								Skript.error("Cannot load the variable {" + name + "} from the database '" + getUserConfigurationName() + "', because it cannot be loaded as " + c.getName().withIndefiniteArticle());
								continue;
							}
							Variables.variableLoaded(name, d, SQLStorage.this);
//					}
						}
					}
				} catch (final SQLException e) {
					return e;
				}
				return null;
			}
		});
		if (e != null)
			throw e;

//			if (!syncDeserializing.isEmpty()) {
//				Task.callSync(new Callable<Void>() {
//					@Override
//					@Nullable
//					public Void call() throws Exception {
//						synchronized (syncDeserializing) {
//							for (final VariableInfo o : syncDeserializing) {
//								final Object d = Classes.deserialize(o.ci, o.value);
//								if (d == null) {
//									Skript.error("Cannot load the variable {" + o.name + "} from the database " + getUserConfigurationName() + ", because it cannot be loaded as a " + o.ci.getName());
//									continue;
//								}
//								Variables.variableLoaded(o.name, d, DatabaseStorage.this);
//							}
//							syncDeserializing.clear();
//							return null;
//						}
//					}
//				});
//			}
//		}
	}

//	private final static class OldVariableInfo {
//		final String name;
//		final String value;
//		final ClassInfo<?> ci;
//
//		public OldVariableInfo(final String name, final String value, final ClassInfo<?> ci) {
//			this.name = name;
//			this.value = value;
//			this.ci = ci;
//		}
//	}

//	final static LinkedList<OldVariableInfo> oldSyncDeserializing = new LinkedList<OldVariableInfo>();

	@Deprecated
	private void oldLoadVariables(final ResultSet r, final boolean hadNewTable) throws SQLException {
//		synchronized (oldSyncDeserializing) {

		final VariablesStorage temp = new VariablesStorage(getUserConfigurationName() + " old variables table") {
			@Override
			protected boolean save(final String name, @Nullable final String type, @Nullable final byte[] value) {
				assert type == null : name + "; " + type;
				return true;
			}

			@Override
			boolean accept(@Nullable final String var) {
				assert false;
				return false;
			}

			@Override
			protected boolean requiresFile() {
				assert false;
				return false;
			}

			@Override
			protected boolean load_i(final SectionNode n) {
				assert false;
				return false;
			}

			@Override
			protected File getFile(final String file) {
				assert false;
				return new File(file);
			}

			@Override
			protected void disconnect() {
				assert false;
			}

			@Override
			protected boolean connect() {
				assert false;
				return false;
			}

			@Override
			protected void allLoaded() {
				assert false;
			}
		};

		final SQLException e = Task.callSync(new Callable<SQLException>() {
			@SuppressWarnings("null")
			@Override
			@Nullable
			public SQLException call() throws Exception {
				try {
					while (r.next()) {
						int i = 1;
						final String name = r.getString(i++);
						if (name == null) {
							Skript.error("Variable with NULL name found in the database, ignoring it");
							continue;
						}
						final String type = r.getString(i++);
						final String value = r.getString(i++);
						lastRowID = r.getLong(i++);
						if (type == null || value == null) {
							Variables.variableLoaded(name, null, hadNewTable ? temp : SQLStorage.this);
						} else {
							final ClassInfo<?> c = Classes.getClassInfoNoError(type);
							Serializer<?> s;
							if (c == null || (s = c.getSerializer()) == null) {
								Skript.error("Cannot load the variable {" + name + "} from the database, because the type '" + type + "' cannot be recognised or not stored in variables");
								continue;
							}
//					if (s.mustSyncDeserialization()) {
//						oldSyncDeserializing.add(new OldVariableInfo(name, value, c));
//					} else {
							final Object d = s.deserialize(value);
							if (d == null) {
								Skript.error("Cannot load the variable {" + name + "} from the database, because '" + value + "' cannot be parsed as a " + type);
								continue;
							}
							Variables.variableLoaded(name, d, SQLStorage.this);
//					}
						}
					}
				} catch (final SQLException e) {
					return e;
				}
				return null;
			}
		});
		if (e != null)
			throw e;

//			if (!oldSyncDeserializing.isEmpty()) {
//				Task.callSync(new Callable<Void>() {
//					@Override
//					@Nullable
//					public Void call() throws Exception {
//						synchronized (oldSyncDeserializing) {
//							for (final OldVariableInfo o : oldSyncDeserializing) {
//								final Serializer<?> s = o.ci.getSerializer();
//								if (s == null) {
//									assert false : o.ci;
//									continue;
//								}
//								final Object d = s.deserialize(o.value);
//								if (d == null) {
//									Skript.error("Cannot load the variable {" + o.name + "} from the database, because '" + o.value + "' cannot be parsed as a " + o.ci.getCodeName());
//									continue;
//								}
//								Variables.variableLoaded(o.name, d, DatabaseStorage.this);
//							}
//							oldSyncDeserializing.clear();
//							return null;
//						}
//					}
//				});
//			}
//		}
	}

	void sqlException(final SQLException e) {
		Skript.error("database error: " + e.getLocalizedMessage());
		if (Skript.testing())
			e.printStackTrace();
		prepareQueries(); // a query has to be recreated after an error
	}

}
