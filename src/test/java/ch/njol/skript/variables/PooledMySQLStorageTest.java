package ch.njol.skript.variables;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;

public class PooledMySQLStorageTest {

	@Test
	public void shippedConfigKeepsCsvAsDefault() throws Exception {
		Config config = new Config(getClass().getResourceAsStream("/config.sk"), "config.sk", false, true, ":");
		SectionNode databases = (SectionNode) config.getMainNode().get("databases");
		SectionNode mysql = (SectionNode) databases.get("mysql variables");
		assertNull(config.getMainNode().get("mysql"));
		assertEquals("disabled", mysql.getValue("type"));
		assertNotNull(mysql.getValue("password"));
		SectionNode defaults = (SectionNode) databases.get("default");
		assertEquals("CSV", defaults.getValue("type"));
		assertEquals("./plugins/Skript/variables.csv", defaults.getValue("file"));
	}

	@Test
	public void databasePatternsApplyToMySQL() {
		org.junit.Assume.assumeNotNull(org.bukkit.Bukkit.getServer());
		SectionNode config = config("players", java.util.Map.of("pattern", "player::.*"));
		PooledMySQLStorage storage = new MySQLStorage("MySQL") {
			@Override
			protected boolean load_i(SectionNode section) {
				return false; // Only test the shared configuration, without opening JDBC.
			}
		};
		assertFalse(storage.load(config));
		assertTrue(storage.accept("player::name"));
		assertFalse(storage.accept("global::name"));
	}

	@Test
	public void journalsAreStableAndSeparateForEachDatabase() {
		SectionNode first = config("first", java.util.Map.of());
		SectionNode second = config("second", java.util.Map.of());
		assertEquals(PooledMySQLStorage.recoveryFileName(first), PooledMySQLStorage.recoveryFileName(first));
		assertNotEquals(PooledMySQLStorage.recoveryFileName(first), PooledMySQLStorage.recoveryFileName(second));
		assertEquals("mysql-pending.bin", PooledMySQLStorage.recoveryFileName(
				config("first", java.util.Map.of("recovery file", "mysql-pending.bin"))));
		assertThrows(IllegalArgumentException.class, () -> PooledMySQLStorage.recoveryFileName(
				config("first", java.util.Map.of("recovery file", "../pending.bin"))));
	}

	private static SectionNode config(String name, java.util.Map<String, String> settings) {
		return new SectionNode(name, "", new Config("test", null).getMainNode(), 1) {
			@Override
			public String getValue(String key) {
				return settings.get(key);
			}
		};
	}

	@Test
	public void invalidInitializationReturnsFailureWithoutPublishingVariables() {
		var settings = java.util.Map.of("pattern", ".*", "host", "localhost",
				"database", "skript", "user", "test", "port", "0");
		SectionNode config = new SectionNode("mysql", "", new Config("test", null).getMainNode(), 1) {
			@Override
			public String getValue(String key) {
				return settings.get(key);
			}
		};
		PooledMySQLStorage storage = new PooledMySQLStorage();
		try (var handler = new ch.njol.skript.log.LogHandler() {
			@Override
			public LogResult log(ch.njol.skript.log.LogEntry entry) {
				assertTrue(entry.getMessage().startsWith("Cannot initialize MySQL:"));
				return LogResult.DO_NOT_LOG;
			}
		}.start()) {
			assertFalse(storage.load_i(config));
		}
		storage.close();
	}

	@Test
	public void validatesPortsAndRejectsSqlIdentifiers() throws Exception {
		assertEquals(3306, PooledMySQLStorage.port("3306"));
		for (String port : new String[]{"", "abc", "0", "-1", "65536", "999999999999"})
			assertThrows(IllegalArgumentException.class, () -> PooledMySQLStorage.port(port));
		Config config = new Config(getClass().getResourceAsStream("/config.sk"), "config.sk", false, true, ":");
		SectionNode databases = (SectionNode) config.getMainNode().get("databases");
		SectionNode mysql = (SectionNode) databases.get("mysql variables");
		String configuredTable = PooledMySQLStorage.required(mysql, "table");
		assertEquals(configuredTable, PooledMySQLStorage.identifier(configuredTable));
		for (String table : new String[]{"", "x; DROP TABLE users", "`variables`", "a.b", "x".repeat(65)})
			assertThrows(IllegalArgumentException.class, () -> PooledMySQLStorage.identifier(table));
	}

	@Test
	public void longUnicodeAndNestedNamesHaveDistinctKeys() {
		String[] names = {"foo", "foo::bar", "foo::bar::1", "foo ", "Foo", "玩家::😀", "x".repeat(100_000)};
		for (int i = 0; i < names.length; i++) {
			byte[] hash = PooledMySQLStorage.hash(names[i].getBytes(StandardCharsets.UTF_8));
			assertEquals(32, hash.length);
			for (int j = 0; j < i; j++)
				assertFalse(Arrays.equals(hash, PooledMySQLStorage.hash(names[j].getBytes(StandardCharsets.UTF_8))));
		}
	}
}
