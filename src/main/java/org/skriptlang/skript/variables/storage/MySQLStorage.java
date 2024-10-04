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
import java.util.function.Function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;
import ch.njol.skript.variables.SerializedVariable;
import ch.njol.util.NonNullPair;

import com.zaxxer.hikari.HikariConfig;

import org.jetbrains.annotations.Nullable;

public class MySQLStorage extends JdbcStorage {

	MySQLStorage(SkriptAddon source, String name) {
		super(source, name,
				"CREATE TABLE IF NOT EXISTS %s (" +
				"rowid        BIGINT  NOT NULL  AUTO_INCREMENT," +
				"name         VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ")  NOT NULL," +
				"type         VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + ")," +
				"value        BLOB(" + MAX_VALUE_SIZE + ")," +
				"PRIMARY KEY(rowid)," +
				"UNIQUE KEY(name)" +
				") CHARACTER SET ucs2 COLLATE ucs2_bin;"
		);
	}

	@Override
	@Nullable
	public final HikariConfig configuration(SectionNode section) {
		String host = getValue(section, "host");
		Integer port = getValue(section, "port", Integer.class);
		String database = getValue(section, "database");
		if (host == null || port == null || database == null)
			return null;

		HikariConfig configuration = new HikariConfig();
		configuration.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
		configuration.setUsername(getValue(section, "user"));
		configuration.setPassword(getValue(section, "password"));

		setTableName(section.get("table", DEFAULT_TABLE_NAME));
		return configuration;
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	protected String getReplaceQuery() {
		return "REPLACE INTO " + getTableName() + " (name, type, value) VALUES (?, ?, ?)";
	}

	@Override
	protected NonNullPair<String, String> getMonitorQueries() {
		return new NonNullPair<>(
				"SELECT rowid, name, type, value FROM " + getTableName() + " WHERE rowid > ?",
				"DELETE FROM " + getTableName() + " WHERE value IS NULL AND rowid < ?"
		);
	}

	@Override
	protected String getSelectQuery() {
		return "SELECT rowid, name, type, value from " + getTableName();
	}

	@Override
	protected @Nullable Function<@Nullable ResultSet, NonNullPair<Long, SerializedVariable>> get(boolean testOperation) {
		return result -> {
			if (result == null)
				return null;
			int i = 1;
			try {
				long rowid = result.getLong(i++); // rowid is used for monitor changes.
				String name = result.getString(i++);
				if (name == null) {
					Skript.error("Variable with NULL name found in the database '" + databaseName + "', ignoring it");
					return null;
				}
				String type = result.getString(i++);
				byte[] value = result.getBytes(i++);
				return new NonNullPair<>(rowid, new SerializedVariable(name, type, value));
			} catch (SQLException e) {
				Skript.exception(e, "Failed to collect variable from database.");
				return null;
			}
		};
	}

}
