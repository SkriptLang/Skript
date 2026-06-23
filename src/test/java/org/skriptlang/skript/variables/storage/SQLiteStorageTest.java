package org.skriptlang.skript.variables.storage;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.ConfigReader;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.StorageAccessor;

public class SQLiteStorageTest {

	private static final boolean ENABLED = Skript.classExists("com.zaxxer.hikari.HikariConfig");
	private final String testSection =
			"sqlite:\n" +
				"\tpattern: .*\n" +
				"\tmonitor interval: 30 seconds\n" +
				"\tfile: ./plugins/Skript/variables.db\n" +
				"\tbackup interval: 0";

	private SQLiteStorage database;

	@Before
	public void setup() {
		if (!ENABLED)
			return;
		Config config;
		try {
			config = new Config(new ByteArrayInputStream(testSection.getBytes(ConfigReader.UTF_8)), "sqlite-junit.sk", false, false, ":");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		StorageAccessor.clearVariableStorages();
		database = new SQLiteStorage(Skript.instance(), "H2");
		SectionNode section = new SectionNode("sqlite", "", config.getMainNode(), 0);
		section.add(new EntryNode("pattern", ".*", section));
		section.add(new EntryNode("monitor interval", "30 seconds", section));
		section.add(new EntryNode("file", "./plugins/Skript/variables.db", section));
		section.add(new EntryNode("backup interval", "0", section));
		assertTrue(database.loadConfig(section));
	}

	@Test
	public void testStorage() {
		// TODO
	}

}
