package ch.njol.skript.variables;

import ch.njol.skript.Skript;
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

/**
	- Optional MySQL backend selected by the main MySQL configuration.
	It implements {@link VariablesStorage} directly instead of using the older
	{@link SQLStorage} adapters.

	- Every change comes in as a {@link SerializedVariable}. Skript's shared
	serializers decide how the data is stored, not this backend.

	- On startup, {@link MySQLSchema} makes sure the table is set up correctly.
	The backend then loads the database rows and applies any changes saved in
	{@link MySQLJournal}. Values are decoded on the server thread before they
	are passed to {@link Variables}. If a row cannot be read, it is kept and
	reported instead of being silently lost.

	- If initialization fails, the normal configured storage can be used instead.
	This also prevents partially loaded data from being published.

	- After startup, one worker handles {@link MySQLConnectionPool}, the journal,
	and the pending changes. Other threads add changes to a queue. The worker
	combines changes for the same variable, saves them to the journal, and then
	writes them to MySQL using a transaction.

	- The journal is only cleared after the database transaction succeeds. This
	means failed or uncertain changes can be tried again.

	- When shutting down, the backend finishes accepted changes and waits for the
	worker to finish its final database attempt or recovery write. If MySQL fails,
	the pending changes are kept. If even the recovery write fails, the error is
	reported.

	- Writes happen in the background, so changes that were still only in memory
	when the server suddenly crashes can be lost.

	- This backend does not support multiple servers sharing data or automatically
	syncing with another storage system.
*/

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
			Map<String, SerializedVariable> loaded = new TreeMap<>();
			try (Connection connection = pool.acquire()) {
				MySQLSchema.initialize(connection, table);
				try (PreparedStatement statement = connection.prepareStatement(
						"SELECT name, type, hash, value FROM `" + table + "` ORDER BY name")) {
					statement.setQueryTimeout(10);
					try (ResultSet rows = statement.executeQuery()) {
						while (rows.next()) {
							String name = rows.getString(1);
							byte[] nameBytes = name == null ? null : name.getBytes(StandardCharsets.UTF_8);
							if (nameBytes == null || !Arrays.equals(hash(nameBytes), rows.getBytes(3))) {
								Skript.error("Invalid MySQL variable key; row retained for recovery.");
								continue;
							}
							String type = rows.getString(2);
							byte[] data = rows.getBytes(4);
							if (type == null || data == null) {
								Skript.error("Invalid MySQL value for {" + name + "}; row retained for recovery.");
								continue;
							}
							loaded.put(name, new SerializedVariable(name,
									new SerializedVariable.Value(type, data)));
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
			Map<String, Object> values = Task.callSync(() -> decodeValues(loaded));
			if (values == null)
				throw new IllegalStateException("MySQL deserialization task did not complete");
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

	Map<String, Object> decodeValues(Map<String, SerializedVariable> loaded) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (SerializedVariable variable : loaded.values()) {
			try {
				var info = Classes.getClassInfoNoError(variable.value.type);
				if (info == null || info.getSerializer() == null)
					throw new IOException("Unknown persisted type");
				Object decoded = Classes.deserialize(info, variable.value.data);
				if (decoded == null)
					throw new IOException("Unreadable persisted value");
				result.put(variable.name, decoded);
			} catch (Exception | LinkageError e) {
				Skript.error("Cannot restore MySQL variable {" + variable.name + "} (type "
						+ variable.value.type + ", " + e.getClass().getSimpleName()
						+ "). Its raw data is retained; other variables will still load.");
			}
		}
		return result;
	}

	private void verifyWritable() throws SQLException {
		try (Connection connection = pool.acquire()) {
			connection.setAutoCommit(false);
			try {
				executeUpdate(connection, "INSERT INTO `" + table
						+ "` (hash,name,type,value) SELECT NULL,NULL,NULL,NULL WHERE FALSE");
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
					"SELECT name FROM `" + table + "` WHERE hash=? FOR UPDATE");
					PreparedStatement delete = connection.prepareStatement(
							"DELETE FROM `" + table + "` WHERE hash=?");
					PreparedStatement write = connection.prepareStatement("INSERT INTO `" + table
							+ "` (hash,name,type,value) VALUES (?,?,?,?) "
							+ "ON DUPLICATE KEY UPDATE type=VALUES(type),value=VALUES(value)")) {
				lookup.setQueryTimeout(5);
				delete.setQueryTimeout(5);
				write.setQueryTimeout(5);
				for (SerializedVariable variable : batch.values()) {
					byte[] name = variable.name.getBytes(StandardCharsets.UTF_8);
					byte[] hash = hash(name);
					lookup.setBytes(1, hash);
					try (ResultSet row = lookup.executeQuery()) {
						if (row.next() && !variable.name.equals(row.getString(1)))
							throw new SQLException("Variable name hash collision; refusing to overwrite");
					}
					if (variable.value == null) {
						delete.setBytes(1, hash);
						delete.addBatch();
					} else {
						write.setBytes(1, hash);
						write.setString(2, variable.name);
						write.setString(3, variable.value.type);
						write.setBytes(4, variable.value.data);
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

	/**
	 * Called after the global dispatcher has drained. Waits for the final database
	 * attempt or durable recovery write, then releases the worker-owned connection.
	 */
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
