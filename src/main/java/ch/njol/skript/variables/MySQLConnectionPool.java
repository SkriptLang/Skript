package ch.njol.skript.variables;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
	- Manages the connection used by {@link PooledMySQLStorage}.
	
	- The pool gives us a connection we can reuse and replaces it if it breaks,
	which can happen pretty often with MySQL.
	
	- Everything is done one at a time. The backend only has one database worker,
	so we don't need multiple connections handling transactions at the same time.
	
	- The pool is initialized before the worker starts using it. Callers close their
	connection handles after they're done, and the backend closes the pool when
	it shuts down. This class does not handle concurrent access itself.
	
	- {@link MySQLStorage} uses the same database settings, such as the driver,
	encoding, timeouts, and TLS settings. It does not use this pool, though.
*/

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
		// Paper owns the shared driver; Skript only closes its own connections.
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
