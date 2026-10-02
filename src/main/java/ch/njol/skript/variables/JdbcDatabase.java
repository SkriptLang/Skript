package ch.njol.skript.variables;

import ch.njol.skript.Skript;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
	- Handles the JDBC connection used by {@link SQLStorage} and its adapters.
	
	- {@link ConnectionFactory} handles the database-specific connection setup,
	so {@link SQLiteStorage} can use the connection
	and statement code without needing another database plugin.
	
	- This class opens connections, prepares statements, and cleans up resources.
	- It does not handle serializers, variable routing, commits, or retries.
	- Those are handled by the storage class using it.
	
	- Statements and results returned by this class must be closed by the caller.
	
	- This class does not handle synchronization itself. {@link SQLStorage} handles
	access using its database lock. {@link PooledMySQLStorage} works differently
	and uses {@link MySQLConnectionPool} because its worker has its own connection
	and recovery setup.
*/

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
