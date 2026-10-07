package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.skriptlang.skript.variables.storage.H2Storage;
import org.skriptlang.skript.variables.storage.SQLiteStorage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import static org.junit.Assert.*;

public class JdbcStorageTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private SectionNode configuration(String file) throws Exception {
		Config config = new Config(new ByteArrayInputStream(new byte[0]), "jdbc-test", false, false, ":");
		SectionNode node = new SectionNode("jdbc-test", "", config.getMainNode(), 0);
		node.add(new EntryNode("pattern", ".*", node));
		node.add(new EntryNode("file", file, node));
		node.add(new EntryNode("table", "test_variables", node));
		node.add(new EntryNode("backup interval", "0", node));
		node.add(new EntryNode("required changes per save", "1000000", node));
		return node;
	}

	@Test
	public void mysqlReplacementWritesInH2CompatibilityMode() throws Exception {
		checkMysqlReplacementWrites(false);
	}

	@Test
	public void mysqlReplacementWritesSupportExistingThreeColumnTable() throws Exception {
		checkMysqlReplacementWrites(true);
	}

	private void checkMysqlReplacementWrites(boolean existingTable) throws Exception {
		if (existingTable) {
			try (Connection connection = new org.h2.Driver().connect(
				"jdbc:h2:" + folder.getRoot().toPath().resolve("mysql") + ";MODE=MySQL;NON_KEYWORDS=VALUE", new java.util.Properties());
				 var statement = connection.createStatement()) {
				statement.execute("CREATE TABLE test_variables (name VARCHAR(380) PRIMARY KEY, type VARCHAR(50), value BLOB)");
			}
		}
		// Exercises the MySQL schema/write adapter without requiring an external server.
		class MysqlDialectStorage extends org.skriptlang.skript.variables.storage.MySQLStorage {
			MysqlDialectStorage() { super(Skript.instance(), "mysql"); }
			@Override
			protected com.zaxxer.hikari.HikariConfig configuration(SectionNode node) {
				var config = new com.zaxxer.hikari.HikariConfig();
				config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
				config.addDataSourceProperty("URL", "jdbc:h2:" + folder.getRoot().toPath().resolve("mysql") + ";MODE=MySQL;NON_KEYWORDS=VALUE");
				return config;
			}
			@Override
			protected String createTableQuery() {
				return super.createTableQuery().replace(" CHARACTER SET utf8mb4 COLLATE utf8mb4_bin", "");
			}
		}
		JdbcStorage first = new MysqlDialectStorage();
		assertTrue(first.loadConfig(configuration("ignored")));
		try {
			first.setVariable("mysql::value", "initial");
			assertTrue(first.flush());
			// H2 REPLACE only matches primary keys; MySQL also matches unique keys.
			if (existingTable)
				first.setVariable("mysql::value", "updated");
			first.setVariable("mysql::deleted", "delete me");
			assertTrue(first.flush());
			first.setVariable("mysql::deleted", null);
			assertTrue(first.flush());
		} finally {
			first.close();
		}
		JdbcStorage reopened = new MysqlDialectStorage();
		assertTrue(reopened.loadConfig(configuration("ignored")));
		try {
			assertEquals(existingTable ? "updated" : "initial", reopened.getVariable("mysql::value"));
			assertNull(reopened.getVariable("mysql::deleted"));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void sqlitePersistence() throws Exception {
		checkPersistence(false);
	}

	@Test
	public void h2Persistence() throws Exception {
		checkPersistence(true);
	}

	private JdbcStorage storage(boolean h2) {
		return h2 ? new H2Storage(Skript.instance(), "h2") : new SQLiteStorage(Skript.instance(), "sqlite");
	}

	private void checkPersistence(boolean h2) throws Exception {
		String file = folder.getRoot().toPath().resolve(h2 ? "variables.mv.db" : "variables.db").toString();
		JdbcStorage first = storage(h2);
		assertTrue(first.loadConfig(configuration(file)));
		try {
			first.setVariable("list_%::1", "one");
			first.setVariable("list_%::2", "two");
			first.setVariable("listXY::1", "other");
			assertTrue(first.flush());
		} finally {
			first.close();
		}
		JdbcStorage second = storage(h2);
		assertTrue(second.loadConfig(configuration(file)));
		try {
			assertEquals(0, second.loadedVariables());
			second.setVariable("list_%::1", "updated");
			Map<?, ?> list = (Map<?, ?>) second.getVariable("list_%::*");
			assertEquals("updated", list.get("1"));
			assertEquals("two", list.get("2"));
			second.setVariable("list_%::*", null);
			assertNull(second.getVariable("list_%::2"));
			assertTrue(second.flush());
		} finally {
			second.close();
		}
		JdbcStorage third = storage(h2);
		assertTrue(third.loadConfig(configuration(file)));
		try {
			assertNull(third.getVariable("list_%::*"));
			assertEquals("other", third.getVariable("listXY::1"));
			third.setVariable("list_%::3", "new");
			assertTrue(third.flush());
		} finally {
			third.close();
		}
	}

	@Test
	public void failedTransactionRetainsWritesAndDeletions() throws Exception {
		class FailingStorage extends SQLiteStorage {
			boolean fail;
			FailingStorage() { super(Skript.instance(), "sqlite"); }
			@Override
			protected void beforeSave(Connection connection) throws SQLException {
				if (fail)
					throw new SQLException("Injected transaction failure");
			}
		}
		String file = folder.getRoot().toPath().resolve("retry.db").toString();
		FailingStorage storage = new FailingStorage();
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("old", "old");
			assertTrue(storage.flush());
			storage.setVariable("old", null);
			storage.setVariable("new", "first");
			storage.fail = true;
			assertFalse(storage.flush());
			storage.setVariable("new", "latest");
			storage.fail = false;
			assertTrue(storage.flush());
		} finally {
			storage.close();
		}
		JdbcStorage reopened = storage(false);
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertNull(reopened.getVariable("old"));
			assertEquals("latest", reopened.getVariable("new"));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void invalidTableNamesAreRejected() throws Exception {
		JdbcStorage storage = storage(false);
		SectionNode node = configuration(folder.getRoot().toPath().resolve("invalid.db").toString());
		node.set("table", "variables; DROP TABLE variables");
		try {
			assertFalse(storage.loadConfig(node));
		} finally {
			storage.close();
		}
	}
	@Test
	public void backupsIncludeUnloadedValuesAndRespectRetention() throws Exception {
		for (boolean h2 : new boolean[]{false, true}) {
			String file = folder.getRoot().toPath().resolve(h2 ? "backup.mv.db" : "backup.db").toString();
			JdbcStorage first = storage(h2);
			assertTrue(first.loadConfig(configuration(file)));
			try {
				first.setVariable("unloaded", "preserved");
				assertTrue(first.flush());
			} finally {
				first.close();
			}
			JdbcStorage reopened = storage(h2);
			assertTrue(reopened.loadConfig(configuration(file)));
			try {
				assertEquals(0, reopened.loadedVariables());
				java.nio.file.Path backup = folder.getRoot().toPath().resolve(h2 ? "h2.csv" : "sqlite.csv");
				reopened.writeBackup(backup);
				assertTrue(java.nio.file.Files.readString(backup).contains("unloaded"));
				assertEquals(0, reopened.loadedVariables());
			} finally {
				reopened.close();
			}
		}
	}

	@Test
	public void existingSqliteTablesKeepAcceptingWrites() throws Exception {
		String file = folder.getRoot().toPath().resolve("legacy.db").toString();
		try (Connection connection = java.sql.DriverManager.getConnection("jdbc:sqlite:" + file);
			 var statement = connection.createStatement()) {
			statement.execute("CREATE TABLE test_variables (name VARCHAR(380) PRIMARY KEY, "
				+ "type VARCHAR(50), value BLOB, update_guid CHAR(36) NOT NULL)");
		}
		JdbcStorage storage = storage(false);
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("entry", "first");
			assertTrue(storage.flush());
			storage.setVariable("entry", "second");
			assertTrue(storage.flush());
		} finally {
			storage.close();
		}
		JdbcStorage reopened = storage(false);
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertEquals("second", reopened.getVariable("entry"));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void oversizedWriteRollsBackAndCanBeCorrected() throws Exception {
		String file = folder.getRoot().toPath().resolve("oversized.db").toString();
		JdbcStorage storage = storage(false);
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("old", "original");
			assertTrue(storage.flush());
			storage.setVariable("old", null);
			storage.setVariable("large", "x".repeat(JdbcStorage.MAX_VALUE_SIZE + 100));
			assertFalse(storage.flush());
			try (Connection connection = java.sql.DriverManager.getConnection("jdbc:sqlite:" + file);
				 var statement = connection.createStatement();
				 var rows = statement.executeQuery("SELECT COUNT(*) FROM test_variables WHERE name = 'old'")) {
				assertTrue(rows.next());
				assertEquals(1, rows.getInt(1));
			}
			storage.setVariable("large", "corrected");
			assertTrue(storage.flush());
		} finally {
			storage.close();
		}
		JdbcStorage reopened = storage(false);
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertNull(reopened.getVariable("old"));
			assertEquals("corrected", reopened.getVariable("large"));
		} finally {
			reopened.close();
		}
	}

}
