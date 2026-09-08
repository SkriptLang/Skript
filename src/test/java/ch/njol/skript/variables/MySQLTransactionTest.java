package ch.njol.skript.variables;

import org.junit.Test;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class MySQLTransactionTest {

	@Test
	public void invalidConnectionIsDiscardedBeforeNextAcquire() throws Exception {
		ConnectionPoolDataSource source = createMock(ConnectionPoolDataSource.class);
		PooledConnection broken = createMock(PooledConnection.class);
		PooledConnection replacement = createMock(PooledConnection.class);
		Connection stale = createMock(Connection.class);
		Connection fresh = createMock(Connection.class);
		expect(source.getPooledConnection()).andReturn(broken).andReturn(replacement);
		expect(broken.getConnection()).andReturn(stale);
		expect(stale.isValid(5)).andReturn(false);
		stale.close();
		broken.close();
		expect(replacement.getConnection()).andReturn(fresh);
		expect(fresh.isValid(5)).andReturn(true);
		fresh.close();
		replacement.close();
		replay(source, broken, replacement, stale, fresh);
		try (MySQLConnectionPool pool = new MySQLConnectionPool(source)) {
			assertThrows(SQLException.class, pool::acquire);
			try (Connection connection = pool.acquire()) {
				assertSame(fresh, connection);
			}
		}
		verify(source, broken, replacement, stale, fresh);
	}

	@Test
	public void poolReusesPhysicalConnectionAndReplacesBrokenConnection() throws Exception {
		ConnectionPoolDataSource source = createMock(ConnectionPoolDataSource.class);
		PooledConnection first = createMock(PooledConnection.class);
		PooledConnection second = createMock(PooledConnection.class);
		Connection handle = createMock(Connection.class);
		expect(source.getPooledConnection()).andReturn(first);
		expect(first.getConnection()).andReturn(handle).times(2);
		expect(handle.isValid(5)).andReturn(true).times(3);
		first.close();
		expect(source.getPooledConnection()).andReturn(second);
		expect(second.getConnection()).andReturn(handle);
		second.close();
		replay(source, first, second, handle);
		try (MySQLConnectionPool pool = new MySQLConnectionPool(source)) {
			assertSame(handle, pool.acquire());
			assertSame(handle, pool.acquire());
			pool.invalidate();
			assertSame(handle, pool.acquire());
		}
		verify(source, first, second, handle);
	}

	@Test
	public void updatesAndDeletesCommitTogether() throws Exception {
		runBatch(false);
	}

	@Test
	public void failedCommitRollsBackAndLeavesInputAvailableForRetry() throws Exception {
		runBatch(true);
	}

	private void runBatch(boolean failCommit) throws Exception {
		ConnectionPoolDataSource source = createMock(ConnectionPoolDataSource.class);
		PooledConnection physical = createMock(PooledConnection.class);
		Connection connection = createMock(Connection.class);
		PreparedStatement lookup = createMock(PreparedStatement.class);
		PreparedStatement write = createMock(PreparedStatement.class);
		PreparedStatement delete = createMock(PreparedStatement.class);
		ResultSet result = createMock(ResultSet.class);
		expect(source.getPooledConnection()).andReturn(physical);
		expect(physical.getConnection()).andReturn(connection);
		expect(connection.isValid(5)).andReturn(true);
		connection.setAutoCommit(false);
		expect(connection.prepareStatement(startsWith("SELECT"))).andReturn(lookup);
		expect(connection.prepareStatement(startsWith("INSERT"))).andReturn(write);
		expect(connection.prepareStatement(startsWith("DELETE"))).andReturn(delete);
		lookup.setQueryTimeout(5);
		write.setQueryTimeout(5);
		delete.setQueryTimeout(5);
		lookup.setBytes(eq(1), anyObject(byte[].class));
		expectLastCall().times(2);
		expect(lookup.executeQuery()).andReturn(result).times(2);
		expect(result.next()).andReturn(false).times(2);
		result.close();
		expectLastCall().times(2);
		write.setBytes(eq(1), anyObject(byte[].class));
		write.setBytes(eq(2), aryEq("list::1".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		write.setString(3, "number");
		write.setBytes(eq(4), aryEq(new byte[]{1, 2, 3}));
		write.setBytes(5, null);
		write.addBatch();
		delete.setBytes(eq(1), anyObject(byte[].class));
		delete.addBatch();
		expect(write.executeBatch()).andReturn(new int[]{1});
		expect(delete.executeBatch()).andReturn(new int[]{1});
		connection.commit();
		if (failCommit) {
			expectLastCall().andThrow(new SQLException("simulated connection loss"));
			connection.rollback();
		}
		write.close();
		delete.close();
		lookup.close();
		connection.close();
		physical.close();
		replay(source, physical, connection, lookup, write, delete, result);
		Map<String, SerializedVariable> batch = new LinkedHashMap<>();
		batch.put("list::1", new SerializedVariable("list::1", new SerializedVariable.Value("number", new byte[]{1, 2, 3})));
		batch.put("list::2", new SerializedVariable("list::2", null));
		try (MySQLConnectionPool pool = new MySQLConnectionPool(source)) {
			PooledMySQLStorage storage = new PooledMySQLStorage(pool, "variables_test");
			if (failCommit)
				assertThrows(SQLException.class, () -> storage.writeBatch(batch));
			else
				storage.writeBatch(batch);
			assertEquals(2, batch.size());
			assertNull(batch.get("list::2").value);
		}
		verify(source, physical, connection, lookup, write, delete, result);
	}
}
