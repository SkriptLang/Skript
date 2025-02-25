package org.skriptlang.skript.config;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfigTest {

	@Test
	public void testValueNodeSettingGetting() {
		Map<ConfigSection, List<ConfigNode>> nodes = new HashMap<>();

		ConfigSection section = new ConfigSection("section", "", new String[]{"comment"});

		nodes.put(null, List.of(
			new ConfigEntry<>("one", 1, "", new String[0]),
			new ConfigEntry<>("two", 2, "", new String[0]),
			section,
			new ConfigEntry<>("four", 4, "", new String[0])
		));

		nodes.put(section, List.of(
			new ConfigEntry<>("three", 3, "", new String[0]),
			new ConfigEntry<>("false", false, "", new String[0])
		));

		ConfigImpl config = new ConfigImpl(nodes);

		assertEquals(1, (int) config.getValue("one", 0));
		assertFalse(config.getValue("section.false", true));
		assertNotNull(config.getNode("section"));
	}

	@Test
	public void testParsing() {
		Config config = getConfig("new-config");

		System.out.println(config);
	}

	public static void main(String[] args) {
		ConfigTest test = new ConfigTest();
		test.testValueNodeSettingGetting();
		test.testParsing();
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/" + name + ".sk")) {
			assertNotNull(resource);
			return new ConfigImpl(resource);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
