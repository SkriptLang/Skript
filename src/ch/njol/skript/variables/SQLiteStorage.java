package ch.njol.skript.variables;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import java.io.File;

/**
 * SQLite variable storage using modern direct JDBC (no SQLibrary dependency).
 */
public class SQLiteStorage extends SQLStorage {

    SQLiteStorage(String type) {
        super(type,
            "CREATE TABLE IF NOT EXISTS %s (" +
            "name         VARCHAR(380)  NOT NULL  PRIMARY KEY," +
            "type         VARCHAR(50)," +
            "value        BLOB(10000)," +
            "update_guid  CHAR(36)  NOT NULL)");
    }

    @Override
    public DatabaseWrapper initialize(SectionNode config) {
        File f = this.file;
        if (f == null) {
            return null;
        }
        this.setTableName(config.get("table", "variables21"));
        return new SQLiteDatabase(SkriptLogger.LOGGER, "[Skript]", f.getAbsolutePath());
    }

    @Override
    protected boolean requiresFile() {
        return true;
    }
}
