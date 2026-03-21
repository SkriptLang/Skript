package ch.njol.skript.variables;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;

/**
 * MySQL variable storage using modern direct JDBC (no SQLibrary dependency).
 * Uses utf8mb4 charset instead of the removed ucs2 charset.
 */
public class MySQLStorage extends SQLStorage {

    // When Variables loads MySQLStorage (already registered), this triggers
    // RedisStorage registration via the bootstrap class.
    static {
        RedisBootstrap.register();
    }

    MySQLStorage(String type) {
        super(type,
            "CREATE TABLE IF NOT EXISTS %s (" +
            "rowid        BIGINT  NOT NULL  AUTO_INCREMENT  PRIMARY KEY," +
            "name         VARCHAR(380)  NOT NULL  UNIQUE," +
            "type         VARCHAR(50)," +
            "value        BLOB(10000)," +
            "update_guid  CHAR(36)  NOT NULL" +
            ") CHARACTER SET utf8mb4 COLLATE utf8mb4_bin");
    }

    @Override
    public DatabaseWrapper initialize(SectionNode config) {
        String host = this.getValue(config, "host");
        Integer port = this.getValue(config, "port", Integer.class);
        String user = this.getValue(config, "user");
        String password = this.getValue(config, "password");
        String database = this.getValue(config, "database");
        this.setTableName(config.get("table", "variables21"));
        if (host == null || port == null || user == null || password == null || database == null) {
            return null;
        }
        return new MySQLDatabase(SkriptLogger.LOGGER, "[Skript]", host, port, database, user, password);
    }

    @Override
    protected boolean requiresFile() {
        return false;
    }
}
