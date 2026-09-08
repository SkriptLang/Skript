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
		SectionNode mysql = (SectionNode) config.getMainNode().get("mysql");
		assertEquals("false", mysql.getValue("enabled"));
		assertNotNull(mysql.getValue("password"));
		SectionNode databases = (SectionNode) config.getMainNode().get("databases");
		SectionNode defaults = (SectionNode) databases.get("default");
		assertEquals("CSV", defaults.getValue("type"));
		assertEquals("./plugins/Skript/variables.csv", defaults.getValue("file"));
	}

	@Test
	public void disabledOrAbsentFlagDoesNotReadConnectionSettings() {
		for (String enabled : new String[]{null, "false", "invalid"}) {
			SectionNode config = new SectionNode("mysql", "", new Config("test", null).getMainNode(), 1) {
				@Override
				public String getValue(String key) {
					assertEquals("Disabled backend must not read connection settings", "enabled", key);
					return enabled;
				}
			};
			assertFalse(new PooledMySQLStorage().load_i(config));
		}
	}

	@Test
	public void validatesPortsAndRejectsSqlIdentifiers() throws Exception {
		assertEquals(3306, PooledMySQLStorage.port("3306"));
		for (String port : new String[]{"", "abc", "0", "-1", "65536", "999999999999"})
			assertThrows(IllegalArgumentException.class, () -> PooledMySQLStorage.port(port));
		Config config = new Config(getClass().getResourceAsStream("/config.sk"), "config.sk", false, true, ":");
		SectionNode mysql = (SectionNode) config.getMainNode().get("mysql");
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
