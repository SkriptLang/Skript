package ch.njol.skript.variables;

import java.io.File;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.SQLite;

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
	public Database initialize(SectionNode config) {
		File f = file;
		if (f == null)
			return null;
		setTableName(config.get("table", "variables21"));
		String name = f.getName();
		assert name.endsWith(".db");
		return new SQLite(SkriptLogger.LOGGER, "[Skript]", f.getParent(), name.substring(0, name.length() - ".db".length()));
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

}
