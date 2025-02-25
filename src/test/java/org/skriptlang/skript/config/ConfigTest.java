package org.skriptlang.skript.config;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfigTest {

	@Test
	public void testEntry() {
		ConfigEntry<Integer> entry = new ConfigEntry<>("one", 1,
			"inline here!!",
			new String[]{"", "hello comment", "how are you", ""});

		assertEquals("\n# hello comment\n# how are you\n\none: 1 # inline here!!", entry.toString());
	}

	@Test
	public void testSection() {
		ConfigSection section = new ConfigSection("two",
			"inline here!!",
			new String[]{"", "hello comment", "how are you", ""});

		assertEquals("\n# hello comment\n# how are you\n\ntwo: # inline here!!", section.toString());
	}

	@Test
	public void testSerialization() {
		List<String> original = getLines("config");
		String[] serialized = getConfig("config").toString().split("\n");

		for (int i = 0; i < original.size(); i++) {
			String originalLine = original.get(i);
			if (originalLine.isBlank()) {
				assertTrue("Line %d differs".formatted(i + 1), serialized[i].isBlank());
				continue;
			}

			assertEquals("Line %d differs".formatted(i + 1), originalLine, serialized[i]);
		}
	}

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

		System.out.println(config.getNode("a"));
		System.out.println("====================");
		System.out.println(config.getNode("a.g"));
		System.out.println("====================");
		System.out.println(config.getNode("a.f"));
		System.out.println("====================");
		System.out.println(config);
	}

	public static void main(String[] args) {
		ConfigTest test = new ConfigTest();
		test.testEntry();
		test.testSection();
		test.testSerialization();
		System.out.println("====================");
		test.testValueNodeSettingGetting();
		System.out.println("====================");
		test.testParsing();
	}

	private List<String> getLines(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/%s.sk".formatted(name))) {
			assert resource != null;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
				return reader.lines().toList();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/%s.sk".formatted(name))) {
			assertNotNull(resource);
			return new ConfigImpl(resource);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
