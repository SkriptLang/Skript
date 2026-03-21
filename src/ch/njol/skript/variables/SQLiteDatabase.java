package ch.njol.skript.variables;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * SQLite database connection using direct JDBC.
 * Replaces SQLibrary's SQLite class.
 */
public class SQLiteDatabase extends DatabaseWrapper {

    private final String filePath;

    public SQLiteDatabase(Logger log, String prefix, String filePath) {
        super(log, prefix);
        this.filePath = filePath;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:sqlite:" + filePath;
    }

    @Override
    public boolean open() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log.severe(prefix + " SQLite JDBC driver not found! " +
                "Please make sure sqlite-jdbc is in your server's classpath.");
            return false;
        }

        try {
            connection = DriverManager.getConnection(getJdbcUrl());
            return true;
        } catch (SQLException e) {
            log.severe(prefix + " Could not establish SQLite connection: " + e.getMessage());
            return false;
        }
    }
}
