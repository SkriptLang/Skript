package ch.njol.skript.variables;

import java.io.File;

import ch.njol.skript.config.SectionNode;

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
