package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Optional, single-server MySQL storage; does not participate in legacy file migration. */
final class PooledMySQLStorage extends VariablesStorage {

	private final Queue<SerializedVariable> incoming = new ConcurrentLinkedQueue<>();
	private final Map<String, SerializedVariable> pending = new LinkedHashMap<>();
	private MySQLConnectionPool pool;
	private MySQLJournal journal;
	private String table;
	private Thread worker;
	private volatile boolean stopping;
	private long lastError;

	PooledMySQLStorage() {
		super("optional MySQL");
	}

	PooledMySQLStorage(MySQLConnectionPool pool, String table) {
		this();
		this.pool = pool;
		this.table = identifier(table);
	}

	@Override
	protected boolean load_i(SectionNode config) {
		try {
			if (!"true".equalsIgnoreCase(config.getValue("enabled")))
				return false;
			if (!".*".equals(config.getValue("pattern")))
				throw new IllegalArgumentException("Optional MySQL requires pattern: .* (it replaces all databases)");
			if ("true".equalsIgnoreCase(config.getValue("monitor changes")))
				throw new IllegalArgumentException("Optional MySQL does not support monitoring or multiple servers");
			String host = required(config, "host");
			String database = required(config, "database");
			String user = required(config, "user");
			int port = port(required(config, "port"));
			table = identifier(required(config, "table"));
			String sslMode = required(config, "ssl mode");
			if (!Set.of("VERIFY_IDENTITY", "VERIFY_CA", "REQUIRED", "DISABLED").contains(sslMode))
				throw new IllegalArgumentException("Unsupported MySQL ssl mode");
			String password = config.getValue("password");
			if (password == null)
				throw new IllegalArgumentException("Missing MySQL password entry (an empty value is allowed)");
			File folder = Skript.getInstance().getDataFolder();
			journal = new MySQLJournal(new File(folder, "mysql-pending.bin").toPath(),
					host + ":" + port + "/" + database + "/" + table);
			pending.putAll(journal.read());
			pool = new MySQLConnectionPool(host, port, database, user, password, sslMode);
			Map<String, SerializedVariable> loaded = new LinkedHashMap<>();
			try (Connection connection = pool.acquire()) {
				executeUpdate(connection, "CREATE TABLE IF NOT EXISTS `" + table + "` ("
						+ "name_hash BINARY(32) PRIMARY KEY, name LONGBLOB NOT NULL, "
						+ "type VARCHAR(255) NOT NULL, small_value VARBINARY(32), value LONGBLOB) "
						+ "ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin");
				verifySchema(connection);
				try (PreparedStatement statement = connection.prepareStatement(
						"SELECT name_hash, name, type, small_value, value FROM `" + table + "`")) {
					statement.setQueryTimeout(10);
					try (ResultSet rows = statement.executeQuery()) {
						while (rows.next()) {
							byte[] nameBytes = rows.getBytes(2);
							if (nameBytes == null || !Arrays.equals(hash(nameBytes), rows.getBytes(1)))
								throw new SQLException("Invalid variable key in MySQL table");
							String name = new String(nameBytes, StandardCharsets.UTF_8);
							String type = rows.getString(3);
							byte[] small = rows.getBytes(4);
							byte[] large = rows.getBytes(5);
							if (type == null || (small == null) == (large == null))
								throw new SQLException("Invalid variable value in MySQL table");
							loaded.put(name, new SerializedVariable(name,
									new SerializedVariable.Value(type, small != null ? small : large)));
						}
					}
				}
			}

			// Overlay recovery before deserializing, including deletions. Never partially publish a failed load.
			for (SerializedVariable variable : pending.values()) {
				if (variable.value == null)
					loaded.remove(variable.name);
				else
					loaded.put(variable.name, variable);
			}
			Map<String, Object> values = Task.callSync(() -> {
				Map<String, Object> result = new LinkedHashMap<>();
				for (SerializedVariable variable : loaded.values()) {
					ClassInfo<?> info = Classes.getClassInfoNoError(variable.value.type);
					Object value;
					try {
						value = info == null || info.getSerializer() == null ? null
								: Classes.deserialize(info, variable.value.data);
					} catch (RuntimeException e) {
						return null;
					}
					if (value == null)
						return null;
					result.put(variable.name, value);
				}
				return result;
			});
			if (values == null)
				throw new IllegalArgumentException("Unrecognized or malformed MySQL variable value");
			// Verify write permission and transaction support before publishing any variables.
			verifyWritable();
			journal.write(pending);
			Task.callSync(() -> {
				values.forEach((name, value) -> Variables.variableLoaded(name, value, this));
				return null;
			});
			return true;
		} catch (Exception | LinkageError e) {
			// JDBC exceptions may contain connection details; report only safe diagnostics.
			String reason = e instanceof IllegalArgumentException ? e.getMessage() : e.getClass().getSimpleName();
			Skript.error("Cannot initialize optional MySQL: " + reason
					+ ". Check the driver, settings, TLS certificates, table schema and database permissions.");
			disconnect();
			return false;
		}
	}

	private void verifySchema(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
			statement.setString(1, table);
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next() || !"InnoDB".equalsIgnoreCase(result.getString(1)))
					throw new SQLException("MySQL variable table must use InnoDB");
			}
		}
		Map<String, String> expected = Map.of("name_hash", "binary(32)", "name", "longblob",
				"type", "varchar(255)", "small_value", "varbinary(32)", "value", "longblob");
		Map<String, String> actual = new HashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY FROM information_schema.COLUMNS "
						+ "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
			statement.setString(1, table);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					String column = result.getString(1);
					actual.put(column, result.getString(2).toLowerCase(Locale.ROOT));
					if (!("name_hash".equals(column) ? "PRI" : "").equals(result.getString(3)))
						throw new SQLException("Unsupported MySQL variable indexes");
				}
			}
		}
		if (!expected.equals(actual))
			throw new SQLException("Unsupported MySQL variable table schema");
	}

	private void verifyWritable() throws SQLException {
		try (Connection connection = pool.acquire()) {
			connection.setAutoCommit(false);
			try {
				executeUpdate(connection, "INSERT INTO `" + table
						+ "` (name_hash,name,type,value) SELECT NULL,NULL,NULL,NULL WHERE FALSE");
				executeUpdate(connection, "UPDATE `" + table + "` SET type=type WHERE FALSE");
				executeUpdate(connection, "DELETE FROM `" + table + "` WHERE FALSE");
			} finally {
				connection.rollback();
			}
		}
	}

	static String required(SectionNode config, String key) {
		String value = config.getValue(key);
		if (value == null || value.isBlank())
			throw new IllegalArgumentException("Missing MySQL setting: " + key);
		return value;
	}

	static int port(String value) {
		try {
			int port = Integer.parseInt(value);
			if (port >= 1 && port <= 65535)
				return port;
		} catch (NumberFormatException ignored) {}
		throw new IllegalArgumentException("MySQL port must be between 1 and 65535");
	}

	static String identifier(String value) {
		if (!value.matches("[A-Za-z_][A-Za-z0-9_]{0,63}"))
			throw new IllegalArgumentException("MySQL table must be an SQL identifier of at most 64 characters");
		return value;
	}

	static byte[] hash(byte[] name) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(name);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

	// Scalar serialized payloads fit inline; all other values use the unchanged generic serialization.
	static boolean small(SerializedVariable.Value value) {
		return value.data.length <= 32 && Set.of("boolean", "number", "uuid").contains(value.type);
	}

	@Override
	void save(SerializedVariable variable) {
		incoming.add(variable);
	}

	@Override
	protected boolean save(String name, @Nullable String type, byte @Nullable [] value) {
		save(new SerializedVariable(name, type == null ? null : new SerializedVariable.Value(type, value)));
		return true;
	}

	@Override
	protected void allLoaded() {
		worker = Skript.newThread(() -> {
			try {
				do {
					SerializedVariable variable;
					while ((variable = incoming.poll()) != null)
						pending.put(variable.name, variable);
					if (!pending.isEmpty()) {
						try {
							journal.write(pending);
							writeBatch(pending);
							// Keep pending until the committed snapshot is also durably acknowledged.
							journal.write(Collections.emptyMap());
							pending.clear();
						} catch (Exception e) {
							pool.invalidate();
							reportFailure();
						}
					}
					if (!stopping) {
						try {
							Thread.sleep(pending.isEmpty() ? 500 : 5000);
						} catch (InterruptedException ignored) {}
					}
				} while (!stopping || !incoming.isEmpty());
			} finally {
				if (!pending.isEmpty()) {
					try {
						journal.write(pending);
						Skript.error("MySQL stopped with " + pending.size()
								+ " pending variables saved in mysql-pending.bin for recovery.");
					} catch (IOException e) {
						Skript.error("CRITICAL: Could not preserve " + pending.size()
								+ " pending MySQL variables on disk. These changes may be lost on shutdown.");
					}
				}
				disconnect();
			}
		}, "Skript optional MySQL writer");
		worker.start();
	}

	private void reportFailure() {
		long now = System.currentTimeMillis();
		if (now - lastError >= 30000) {
			Skript.error("MySQL persistence failed. " + pending.size() + " changes remain pending; retrying. "
					+ "Check database availability and free disk space for mysql-pending.bin.");
			lastError = now;
		}
	}

	void writeBatch(Map<String, SerializedVariable> batch) throws SQLException {
		try (Connection connection = pool.acquire()) {
			connection.setAutoCommit(false);
			try (PreparedStatement lookup = connection.prepareStatement(
					"SELECT name FROM `" + table + "` WHERE name_hash=? FOR UPDATE");
					PreparedStatement delete = connection.prepareStatement(
							"DELETE FROM `" + table + "` WHERE name_hash=?");
					PreparedStatement write = connection.prepareStatement("INSERT INTO `" + table
							+ "` (name_hash,name,type,small_value,value) VALUES (?,?,?,?,?) "
							+ "ON DUPLICATE KEY UPDATE type=VALUES(type),small_value=VALUES(small_value),value=VALUES(value)")) {
				lookup.setQueryTimeout(5);
				delete.setQueryTimeout(5);
				write.setQueryTimeout(5);
				for (SerializedVariable variable : batch.values()) {
					byte[] name = variable.name.getBytes(StandardCharsets.UTF_8);
					byte[] hash = hash(name);
					lookup.setBytes(1, hash);
					try (ResultSet row = lookup.executeQuery()) {
						if (row.next() && !Arrays.equals(name, row.getBytes(1)))
							throw new SQLException("Variable name hash collision; refusing to overwrite");
					}
					if (variable.value == null) {
						delete.setBytes(1, hash);
						delete.addBatch();
					} else {
						write.setBytes(1, hash);
						write.setBytes(2, name);
						write.setString(3, variable.value.type);
						boolean small = small(variable.value);
						write.setBytes(4, small ? variable.value.data : null);
						write.setBytes(5, small ? null : variable.value.data);
						write.addBatch();
					}
				}
				write.executeBatch();
				delete.executeBatch();
				connection.commit();
			} catch (SQLException e) {
				try {
					connection.rollback();
				} catch (SQLException rollback) {
					e.addSuppressed(rollback);
				}
				throw e;
			}
		}
	}

	@Override
	public void close() {
		super.close();
		stopping = true;
		if (worker != null) {
			worker.interrupt();
			boolean interrupted = false;
			while (worker.isAlive()) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	protected File getFile(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean connect() {
		return false; // Connections are acquired exclusively by the writer.
	}

	@Override
	protected void disconnect() {
		if (pool != null)
			pool.close();
	}

	private static void executeUpdate(Connection connection, String sql) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setQueryTimeout(10);
			statement.executeUpdate();
		}
	}
}
