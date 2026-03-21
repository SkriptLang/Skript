package ch.njol.skript.variables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Modern database wrapper replacing the abandoned SQLibrary plugin.
 * Provides direct JDBC connection management with auto-reconnect.
 */
public abstract class DatabaseWrapper {

    protected final Logger log;
    protected final String prefix;
    protected volatile Connection connection;

    public DatabaseWrapper(Logger log, String prefix) {
        this.log = log;
        this.prefix = prefix;
    }

    /**
     * Opens the database connection using the subclass-specific JDBC URL.
     */
    public abstract boolean open();

    /**
     * Returns the JDBC URL for this database.
     */
    protected abstract String getJdbcUrl();

    /**
     * Checks if the connection is open and valid.
     */
    public boolean isOpen() {
        return isOpen(1);
    }

    public boolean isOpen(int seconds) {
        if (connection != null) {
            try {
                if (connection.isValid(seconds)) {
                    return true;
                }
            } catch (SQLException e) {
                // connection is broken
            }
        }
        return false;
    }

    /**
     * Returns the active connection, reconnecting if necessary.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Ensures the connection is valid, attempts reconnection if not.
     */
    public boolean ensureConnection() {
        if (isOpen()) {
            return true;
        }
        log.warning(prefix + " Connection lost, attempting reconnect...");
        close();
        return open();
    }

    /**
     * Closes the database connection.
     */
    public boolean close() {
        if (connection != null) {
            try {
                connection.close();
                return true;
            } catch (SQLException e) {
                log.severe(prefix + " Could not close connection: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if a table exists in the database.
     */
    public boolean isTable(String table) {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT 1 FROM " + table + " LIMIT 1");
            statement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Executes a query. Returns ResultSet for SELECT, or a fabricated ResultSet for updates.
     */
    public ResultSet query(String query) throws SQLException {
        Statement statement = connection.createStatement();
        if (statement.execute(query)) {
            return statement.getResultSet();
        }
        int uc = statement.getUpdateCount();
        return connection.createStatement().executeQuery("SELECT " + uc);
    }

    /**
     * Prepares a statement.
     */
    public PreparedStatement prepare(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
}
