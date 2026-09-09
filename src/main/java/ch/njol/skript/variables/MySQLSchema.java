package ch.njol.skript.variables;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Initializes and upgrades only the recognized optional MySQL variable schemas. */
final class MySQLSchema {

	private MySQLSchema() {}

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
				if (!result.next() || !"InnoDB".equalsIgnoreCase(result.getString(1)))
					throw new SQLException("MySQL variable table must use InnoDB");
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
					if (!(("name_hash".equals(column) || "hash".equals(column)) ? "PRI" : "").equals(result.getString(3)))
						throw new SQLException("Unsupported MySQL variable indexes");
					if ("name".equals(column) && "longtext".equals(type)
							&& !("utf8mb4".equals(result.getString(4)) && "utf8mb4_bin".equals(result.getString(5))))
						throw new SQLException("MySQL variable names must use utf8mb4_bin");
				}
			}
		}
		migrate(connection, table, columns);
	}

	static void migrate(Connection connection, String table, Map<String, String> columns) throws SQLException {
		table = PooledMySQLStorage.identifier(table);
		Map<String, String> expected = new HashMap<>(Map.of("hash", "binary(32)", "name", "longtext",
				"type", "varchar(255)", "value", "longblob"));
		boolean legacyHash = columns.containsKey("name_hash");
		if (legacyHash) {
			expected.remove("hash");
			expected.put("name_hash", "binary(32)");
		}
		boolean binaryNames = "longblob".equals(columns.get("name"));
		boolean compactValues = columns.containsKey("small_value");
		if (binaryNames)
			expected.put("name", "longblob");
		if (compactValues)
			expected.put("small_value", "varbinary(32)");
		if (!expected.equals(columns))
			throw new SQLException("Unsupported MySQL variable table schema");
		if (!binaryNames && !compactValues && !legacyHash)
			return;

		if (binaryNames) {
			// Reject invalid encodings before ALTER TABLE could replace bytes in permissive SQL mode.
			try (PreparedStatement statement = connection.prepareStatement("SELECT name FROM `" + table + "`")) {
				statement.setQueryTimeout(10);
				try (ResultSet result = statement.executeQuery()) {
					while (result.next())
						decodeName(result.getBytes(1));
				}
			}
		}
		if (compactValues) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM `" + table
					+ "` WHERE small_value IS NOT NULL AND value IS NOT NULL AND small_value <> value LIMIT 1")) {
				statement.setQueryTimeout(10);
				try (ResultSet result = statement.executeQuery()) {
					if (result.next())
						throw new SQLException("Conflicting MySQL payload columns; refusing to discard data");
				}
			}
			// Idempotent if shutdown interrupts migration before the subsequent atomic DDL.
			execute(connection, "UPDATE `" + table + "` SET value=small_value WHERE value IS NULL AND small_value IS NOT NULL");
		}
		String alteration = "MODIFY COLUMN name LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL FIRST, "
				+ "MODIFY COLUMN type VARCHAR(255) NOT NULL AFTER name, "
				+ (legacyHash ? "CHANGE COLUMN name_hash hash" : "MODIFY COLUMN hash")
				+ " BINARY(32) NOT NULL AFTER type, MODIFY COLUMN value LONGBLOB AFTER hash";
		if (compactValues)
			alteration += ", DROP COLUMN small_value";
		execute(connection, "ALTER TABLE `" + table + "` " + alteration);
	}

	static String decodeName(byte[] bytes) throws SQLException {
		if (bytes == null)
			throw new SQLException("Null MySQL variable name");
		try {
			return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			throw new SQLException("Invalid UTF-8 MySQL variable name; migration stopped without altering names", e);
		}
	}

	private static void execute(Connection connection, String sql) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setQueryTimeout(30);
			statement.executeUpdate();
		}
	}
}
