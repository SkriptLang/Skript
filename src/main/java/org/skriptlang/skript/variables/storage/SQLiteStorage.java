package org.skriptlang.skript.variables.storage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;
import ch.njol.skript.variables.SerializedVariable;

import com.zaxxer.hikari.HikariConfig;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

/**
 * SQLite storage for Skript variables.
 * <p>
 * This class is deprecated and scheduled for removal in future versions of Skript.
 * Use {@link JdbcStorage} with a different database like H2Storage or MySQLStorage instead.
 */
@Deprecated
@ScheduledForRemoval
public class SQLiteStorage extends JdbcStorage {

	/**
	 * Creates a new SQLite storage.
	 *
	 * @param source The source of the storage.
	 * @param type The database type.
	 */
	SQLiteStorage(SkriptAddon source, String type) {
		super(source, type,
				"CREATE TABLE IF NOT EXISTS %s (" +
				"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL," +
				"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"value        BLOB(" + MAX_VALUE_SIZE + ")" +
				");"
		);
	}

	@Override
	@Nullable
	public final HikariConfig configuration(SectionNode config) {
		File file = this.file;
		if (file == null)
			return null;
		setTableName(config.get("table", DEFAULT_TABLE_NAME));
		String name = file.getName();
		if (!name.endsWith(".db"))
			name = name + ".db";

		HikariConfig configuration = new HikariConfig();
		configuration.setJdbcUrl("jdbc:sqlite:" + (file == null ? ":memory:" : file.getAbsolutePath()));
		return configuration;
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	@Override
	protected File getFile(String file) {
		if (!file.endsWith(".db"))
			file = file + ".db"; // required by SQLite
		return new File(file);
	}

	@Override
	protected String getReplaceQuery() {
		return "REPLACE INTO " + getTableName() + " (name, type, value) VALUES (?, ?, ?)";
	}

	@Override
	protected String getSelectQuery() {
		return "SELECT name, type, value from " + getTableName();
	}

	@Override
	protected @Nullable Function<@Nullable ResultSet, JdbcVariableResult> get(boolean testOperation) {
		return result -> {
			if (result == null)
				return null;
			int i = 1;
			try {
				String name = result.getString(i++);
				if (name == null) {
					Skript.error("Variable with NULL name found in the database '" + getUserConfigurationName() + "', ignoring it");
					return null;
				}
				String type = result.getString(i++);
				byte[] value = result.getBytes(i++);
				return new JdbcVariableResult(-1L, new SerializedVariable(name, type, value));
			} catch (SQLException e) {
				Skript.exception(e, "Failed to collect variable from database.");
				return null;
			}
		};
	}

}
