package org.skriptlang.skript.variables.storage;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.ConfigReader;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.StorageAccessor;

public class H2StorageTest {

	private static final boolean ENABLED = Skript.classExists("com.zaxxer.hikari.HikariConfig");
	private final String testSection =
			"h2:\n" +
				"\tpattern: .*\n" +
				"\tfile: ./plugins/Skript/variables\n" +
				"\tbackup interval: 0";

	private H2Storage database;

	@Before
	public void setup() {
		if (!ENABLED)
			return;
		Config config;
		try {
			config = new Config(new ByteArrayInputStream(testSection.getBytes(ConfigReader.UTF_8)), "h2-junit.sk", false, false, ":");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		assertTrue(config != null);
		StorageAccessor.clearVariableStorages();
		database = new H2Storage(Skript.getAddonInstance(), "H2");
		SectionNode section = new SectionNode("h2", "", config.getMainNode(), 0);
		section.add(new EntryNode("pattern", ".*", section));
		section.add(new EntryNode("file", "./plugins/Skript/variables", section));
		section.add(new EntryNode("backup interval", "0", section));
		assertTrue(database.load_i(section));
	}

	@Test
	public void testStorage() throws SQLException, InterruptedException, ExecutionException, TimeoutException {
		if (!ENABLED)
			return;
		synchronized (database) {
			assertTrue(database.save("testing", "string", Classes.serialize("Hello World!").data));
//			SerializedVariable result = database.executeTestQuery();
//			assertTrue(result != null);
		}
	}

}
