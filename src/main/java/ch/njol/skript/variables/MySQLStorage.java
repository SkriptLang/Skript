package ch.njol.skript.variables;

import ch.njol.skript.config.SectionNode;

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
