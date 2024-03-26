/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.variables.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.Nullable;

import com.zaxxer.hikari.HikariConfig;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;
import ch.njol.skript.variables.SerializedVariable;

public class H2Storage extends JdbcStorage {

	H2Storage(SkriptAddon source, String name) {
		super(source, name,
				"CREATE TABLE IF NOT EXISTS %s (" +
				"`name`         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL  PRIMARY KEY," +
				"`type`         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"`value`        BINARY LARGE OBJECT(" + MAX_VALUE_SIZE + ")" +
				");"
		);
	}

	@Override
	@Nullable
	public HikariConfig configuration(SectionNode config) {
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
	protected BiFunction<Integer, ResultSet, SerializedVariable> get() {
		return (index, result) -> {
			int i = 1;
			try {
				String name = result.getString(i++);
				if (name == null) {
					Skript.error("Variable with a NULL name found in the database '" + databaseName + "', ignoring it");
					return null;
				}
				String type = result.getString(i++);
				byte[] value = result.getBytes(i++);
				return new SerializedVariable(name, type, value);
			} catch (SQLException e) {
				Skript.exception(e, "Failed to collect variable from database.");
				return null;
			}
		};
	}

}
