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
 * MySQL storage adapted from vlt-references/mysql.
 * Uses its row-id schema and replacement writes through Paper's JDBC driver.
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
		String user = getValue(sectionNode, "user");
		String password = getValue(sectionNode, "password");
		if (host == null || port == null || database == null || user == null || password == null)
			return null;

		HikariConfig configuration = new HikariConfig();
		configuration.setDriverClassName("com.mysql.cj.jdbc.Driver");
		configuration.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
		configuration.setUsername(user);
		configuration.setPassword(password);

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
			"rowid BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"name VARCHAR(" + MAX_VARIABLE_NAME_LENGTH + ") NOT NULL UNIQUE, " +
			"type VARCHAR(" + MAX_CLASS_CODENAME_LENGTH + "), " +
			"value BLOB(" + MAX_VALUE_SIZE + "), " +
			"update_guid CHAR(36) NOT NULL" +
			") CHARACTER SET utf8mb4 COLLATE utf8mb4_bin";
	}

	@Override
	protected PreparedStatement writeSingleQuery(Connection connection) throws SQLException {
		return connection.prepareStatement(
			"REPLACE INTO " + table + " (name, type, value" + legacyColumns() + ") VALUES (?, ?, ?"
				+ legacyValue() + ")"
		);
	}

}
