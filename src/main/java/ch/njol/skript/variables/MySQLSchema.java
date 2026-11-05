package ch.njol.skript.variables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
	- Defines and checks the table used by {@link PooledMySQLStorage}.

	- On startup, the current table is created if it does not exist. If it already
	exists, it is checked before variables are loaded or written to make sure the
	table is set up the way the backend expects.

	- The name is stored as normal Unicode text, while the type and value contain
	Skript's serialized data. A fixed-size hash is also stored and indexed so long
	names do not have to be used as the database key.

	- When changing a variable, the backend checks the full name as well as the hash
	so a hash collision does not cause the wrong variable to be changed. This class
	does not care what the serialized value actually contains.

	- The caller owns the database connection. This class only handles its own
	temporary statements and results. If the table is incompatible, startup fails
	instead of changing or migrating the table. {@link Variables} then handles the
	configured fallback.

	- The regular {@link SQLStorage} adapters do not use this table definition.
*/

final class MySQLSchema {

	private MySQLSchema() {}

	/** Schema validation failure with a message safe to display without connection details. */
	static final class ValidationException extends SQLException {
		ValidationException(String message) {
			super(message);
		}
	}

	static void initialize(Connection connection, String table) throws SQLException {
		table = PooledMySQLStorage.identifier(table);
		execute(connection, "CREATE TABLE IF NOT EXISTS `" + table + "` ("
				+ "name LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, "
				+ "type VARCHAR(255) NOT NULL, hash BINARY(32) PRIMARY KEY, value LONGBLOB) "
				+ "ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin");
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
			statement.setQueryTimeout(10);
			statement.setString(1, table);
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next())
					throw new ValidationException("Configured table is not visible in information_schema.TABLES");
				if (!"InnoDB".equalsIgnoreCase(result.getString(1)))
					throw new ValidationException("MySQL variable table must use InnoDB");
			}
		}
		Map<String, String> columns = new HashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY,CHARACTER_SET_NAME,COLLATION_NAME FROM information_schema.COLUMNS "
						+ "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
			statement.setQueryTimeout(10);
			statement.setString(1, table);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					String column = result.getString(1);
					String type = result.getString(2).toLowerCase(Locale.ROOT);
					columns.put(column, type);
					if (!("hash".equals(column) ? "PRI" : "").equals(result.getString(3)))
						throw new ValidationException("MySQL variable table must have hash as its only primary-key column and no other indexed columns");
					if ("name".equals(column) && "longtext".equals(type)
							&& !("utf8mb4".equals(result.getString(4)) && "utf8mb4_bin".equals(result.getString(5))))
						throw new ValidationException("MySQL variable names must use utf8mb4 character set and utf8mb4_bin collation");
				}
			}
		}
		if (!Map.of("hash", "binary(32)", "name", "longtext",
				"type", "varchar(255)", "value", "longblob").equals(columns))
			throw new ValidationException("MySQL variable table must contain exactly: name LONGTEXT, type VARCHAR(255), hash BINARY(32), value LONGBLOB");
	}

	private static void execute(Connection connection, String sql) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setQueryTimeout(30);
			statement.executeUpdate();
		}
	}
}
