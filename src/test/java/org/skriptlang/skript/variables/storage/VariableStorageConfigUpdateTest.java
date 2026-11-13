package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Tests {@link VariableStorageManager#migratePatterns(SectionNode)}, which updates old configs from patterns to prefixes.
 * Needs a server, since configs are parsed through Skript.
 */
public class VariableStorageConfigUpdateTest {

	private static Config config(String databases) throws IOException {
		return new Config(new ByteArrayInputStream(("databases:\n" + databases).getBytes(StandardCharsets.UTF_8)),
			"test.sk", false, true, ":");
	}

	private static SectionNode databases(Config config) {
		return (SectionNode) config.getMainNode().get("databases");
	}

	private static SectionNode database(Config config, String name) {
		return (SectionNode) databases(config).get(name);
	}

	@Test
	public void simplePatternsBecomePrefixes() throws IOException {
		Config config = config("""
			\tkills:
			\t\ttype: csv
			\t\tpattern: kills::.*
			\tmoney:
			\t\tpattern: (?i)(bank::|tax::).*
			""");
		assertTrue(VariableStorageManager.migratePatterns(databases(config)));
		assertNull(database(config, "kills").get("pattern"));
		assertEquals("kills::", database(config, "kills").getValue("prefix"));
		assertEquals("bank::, tax::", database(config, "money").getValue("prefix"));
	}

	@Test
	public void patternsMatchingEverythingAreRemoved() throws IOException {
		Config config = config("""
			\tdefault:
			\t\ttype: csv
			\t\tpattern: .*
			\t\tfile: ./variables.csv
			""");
		assertTrue(VariableStorageManager.migratePatterns(databases(config)));
		SectionNode database = database(config, "default");
		assertNull(database.get("pattern"));
		assertNull(database.get("prefix"));
		assertEquals("./variables.csv", database.getValue("file"));
	}

	@Test
	public void patternsThatArentPrefixesAreLeft() throws IOException {
		Config config = config("""
			\tnegated:
			\t\tpattern: (?!x_).*
			\tcomma:
			\t\tpattern: a\\,b.*
			\tspaced:
			\t\tpattern: kills .*
			""");
		assertFalse(VariableStorageManager.migratePatterns(databases(config)));
		assertEquals("(?!x_).*", database(config, "negated").getValue("pattern"));
		assertEquals("a\\,b.*", database(config, "comma").getValue("pattern"));
		assertEquals("kills .*", database(config, "spaced").getValue("pattern"));
	}

	@Test
	public void databasesWithAPrefixAreLeft() throws IOException {
		Config config = config("""
			\tkills:
			\t\tprefix: kills::
			\t\tpattern: deaths::.*
			""");
		assertFalse(VariableStorageManager.migratePatterns(databases(config)));
		assertEquals("kills::", database(config, "kills").getValue("prefix"));
		assertEquals("deaths::.*", database(config, "kills").getValue("pattern"));
	}

	@Test
	public void theUpdatedConfigSavesAndLoadsAgain() throws IOException {
		Config config = config("""
			\tscores:
			\t\tpattern: score##s::.*
			""");
		assertEquals("score#s::.*", database(config, "scores").getValue("pattern"));
		assertTrue(VariableStorageManager.migratePatterns(databases(config)));

		File file = File.createTempFile("skript-config", ".sk");
		try {
			config.save(file);
			Config saved = new Config(file, false, true, ":");
			assertEquals("score#s::", database(saved, "scores").getValue("prefix"));
			assertNull(database(saved, "scores").get("pattern"));
		} finally {
			Files.delete(file.toPath());
		}
	}

}
