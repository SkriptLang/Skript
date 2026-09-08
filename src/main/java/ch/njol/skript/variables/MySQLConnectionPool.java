package ch.njol.skript.variables;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

/** A single physical connection, leased sequentially by the sole database writer. */
final class MySQLConnectionPool implements AutoCloseable {

	private final ConnectionPoolDataSource source;
	private PooledConnection pooled;

	MySQLConnectionPool(ConnectionPoolDataSource source) {
		this.source = source;
	}

	MySQLConnectionPool(String host, int port, String database,
			String user, String password, String sslMode) throws SQLException {
		source = dataSource(host, port, database, user, password, sslMode);
	}

	static com.mysql.cj.jdbc.MysqlConnectionPoolDataSource dataSource(String host, int port,
			String database, String user, String password, String sslMode) throws SQLException {
		var source = new com.mysql.cj.jdbc.MysqlConnectionPoolDataSource();
		source.setServerName(host);
		source.setPortNumber(port);
		source.setDatabaseName(database);
		source.setUser(user);
		source.setPassword(password);
		source.setConnectTimeout(5000);
		source.setSocketTimeout(5000);
		source.setSslMode(sslMode);
		source.setCharacterEncoding("UTF-8");
		// Skript closes every connection explicitly. Stop the private, relocated driver's
		// cleanup executor without changing JVM properties used by other plugins.
		com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.uncheckedShutdown();
		return source;
	}

	Connection acquire() throws SQLException {
		if (pooled == null)
			pooled = source.getPooledConnection();
		try {
			Connection connection = pooled.getConnection();
			if (!connection.isValid(5)) {
				connection.close();
				throw new SQLException("MySQL connection validation failed");
			}
			return connection;
		} catch (SQLException e) {
			invalidate();
			throw e;
		}
	}

	void invalidate() {
		if (pooled != null) {
			try {
				pooled.close();
			} catch (SQLException ignored) {}
			pooled = null;
		}
	}

	@Override
	public void close() {
		invalidate();
	}
}
