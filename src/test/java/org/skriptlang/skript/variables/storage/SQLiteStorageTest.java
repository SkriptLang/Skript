/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.variables.storage;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.storage.H2Storage;
import org.skriptlang.skript.variables.storage.SQLiteStorage;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.StorageAccessor;
import ch.njol.skript.variables.Variables;

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
			config = new Config(testSection, "sqlite-junit.sk", false, false, ":");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		assertTrue(config != null);
		StorageAccessor.clearVariableStorages();
		database = new SQLiteStorage(Skript.getAddonInstance(), "H2");
		SectionNode section = new SectionNode("sqlite", "", config.getMainNode(), 0);
		section.add(new EntryNode("pattern", ".*", section));
		section.add(new EntryNode("monitor interval", "30 seconds", section));
		section.add(new EntryNode("file", "./plugins/Skript/variables.db", section));
		section.add(new EntryNode("backup interval", "0", section));
		assertTrue(database.load(section));
	}

	@Test
	public void testStorage() throws SQLException, InterruptedException, ExecutionException, TimeoutException {
		if (!ENABLED)
			return;
		synchronized (database) {
			assertTrue(database.save("testing", "string", Classes.serialize("Hello World!").data));
//			SerializedVariable result = database.executeTestQuery();
//			assertTrue(result != null);
//			System.out.println(result.getName());
		}
	}

}
