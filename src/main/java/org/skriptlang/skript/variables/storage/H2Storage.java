package org.skriptlang.skript.variables.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.zaxxer.hikari.HikariConfig;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;
import ch.njol.skript.variables.SerializedVariable;

public class H2Storage extends JdbcStorage {

	/**
	 * Creates a new H2 storage.
	 * 
	 * @param source The source of the storage.
	 * @param name The database name.
	 */
	H2Storage(SkriptAddon source, String type) {
		super(source, type,
				"CREATE TABLE IF NOT EXISTS %s (" +
				"`name`         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  PRIMARY KEY," +
				"`type`         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"`value`        BINARY LARGE OBJECT(" + MAX_VALUE_SIZE + ")" +
				");"
		);
	}

	@Override
	@Nullable
	public final HikariConfig configuration(SectionNode config) {
		if (file == null)
			return null;
		HikariConfig configuration = new HikariConfig();
		configuration.setPoolName("H2-Pool");
		configuration.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
		configuration.setConnectionTestQuery("VALUES 1");

		String url = "";
		if (config.get("memory", "false").equalsIgnoreCase("true"))
			url += "mem:";
		url += "file:" + file.getAbsolutePath();
		configuration.addDataSourceProperty("URL", "jdbc:h2:" + url);
		configuration.addDataSourceProperty("user", config.get("user", ""));
		configuration.addDataSourceProperty("password", config.get("password", ""));
		configuration.addDataSourceProperty("description", config.get("description", ""));
		return configuration;
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	@Override
	protected String getReplaceQuery() {
		return "MERGE INTO " + getTableName() + " KEY(name) VALUES (?, ?, ?)";
	}

	@Override
	protected String getSelectQuery() {
		return "SELECT `name`, `type`, `value` FROM " + getTableName();
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
					Skript.error("Variable with a NULL name found in the database '" + getUserConfigurationName() + "', ignoring it");
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
