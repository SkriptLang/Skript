package org.skriptlang.skript.variables.storage.jdbc;

import org.skriptlang.skript.addon.SkriptAddon;

import java.util.ArrayList;
import java.util.List;

/**
 * An SQLite storage that acts like a database other servers could share, so the monitoring code {@link JdbcStorage} has
 * for MySQL can be tested without a MySQL server: it supports monitoring, and makes the change log while monitoring.
 * Real SQLite does neither, since no other server can use its file.
 */
abstract class ShareableSQLiteStorage extends SQLiteStorage {

	ShareableSQLiteStorage(SkriptAddon registeringAddon, String type) {
		super(registeringAddon, type);
	}

	@Override
	protected boolean supportsMonitoring() {
		return true;
	}

	@Override
	protected List<String> schemaSql(String table, String metaTable, String logTable) {
		List<String> statements = new ArrayList<>(super.schemaSql(table, metaTable, logTable));
		if (monitorsChanges()) {
			// like MySQL's: one entry per name, read in version order
			statements.add("CREATE TABLE IF NOT EXISTS " + logTable + " (name TEXT NOT NULL PRIMARY KEY, "
				+ "version INTEGER NOT NULL, seq INTEGER NOT NULL) WITHOUT ROWID");
			statements.add("CREATE INDEX IF NOT EXISTS " + logTable + "_version ON " + logTable + " (version, seq)");
		}
		return statements;
	}

}
