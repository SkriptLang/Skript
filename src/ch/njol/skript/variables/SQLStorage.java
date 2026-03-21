package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.skript.variables.VariablesStorage;
import ch.njol.skript.variables.DatabaseWrapper;
import ch.njol.util.SynchronizedReference;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Modified SQLStorage that uses DatabaseWrapper instead of SQLibrary.
 * No longer requires the SQLibrary plugin to be installed.
 */
public abstract class SQLStorage extends VariablesStorage {

    public static final int MAX_VARIABLE_NAME_LENGTH = 380;
    public static final int MAX_CLASS_CODENAME_LENGTH = 50;
    public static final int MAX_VALUE_SIZE = 10000;
    private static final String SELECT_ORDER = "name, type, value, rowid";
    private static final String OLD_TABLE_NAME = "variables";

    @Nullable
    private String formattedCreateQuery;
    private final String createTableQuery;
    private String tableName;
    final SynchronizedReference<DatabaseWrapper> db = new SynchronizedReference<DatabaseWrapper>(null);
    private boolean monitor = false;
    long monitor_interval;
    private static final String guid = UUID.randomUUID().toString();
    private static final long TRANSACTION_DELAY = 500L;

    @Nullable
    private PreparedStatement writeQuery;
    @Nullable
    private PreparedStatement deleteQuery;
    @Nullable
    private PreparedStatement monitorQuery;
    @Nullable
    PreparedStatement monitorCleanUpQuery;
    long lastRowID = -1L;

    public SQLStorage(String type, String createTableQuery) {
        super(type);
        this.createTableQuery = createTableQuery;
        this.tableName = "variables21";
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Nullable
    public abstract DatabaseWrapper initialize(SectionNode var1);

    @Nullable
    public String getFormattedCreateQuery() {
        if (this.formattedCreateQuery == null) {
            this.formattedCreateQuery = String.format(this.createTableQuery, this.tableName);
        }
        return this.formattedCreateQuery;
    }

    @Override
    protected boolean load_i(SectionNode n) {
        synchronized (this.db) {
            DatabaseWrapper db;
            // No longer require SQLibrary plugin - we use direct JDBC now
            Boolean monitor_changes = this.getValue(n, "monitor changes", Boolean.class);
            Timespan monitor_interval = this.getValue(n, "monitor interval", Timespan.class);
            if (monitor_changes == null || monitor_interval == null) {
                return false;
            }
            this.monitor = monitor_changes;
            this.monitor_interval = monitor_interval.getAs(Timespan.TimePeriod.MILLISECOND);
            try {
                DatabaseWrapper database = this.initialize(n);
                if (database == null) {
                    return false;
                }
                db = database;
                this.db.set(db);
            } catch (RuntimeException e) {
                Skript.error("Database initialization error: " + e.getLocalizedMessage());
                return false;
            }
            SkriptLogger.setNode(null);
            if (!this.connect(true)) {
                return false;
            }
            try {
                boolean hadNewTable = db.isTable(this.getTableName());
                if (this.getFormattedCreateQuery() == null) {
                    Skript.error("Could not create the variables table in the database. The query to create the variables table '" + this.tableName + "' in the database '" + this.getUserConfigurationName() + "' is null.");
                    return false;
                }
                try {
                    db.query(this.getFormattedCreateQuery());
                } catch (SQLException e) {
                    Skript.error("Could not create the variables table '" + this.tableName + "' in the database '" + this.getUserConfigurationName() + "': " + e.getLocalizedMessage() + ". Please create the table yourself using the following query: " + String.format(this.createTableQuery, this.tableName).replace(",", ", ").replaceAll("\\s+", " "));
                    return false;
                }
                if (!this.prepareQueries()) {
                    return false;
                }
                ResultSet r2 = db.query("SELECT name, type, value, rowid FROM " + this.getTableName());
                assert (r2 != null);
                try {
                    this.loadVariables(r2);
                } finally {
                    r2.close();
                }
            } catch (SQLException e) {
                this.sqlException(e);
                return false;
            }

            // Keep-alive thread
            Skript.newThread(new Runnable() {
                @Override
                public void run() {
                    while (!SQLStorage.this.closed) {
                        synchronized (SQLStorage.this.db) {
                            try {
                                DatabaseWrapper db = SQLStorage.this.db.get();
                                if (db != null) {
                                    if (!db.isOpen()) {
                                        Skript.warning("Database '" + SQLStorage.this.getUserConfigurationName() + "' connection lost, attempting reconnect...");
                                        if (db.ensureConnection()) {
                                            // Restore connection state after reconnect
                                            try {
                                                db.getConnection().setAutoCommit(false);
                                            } catch (SQLException ex) {
                                                Skript.error("Could not set autoCommit after reconnect: " + ex.getMessage());
                                            }
                                            // Re-prepare all queries with the new connection
                                            SQLStorage.this.prepareQueries();
                                            Skript.info("Database '" + SQLStorage.this.getUserConfigurationName() + "' reconnected successfully!");
                                        } else {
                                            Skript.error("Database '" + SQLStorage.this.getUserConfigurationName() + "' reconnect failed!");
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Skript.error("Database keep-alive error: " + e.getMessage());
                            }
                        }
                        try {
                            Thread.sleep(10000L);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }, "Skript database '" + this.getUserConfigurationName() + "' connection keep-alive thread").start();

            return true;
        }
    }

    @Override
    protected void allLoaded() {
        Skript.debug("Database " + this.getUserConfigurationName() + " loaded. Queue size = " + this.changesQueue.size());

        // Transaction commit thread
        Skript.newThread(new Runnable() {
            @Override
            public void run() {
                while (!SQLStorage.this.closed) {
                    long lastCommit;
                    synchronized (SQLStorage.this.db) {
                        DatabaseWrapper db = SQLStorage.this.db.get();
                        try {
                            if (db != null && db.getConnection() != null) {
                                db.getConnection().commit();
                            }
                        } catch (SQLException e) {
                            SQLStorage.this.sqlException(e);
                        }
                        lastCommit = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(Math.max(0L, lastCommit + 500L - System.currentTimeMillis()));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "Skript database '" + this.getUserConfigurationName() + "' transaction committing thread").start();

        // Monitor thread
        if (this.monitor) {
            Skript.newThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(SQLStorage.this.monitor_interval);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    long lastWarning = Long.MIN_VALUE;
                    int WARING_INTERVAL = 10;
                    while (!SQLStorage.this.closed) {
                        long next = System.currentTimeMillis() + SQLStorage.this.monitor_interval;
                        SQLStorage.this.checkDatabase();
                        long now = System.currentTimeMillis();
                        if (next < now && lastWarning + 10000L < now) {
                            Skript.warning("Cannot load variables from the database fast enough (loading took " + ((double)(now - next + SQLStorage.this.monitor_interval) / 1000.0) + "s, monitor interval = " + ((double)SQLStorage.this.monitor_interval / 1000.0) + "s). Please increase your monitor interval or reduce usage of variables. (this warning will be repeated at most once every 10 seconds)");
                            lastWarning = now;
                        }
                        while (System.currentTimeMillis() < next) {
                            try {
                                Thread.sleep(next - System.currentTimeMillis());
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                    }
                }
            }, "Skript database '" + this.getUserConfigurationName() + "' monitor thread").start();
        }
    }

    @Override
    protected File getFile(String file) {
        if (!((String) file).endsWith(".db")) {
            file = (String) file + ".db";
        }
        return new File((String) file);
    }

    @Override
    protected boolean connect() {
        return this.connect(false);
    }

    private final boolean connect(boolean first) {
        synchronized (this.db) {
            DatabaseWrapper db = this.db.get();
            if (db == null || !db.open()) {
                if (first) {
                    Skript.error("Cannot connect to the database '" + this.getUserConfigurationName() + "'! Please make sure that all settings are correct");
                } else {
                    Skript.exception("Cannot reconnect to the database '" + this.getUserConfigurationName() + "'!");
                }
                return false;
            }
            try {
                db.getConnection().setAutoCommit(false);
            } catch (SQLException e) {
                this.sqlException(e);
                return false;
            }
            return true;
        }
    }

    private boolean prepareQueries() {
        synchronized (this.db) {
            DatabaseWrapper db = this.db.get();
            assert (db != null);
            try {
                try {
                    if (this.writeQuery != null) {
                        this.writeQuery.close();
                    }
                } catch (SQLException e) {
                    // ignore
                }
                this.writeQuery = db.prepare("REPLACE INTO " + this.getTableName() + " (name, type, value, update_guid) VALUES (?, ?, ?, ?)");
                try {
                    if (this.deleteQuery != null) {
                        this.deleteQuery.close();
                    }
                } catch (SQLException e) {
                    // ignore
                }
                this.deleteQuery = db.prepare("DELETE FROM " + this.getTableName() + " WHERE name = ?");
                try {
                    if (this.monitorQuery != null) {
                        this.monitorQuery.close();
                    }
                } catch (SQLException e) {
                    // ignore
                }
                this.monitorQuery = db.prepare("SELECT name, type, value, rowid FROM " + this.getTableName() + " WHERE rowid > ? AND update_guid != ?");
                try {
                    if (this.monitorCleanUpQuery != null) {
                        this.monitorCleanUpQuery.close();
                    }
                } catch (SQLException e) {
                    // ignore
                }
                this.monitorCleanUpQuery = db.prepare("DELETE FROM " + this.getTableName() + " WHERE value IS NULL AND rowid < ?");
            } catch (SQLException e) {
                Skript.exception((Throwable) e, "Could not prepare queries for the database '" + this.getUserConfigurationName() + "': " + e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void disconnect() {
        synchronized (this.db) {
            DatabaseWrapper db = this.db.get();
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
        synchronized (this.db) {
            if (name.length() > 380) {
                Skript.error("The name of the variable {" + name + "} is too long to be saved in a database (length: " + name.length() + ", maximum allowed: 380)! It will be truncated and won't bet available under the same name again when loaded.");
            }
            if (value != null && value.length > 10000) {
                Skript.error("The variable {" + name + "} cannot be saved in the database as its value's size (" + value.length + ") exceeds the maximum allowed size of 10000! An attempt to save the variable will be made nonetheless.");
            }
            try {
                // Ensure connection is still valid before saving
                DatabaseWrapper db = this.db.get();
                if (db != null && !db.isOpen()) {
                    Skript.warning("Database connection lost, attempting reconnect for save operation...");
                    if (db.ensureConnection()) {
                        try { db.getConnection().setAutoCommit(false); } catch (SQLException ex) { /* ignore */ }
                        this.prepareQueries();
                    }
                }

                if (type == null) {
                    assert (value == null);
                    PreparedStatement deleteQuery = this.deleteQuery;
                    assert (deleteQuery != null);
                    deleteQuery.setString(1, name);
                    deleteQuery.executeUpdate();
                } else {
                    int i = 1;
                    PreparedStatement writeQuery = this.writeQuery;
                    assert (writeQuery != null);
                    writeQuery.setString(i++, name);
                    writeQuery.setString(i++, type);
                    writeQuery.setBytes(i++, value);
                    writeQuery.setString(i++, guid);
                    writeQuery.executeUpdate();
                }
            } catch (SQLException e) {
                this.sqlException(e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() {
        synchronized (this.db) {
            super.close();
            DatabaseWrapper db = this.db.get();
            if (db != null) {
                try {
                    if (db.getConnection() != null) {
                        db.getConnection().commit();
                    }
                } catch (SQLException e) {
                    this.sqlException(e);
                }
                db.close();
                this.db.set(null);
            }
        }
    }

    protected void checkDatabase() {
        try {
            final long savedRowID;
            ResultSet r = null;
            try {
                synchronized (this.db) {
                    if (this.closed || this.db.get() == null) {
                        return;
                    }
                    savedRowID = this.lastRowID;
                    PreparedStatement monitorQuery = this.monitorQuery;
                    assert (monitorQuery != null);
                    monitorQuery.setLong(1, savedRowID);
                    monitorQuery.setString(2, guid);
                    monitorQuery.execute();
                    r = monitorQuery.getResultSet();
                    assert (r != null);
                }
                if (!this.closed) {
                    this.loadVariables(r);
                }
            } finally {
                if (r != null) {
                    try { r.close(); } catch (SQLException ex) { /* ignore */ }
                }
            }
            if (!this.closed) {
                new Task((Plugin) Skript.getInstance(), (long) Math.ceil(2.0 * (double) this.monitor_interval / 50.0) + 100L, true) {
                    @Override
                    public void run() {
                        try {
                            synchronized (SQLStorage.this.db) {
                                if (SQLStorage.this.closed || SQLStorage.this.db.get() == null) {
                                    return;
                                }
                                PreparedStatement monitorCleanUpQuery = SQLStorage.this.monitorCleanUpQuery;
                                assert (monitorCleanUpQuery != null);
                                monitorCleanUpQuery.setLong(1, savedRowID);
                                monitorCleanUpQuery.executeUpdate();
                            }
                        } catch (SQLException e) {
                            SQLStorage.this.sqlException(e);
                        }
                    }
                };
            }
        } catch (SQLException e) {
            this.sqlException(e);
        }
    }

    private void loadVariables(final ResultSet r) throws SQLException {
        SQLException e = Task.callSync(new Callable<SQLException>() {
            @Override
            @Nullable
            public SQLException call() throws Exception {
                try {
                    while (r.next()) {
                        Serializer<?> s;
                        String name;
                        int i = 1;
                        if ((name = r.getString(i++)) == null) {
                            Skript.error("Variable with NULL name found in the database '" + SQLStorage.this.getUserConfigurationName() + "', ignoring it");
                            continue;
                        }
                        String type = r.getString(i++);
                        byte[] value = r.getBytes(i++);
                        SQLStorage.this.lastRowID = r.getLong(i++);
                        if (value == null) {
                            Variables.variableLoaded(name, null, SQLStorage.this);
                            continue;
                        }
                        ClassInfo<?> c = Classes.getClassInfoNoError(type);
                        if (c == null || (s = c.getSerializer()) == null) {
                            Skript.error("Cannot load the variable {" + name + "} from the database '" + SQLStorage.this.getUserConfigurationName() + "', because the type '" + type + "' cannot be recognised or cannot be stored in variables");
                            continue;
                        }
                        Object d = Classes.deserialize(c, value);
                        if (d == null) {
                            Skript.error("Cannot load the variable {" + name + "} from the database '" + SQLStorage.this.getUserConfigurationName() + "', because it cannot be loaded as " + c.getName().withIndefiniteArticle());
                            continue;
                        }
                        Variables.variableLoaded(name, d, SQLStorage.this);
                    }
                } catch (SQLException e) {
                    return e;
                }
                return null;
            }
        });
        if (e != null) {
            throw e;
        }
    }

    void sqlException(SQLException e) {
        Skript.error("database error: " + e.getLocalizedMessage());
        if (Skript.testing()) {
            e.printStackTrace();
        }
        this.prepareQueries();
    }
}
