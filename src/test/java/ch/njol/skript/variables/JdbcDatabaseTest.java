package ch.njol.skript.variables;

import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.*;

public class JdbcDatabaseTest {

	@Test
	public void standaloneSQLitePreservesLegacySchemaAndPreparedValues() throws Exception {
		org.sqlite.SQLiteDataSource source = new org.sqlite.SQLiteDataSource();
		source.setUrl("jdbc:sqlite::memory:");
		try (JdbcDatabase database = new JdbcDatabase(source::getConnection)) {
			assertTrue(database.open());
			var connection = database.getConnection();
			assertTrue(database.open());
			assertSame(connection, database.getConnection());
			database.query("CREATE TABLE variables21 (name TEXT PRIMARY KEY, type TEXT, value BLOB, update_guid TEXT)");
			connection.setAutoCommit(false);
			try (PreparedStatement write = database.prepare(
					"REPLACE INTO variables21 (name,type,value,update_guid) VALUES (?,?,?,?)")) {
				write.setString(1, "玩家::' OR 1=1 --");
				write.setString(2, "number");
				write.setBytes(3, new byte[]{1, 2, 3});
				write.setString(4, "test");
				assertEquals(1, write.executeUpdate());
			}
			connection.commit();
			try (ResultSet rows = database.query("SELECT name, value FROM variables21")) {
				assertTrue(rows.next());
				assertEquals("玩家::' OR 1=1 --", rows.getString(1));
				assertArrayEquals(new byte[]{1, 2, 3}, rows.getBytes(2));
				assertFalse(rows.next());
			}
			database.close();
			assertTrue(connection.isClosed());
		}
	}
}
