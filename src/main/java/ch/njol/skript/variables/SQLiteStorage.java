package ch.njol.skript.variables;

import java.io.File;

import ch.njol.skript.config.SectionNode;

/**
	- SQLite adapter for {@link SQLStorage} that stores data in a file.

	- It provides the SQLite table setup and opens the configured database file
	using the bundled JDBC driver. The parent class handles loading, queued SQL
	changes, commits, and optional monitoring. {@link JdbcDatabase} handles the
	connection.

	- SQLite is selected normally through {@link Variables}. It uses the same
	serialized types and values as the other storage options.

	- File and backup handling is done through {@link VariablesStorage}.

	- It does not use the optional MySQL backend's journal or connection pool.
*/

public class SQLiteStorage extends SQLStorage {

	SQLiteStorage(String type) {
		super(type, "CREATE TABLE IF NOT EXISTS %s (" +
				"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  PRIMARY KEY," +
				"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"value        BLOB(" + MAX_VALUE_SIZE + ")," +
				"update_guid  CHAR(36)  NOT NULL" +
				")");
	}

	@Override
	public JdbcDatabase initialize(SectionNode config) {
		File f = file;
		if (f == null)
			return null;
		setTableName(config.get("table", "variables21"));
		return new JdbcDatabase(() -> {
			org.sqlite.SQLiteDataSource source = new org.sqlite.SQLiteDataSource();
			source.setUrl("jdbc:sqlite:" + f.getAbsolutePath());
			return source.getConnection();
		});
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

}
