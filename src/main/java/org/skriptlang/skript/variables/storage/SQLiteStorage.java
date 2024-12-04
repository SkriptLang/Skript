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

import java.io.File;
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

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

@Deprecated
@ScheduledForRemoval
public class SQLiteStorage extends JdbcStorage {

	SQLiteStorage(SkriptAddon source, String name) {
		super(source, name,
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
	protected @Nullable Function<@Nullable ResultSet, NonNullPair<Long, SerializedVariable>> get(boolean testOperation) {
		return result -> {
			if (result == null)
				return null;
			int i = 1;
			try {
				String name = result.getString(i++);
				if (name == null) {
					Skript.error("Variable with NULL name found in the database '" + databaseName + "', ignoring it");
					return null;
				}
				String type = result.getString(i++);
				byte[] value = result.getBytes(i++);
				return new NonNullPair<>(-1L, new SerializedVariable(name, type, value));
			} catch (SQLException e) {
				Skript.exception(e, "Failed to collect variable from database.");
				return null;
			}
		};
	}

}
