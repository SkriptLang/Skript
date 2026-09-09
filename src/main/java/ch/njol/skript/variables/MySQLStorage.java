package ch.njol.skript.variables;

import ch.njol.skript.config.SectionNode;

/**
	- MySQL adapter used by the normal database configuration and {@link SQLStorage}.

	- It provides the MySQL table setup and JDBC connection factory. The parent class
	handles loading, queued changes, commits, and watching for changes.

	- The stored types and values use Skript's normal serializers.

	- {@link JdbcDatabase} handles the connection. The MySQL settings are shared with
	{@link MySQLConnectionPool}, but this adapter does not use the pool or its recovery
	system.

	- The main MySQL configuration uses {@link PooledMySQLStorage} instead. They use
	different tables and work differently, so this adapter is not a wrapper or migration
	path for the optional backend.
*/

public class MySQLStorage extends SQLStorage {

	MySQLStorage(String type) {
		super(type, "CREATE TABLE IF NOT EXISTS %s (" +
				"rowid        BIGINT  NOT NULL  AUTO_INCREMENT  PRIMARY KEY," +
				"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  UNIQUE," +
				"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"value        BLOB(" + MAX_VALUE_SIZE + ")," +
				"update_guid  CHAR(36)  NOT NULL" +
				") CHARACTER SET ucs2 COLLATE ucs2_bin");
	}

	@Override
	public JdbcDatabase initialize(SectionNode config) throws java.sql.SQLException {
		String host = getValue(config, "host");
		Integer port = getValue(config, "port", Integer.class);
		String user = getValue(config, "user");
		String password = getValue(config, "password");
		String database = getValue(config, "database");
		setTableName(config.get("table", "variables21"));
		if (host == null || port == null || user == null || password == null || database == null)
			return null;
		return new JdbcDatabase(MySQLConnectionPool.dataSource(host, port, database, user, password,
				config.get("ssl mode", "REQUIRED"))::getConnection);
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

}
