package ch.njol.skript.variables;

import ch.njol.skript.Skript;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Connection owner for the existing SQL storage schema. Access is serialized by SQLStorage. */
public final class JdbcDatabase implements AutoCloseable {

	@FunctionalInterface
	public interface ConnectionFactory {
		Connection open() throws SQLException;
	}

	private final ConnectionFactory factory;
	private Connection connection;

	public JdbcDatabase(ConnectionFactory factory) {
		this.factory = factory;
	}

	public boolean open() {
		try {
			if (connection != null && !connection.isClosed())
				return true;
			connection = factory.open();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed())
			throw new SQLException("Database connection is closed");
		return connection;
	}

	public PreparedStatement prepare(String sql) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(sql);
		statement.setQueryTimeout(10);
		return statement;
	}

	public ResultSet query(String sql) throws SQLException {
		PreparedStatement statement = prepare(sql);
		try {
			if (statement.execute()) {
				statement.closeOnCompletion();
				return statement.getResultSet();
			}
			statement.close();
			return null;
		} catch (SQLException e) {
			statement.close();
			throw e;
		}
	}

	@Override
	public void close() {
		if (connection == null)
			return;
		try {
			connection.close();
		} catch (SQLException e) {
			Skript.warning("Could not close a variable database connection");
		} finally {
			connection = null;
		}
	}
}
