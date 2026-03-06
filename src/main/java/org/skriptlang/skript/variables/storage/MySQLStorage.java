package org.skriptlang.skript.variables.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.JdbcStorage;

import com.zaxxer.hikari.HikariConfig;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

/**
 * MySQL storage for Skript variables.
 */
@SuppressWarnings("SqlSourceToSinkFlow")
public class MySQLStorage extends JdbcStorage {

	public MySQLStorage(SkriptAddon source, String type) {
		super(source, type);
	}

	@Override
	protected @Nullable HikariConfig configuration(SectionNode sectionNode) {
		String host = getValue(sectionNode, "host");
		Integer port = getValue(sectionNode, "port", Integer.class);
		String database = getValue(sectionNode, "database");
		if (host == null || port == null || database == null)
			return null;

		HikariConfig configuration = new HikariConfig();
		configuration.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
		configuration.setUsername(getValue(sectionNode, "user"));
		configuration.setPassword(getValue(sectionNode, "password"));

		return configuration;
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	protected File getFile(String fileName) {
		throw new UnsupportedOperationException();
	}

	// language=MySQL
	@Override
	protected String createTableQuery() {
		return "CREATE TABLE IF NOT EXISTS " + table + " (" +
			"name VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ") PRIMARY KEY, " +
			"type VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + "), " +
			"value BLOB(" + MAX_VALUE_SIZE + ")" +
			") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
	}

	@Override
	protected PreparedStatement writeSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement(
			"INSERT INTO " + table + " (name, type, value) VALUES (?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE type=VALUES(type), value=VALUES(value)"
		);
	}

}
