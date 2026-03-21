package ch.njol.skript.variables;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Modern MySQL database connection using direct JDBC.
 * Replaces SQLibrary's MySQL class.
 * Supports MySQL Connector/J 8.x+ (com.mysql.cj.jdbc.Driver)
 * and fallback to 5.x (com.mysql.jdbc.Driver).
 *
 * FIXED: Removed deprecated autoReconnect=true which masked real errors
 * with "Attempted reconnect 3 times. Giving up." Added better diagnostics.
 */
public class MySQLDatabase extends DatabaseWrapper {

    private final String hostname;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLDatabase(Logger log, String prefix, String hostname, int port,
                         String database, String username, String password) {
        super(log, prefix);
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" + hostname + ":" + port + "/" + database
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF-8"
            + "&useUnicode=true"
            + "&connectTimeout=10000"
            + "&socketTimeout=30000";
    }

    @Override
    public boolean open() {
        // Try MySQL Connector/J 8.x driver first, then fallback to 5.x
        boolean driverLoaded = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e2) {
                log.severe(prefix + " MySQL JDBC driver not found! " +
                    "Please make sure mysql-connector-java is in your server's classpath. " +
                    "Tried: com.mysql.cj.jdbc.Driver (8.x) and com.mysql.jdbc.Driver (5.x)");
                return false;
            }
        }

        String url = getJdbcUrl();
        log.info(prefix + " Connecting to MySQL: " + hostname + ":" + port + "/" + database + " as " + username);
        try {
            connection = DriverManager.getConnection(url, username, password);
            log.info(prefix + " MySQL connection established successfully!");
            return true;
        } catch (SQLException e) {
            log.severe(prefix + " Could not establish MySQL connection!");
            log.severe(prefix + " URL: jdbc:mysql://" + hostname + ":" + port + "/" + database);
            log.severe(prefix + " Error: " + e.getMessage());
            log.severe(prefix + " SQLState: " + e.getSQLState());
            log.severe(prefix + " ErrorCode: " + e.getErrorCode());
            if (e.getCause() != null) {
                log.severe(prefix + " Root cause: " + e.getCause().getMessage());
            }
            return false;
        }
    }
}
