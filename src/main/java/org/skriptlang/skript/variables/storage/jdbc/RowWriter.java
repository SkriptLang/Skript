package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Writes one save's rows: upserts, deletions by name, and, if the table is monitored, its entries in the change log.
 * Rows are collected and sent as statements of up to {@code maxRows} rows, sooner if the next row would make the
 * statement bigger than {@code maxBytes}. A row is always sent, even alone over the limit, so this can't get stuck.
 * A full statement is prepared once per save and reused; shorter ones, at the end or cut short by size, are prepared
 * when sent. Closing it sends what's left.
 * <p>
 * Statements of many rows beat the driver's {@link PreparedStatement#addBatch()}: MySQL's driver copies every row's
 * parameters and escapes each string twice, about 3 KB of garbage per row, and without its rewriting sends each delete
 * on its own. SQLite is a little faster this way too.
 * <p>
 * Log entries are numbered in the order they're added, so add a save's clears before its changes: a server reading the
 * log applies them in that order. Each replaces its name's earlier entry, so the log keeps one per name.
 * It only sends what it's given. What can be saved is the storage's choice.
 */
final class RowWriter implements AutoCloseable {

	private final Upserts upserts;
	private final Deletes deletes;
	private final @Nullable LogEntries log;

	/**
	 * @param connection the connection, in the save's transaction
	 * @param table      the table, already checked to be a plain name
	 * @param upsertSql  the upsert statement for a number of rows; see {@link JdbcStorage#upsertSql}
	 * @param maxRows    the most rows one statement may cover
	 * @param maxBytes   the most bytes one statement may take, as the driver sends it
	 * @param logSql     the change log's upsert for a number of entries, each bound as name, version and seq, to note
	 *                   each row and clear in, or null if the table isn't monitored
	 * @param version    the save's version, which its log entries are filed under
	 */
	RowWriter(Connection connection, String table, IntFunction<String> upsertSql, int maxRows, long maxBytes,
	          @Nullable IntFunction<String> logSql, long version) {
		upserts = new Upserts(connection, maxRows, maxBytes, upsertSql);
		deletes = new Deletes(connection, maxRows, maxBytes, table);
		log = logSql == null ? null : new LogEntries(connection, maxRows, maxBytes, logSql, version);
	}

	/**
	 * Logs that a list was cleared. The clear itself is a range delete, which the storage sends.
	 *
	 * @param listName the list's full name, such as {@code a::*}
	 */
	void cleared(String listName) throws SQLException {
		if (log != null)
			log.add(listName);
	}

	/**
	 * Writes a row.
	 */
	void upsert(String name, SerializedVariable.Value value) throws SQLException {
		upserts.add(new Upsert(name, value));
		if (log != null)
			log.add(name);
	}

	/**
	 * Removes a row.
	 */
	void delete(String name) throws SQLException {
		deletes.add(name);
		if (log != null)
			log.add(name);
	}

	/**
	 * Sends what's left. Deletions go after upserts, but a save has one change per name, so the order doesn't matter.
	 */
	@Override
	public void close() throws SQLException {
		try (log; deletes) {
			upserts.close();
		}
	}

	/**
	 * @return At most how many bytes a row adds to an upsert statement, which the driver sends as text: up to 3 UTF-8
	 * bytes per character, doubled when escaped, for the name and type, 2 per byte for the value's hex, and 100 for the
	 * quotes and commas
	 */
	static long upsertBytes(String name, SerializedVariable.Value value) {
		return 6L * name.length() + 100 + 6L * value.type.length() + 2L * value.data.length;
	}

	private record Upsert(String name, SerializedVariable.Value value) {}

	/**
	 * Collects rows of one kind and sends them as statements of several rows.
	 *
	 * @param <R> what one row is
	 */
	abstract static class Sender<R> implements AutoCloseable {

		private final Connection connection;
		private final int maxRows;
		private final long maxBytes;
		private final List<R> rows = new ArrayList<>();
		private long bytes;
		private @Nullable PreparedStatement full;

		Sender(Connection connection, int maxRows, long maxBytes) {
			this.connection = connection;
			this.maxRows = maxRows;
			this.maxBytes = maxBytes;
		}

		/**
		 * @return The statement for that many rows
		 */
		abstract String sql(int rows);

		/**
		 * @return At most how many bytes the row adds to a statement, as the driver sends it
		 */
		abstract long bytes(R row);

		/**
		 * Binds the row's parameters, starting after parameter {@code offset}.
		 *
		 * @return How many parameters it bound
		 */
		abstract int bind(PreparedStatement statement, int offset, R row) throws SQLException;

		final void add(R row) throws SQLException {
			long size = bytes(row);
			if (!rows.isEmpty() && bytes + size > maxBytes)
				send();
			rows.add(row);
			bytes += size;
			if (rows.size() == maxRows)
				send();
		}

		private void send() throws SQLException {
			if (rows.size() == maxRows) {
				if (full == null)
					full = connection.prepareStatement(sql(maxRows));
				bindAndRun(full);
			} else {
				try (PreparedStatement statement = connection.prepareStatement(sql(rows.size()))) {
					bindAndRun(statement);
				}
			}
			rows.clear();
			bytes = 0;
		}

		private void bindAndRun(PreparedStatement statement) throws SQLException {
			int offset = 0;
			for (R row : rows)
				offset += bind(statement, offset, row);
			statement.executeUpdate();
		}

		@Override
		public void close() throws SQLException {
			try {
				if (!rows.isEmpty())
					send();
			} finally {
				if (full != null)
					full.close();
			}
		}

	}

	private static final class Upserts extends Sender<Upsert> {

		private final IntFunction<String> sql;

		Upserts(Connection connection, int maxRows, long maxBytes, IntFunction<String> sql) {
			super(connection, maxRows, maxBytes);
			this.sql = sql;
		}

		@Override
		String sql(int rows) {
			return sql.apply(rows);
		}

		@Override
		long bytes(Upsert row) {
			return upsertBytes(row.name(), row.value());
		}

		@Override
		int bind(PreparedStatement statement, int offset, Upsert row) throws SQLException {
			statement.setString(offset + 1, row.name());
			statement.setString(offset + 2, row.value().type);
			statement.setBytes(offset + 3, row.value().data);
			return 3;
		}

	}

	/**
	 * Deletes rows by name, as {@code DELETE ... WHERE name IN (...)} statements of several names.
	 */
	private static final class Deletes extends Sender<String> {

		private final String table;

		Deletes(Connection connection, int maxRows, long maxBytes, String table) {
			super(connection, maxRows, maxBytes);
			this.table = table;
		}

		@Override
		String sql(int rows) {
			return "DELETE FROM " + table + " WHERE name IN (" + "?, ".repeat(rows - 1) + "?)";
		}

		@Override
		long bytes(String name) {
			return 6L * name.length() + 10; // up to 3 UTF-8 bytes per character, doubled when escaped, plus quotes and a comma
		}

		@Override
		int bind(PreparedStatement statement, int offset, String name) throws SQLException {
			statement.setString(offset + 1, name);
			return 1;
		}

	}

	/**
	 * Notes names in the change log, each with the save's version and its number within the save, replacing the name's
	 * earlier entry.
	 */
	private static final class LogEntries extends Sender<String> {

		private final IntFunction<String> sql;
		private final long version;
		private int seq; // the next entry's number within the save

		LogEntries(Connection connection, int maxRows, long maxBytes, IntFunction<String> sql, long version) {
			super(connection, maxRows, maxBytes);
			this.sql = sql;
			this.version = version;
		}

		@Override
		String sql(int rows) {
			return sql.apply(rows);
		}

		@Override
		long bytes(String name) {
			return 6L * name.length() + 60; // as for a delete, plus two numbers
		}

		@Override
		int bind(PreparedStatement statement, int offset, String name) throws SQLException {
			// numbered as they're bound, which is the order they were added in, since rows are sent in order
			statement.setString(offset + 1, name);
			statement.setLong(offset + 2, version);
			statement.setInt(offset + 3, seq++);
			return 3;
		}

	}

}
