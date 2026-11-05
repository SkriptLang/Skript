package ch.njol.skript.variables;

import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import org.junit.Test;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class MySQLLifecycleTest {

	@Test
	public void shutdownCommitsQueuedChangesBeforeAcknowledgingJournal() throws Exception {
		runShutdown(false);
	}

	@Test
	public void unavailableDatabaseLeavesQueuedChangesInRecoveryJournalOnShutdown() throws Exception {
		runShutdown(true);
	}

	private void runShutdown(boolean unavailable) throws Exception {
		org.junit.Assume.assumeNotNull(org.bukkit.Bukkit.getServer());
		var path = Files.createTempDirectory("mysql-shutdown-test").resolve("journal.bin");
		ConnectionPoolDataSource source = createMock(ConnectionPoolDataSource.class);
		PooledConnection physical = createNiceMock(PooledConnection.class);
		Connection connection = createNiceMock(Connection.class);
		PreparedStatement statement = createNiceMock(PreparedStatement.class);
		ResultSet rows = createNiceMock(ResultSet.class);
		AtomicBoolean committed = new AtomicBoolean();
		if (unavailable) {
			expect(source.getPooledConnection()).andThrow(new SQLException("unavailable")).anyTimes();
		} else {
			expect(source.getPooledConnection()).andReturn(physical);
			expect(physical.getConnection()).andReturn(connection).anyTimes();
			expect(connection.isValid(5)).andReturn(true).anyTimes();
			expect(connection.prepareStatement(anyString())).andReturn(statement).anyTimes();
			expect(statement.executeQuery()).andReturn(rows).anyTimes();
			connection.commit();
			expectLastCall().andAnswer(() -> {
				assertFalse(new MySQLJournal(path, "test").read().isEmpty());
				committed.set(true);
				return null;
			}).atLeastOnce();
		}
		replay(source, physical, connection, statement, rows);
		List<LogEntry> errors = new ArrayList<>();
		PooledMySQLStorage storage = new PooledMySQLStorage(new MySQLConnectionPool(source), "variables_test") {
			private LogHandler handler;

			@Override
			void writeBatch(Map<String, SerializedVariable> batch) throws SQLException {
				// Handlers are thread-local: capture the worker's failure and shutdown reports.
				if (handler == null) {
					handler = new LogHandler() {
						@Override
						public LogResult log(LogEntry entry) {
							errors.add(entry);
							return LogResult.DO_NOT_LOG;
						}
					}.start();
				}
				super.writeBatch(batch);
			}

			@Override
			protected void disconnect() {
				try {
					super.disconnect();
				} finally {
					if (handler != null)
						handler.stop();
				}
			}
		};
		// Supply the same journal that load_i normally creates, without requiring a live database.
		var journalField = PooledMySQLStorage.class.getDeclaredField("journal");
		journalField.setAccessible(true);
		journalField.set(storage, new MySQLJournal(path, "test"));
		var journalFileField = PooledMySQLStorage.class.getDeclaredField("journalFile");
		journalFileField.setAccessible(true);
		journalFileField.set(storage, path.toFile());
		try {
			storage.save(new SerializedVariable("list::1", new SerializedVariable.Value("string", new byte[]{1, 2, 3})));
			storage.save(new SerializedVariable("list::2", null));
			storage.allLoaded();
			storage.close();
			var recovered = new MySQLJournal(path, "test").read();
			assertEquals(!unavailable, committed.get());
			if (unavailable) {
				assertEquals(List.of(
						"MySQL persistence failed. 2 changes remain pending; retrying. "
								+ "Check database availability and free disk space for the recovery file.",
						"MySQL stopped with 2 pending variables saved in " + path + " for recovery."),
						errors.stream().map(LogEntry::getMessage).toList());
				errors.forEach(entry -> assertEquals(Level.SEVERE, entry.getLevel()));
				assertArrayEquals(new byte[]{1, 2, 3}, recovered.get("list::1").value.data);
				assertNull(recovered.get("list::2").value);
			} else {
				assertTrue(recovered.isEmpty());
				assertTrue("Successful shutdown must not log errors: " + errors, errors.isEmpty());
			}
			verify(source, physical, connection, statement, rows);
		} finally {
			storage.close();
			Files.deleteIfExists(path);
			Files.delete(path.getParent());
		}
	}
}
