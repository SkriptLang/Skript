package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Writes one save's rows: upserts, tombstones included, and deletions by name. Rows are collected and sent as statements
 * of up to {@code maxRows} rows, sooner if the next row would make the statement bigger than {@code maxBytes}. A row is
 * always sent, even alone over the limit, so this can't get stuck. A full statement is prepared once per save and reused;
 * shorter ones, at the end or cut short by size, are prepared when sent. Closing it sends what's left.
 * <p>
 * Statements of many rows beat the driver's {@link PreparedStatement#addBatch()}: MySQL's driver copies every row's
 * parameters and escapes each string twice, about 3 KB of garbage per row, and without its rewriting sends each delete
 * on its own. SQLite is a little faster this way too.
 * <p>
 * It only sends what it's given. What can be saved, and whether a deletion is a tombstone, is the storage's choice.
 */
final class RowWriter implements AutoCloseable {

	private final Upserts upserts;
	private final Deletes deletes;

	/**
	 * @param connection the connection, in the save's transaction
	 * @param table      the table, already checked to be a plain name
	 * @param upsertSql  the upsert statement for a number of rows; see {@link JdbcStorage#upsertSql}
	 * @param maxRows    the most rows one statement may cover
	 * @param maxBytes   the most bytes one statement may take, as the driver sends it
	 * @param version    the version to stamp each upserted row with
	 * @param server     what to put in each upserted row's server column
	 */
	RowWriter(Connection connection, String table, IntFunction<String> upsertSql, int maxRows, long maxBytes,
	          long version, String server) {
		upserts = new Upserts(connection, maxRows, maxBytes, upsertSql, version, server);
		deletes = new Deletes(connection, maxRows, maxBytes, table);
	}

	/**
	 * Writes a row: a tombstone if {@code value} is null.
	 */
	void upsert(String name, SerializedVariable.@Nullable Value value) throws SQLException {
		upserts.add(new Upsert(name, value));
	}

	/**
	 * Removes a row.
	 */
	void delete(String name) throws SQLException {
		deletes.add(name);
	}

	/**
	 * Sends what's left. Deletions go last, but a save has one change per name, so the order doesn't matter.
	 */
	@Override
	public void close() throws SQLException {
		try (deletes) {
			upserts.close();
		}
	}

	/**
	 * @return At most how many bytes a row adds to an upsert statement, which the driver sends as text: up to 3 UTF-8
	 * bytes per character, doubled when escaped, for the name and type, 2 per byte for the value's hex, and 100 for the
	 * version, the server, quotes and commas
	 */
	static long upsertBytes(String name, SerializedVariable.@Nullable Value value) {
		long bytes = 6L * name.length() + 100;
		if (value != null)
			bytes += 6L * value.type.length() + 2L * value.data.length;
		return bytes;
	}

	private record Upsert(String name, SerializedVariable.@Nullable Value value) {}

	/**
	 * Collects rows of one kind and sends them as statements of several rows.
	 *
	 * @param <R> what one row is
	 */
	private abstract static class Sender<R> implements AutoCloseable {

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
		private final long version;
		private final String server;

		Upserts(Connection connection, int maxRows, long maxBytes, IntFunction<String> sql, long version, String server) {
			super(connection, maxRows, maxBytes);
			this.sql = sql;
			this.version = version;
			this.server = server;
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
			if (row.value() == null) { // a tombstone: both null
				statement.setNull(offset + 2, Types.VARCHAR);
				statement.setNull(offset + 3, Types.BLOB);
			} else {
				statement.setString(offset + 2, row.value().type);
				statement.setBytes(offset + 3, row.value().data);
			}
			statement.setLong(offset + 4, version);
			statement.setString(offset + 5, server);
			return 5;
		}

	}

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

}
