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
package org.skriptlang.skript.test.tests.utils;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import ch.njol.skript.util.Utils;

/**
 * Test methods from the Utils class.
 */
public class UtilsTest {

	/**
	 * Example of how to fake a Bukkit server using EasyMock
	 */
//	@Before
//	public void fakeServer() throws Exception {
//		if (Bukkit.getServer() == null) {
//			Logger logger = Logger.getLogger(getClass().getCanonicalName());
//			logger.setParent(SkriptLogger.LOGGER);
//			logger.setLevel(Level.WARNING);
//	
//			Server server = createMock(Server.class);
//			server.getLogger();
//			expectLastCall().andReturn(logger).anyTimes();
//			server.isPrimaryThread();
//			expectLastCall().andReturn(true).anyTimes();
//			server.getName();
//			expectLastCall().andReturn("Whatever").anyTimes();
//			server.getVersion();
//			expectLastCall().andReturn("2.0").anyTimes();
//			server.getBukkitVersion();
//			expectLastCall().andReturn("2.0").anyTimes();
//			replay(server);
//
//			Bukkit.setServer(server);
//		}
//	}

	/**
	 * Testing method Utils.getEnglishPlural
	 */
	@Test
	public void testPlural() {
		String[][] strings = {
				{"house", "houses"},
				{"cookie", "cookies"},
				{"creeper", "creepers"},
				{"cactus", "cacti"},
				{"rose", "roses"},
				{"dye", "dyes"},
				{"name", "names"},
				{"ingot", "ingots"},
				{"derp", "derps"},
				{"sheep", "sheep"},
				{"choir", "choirs"},
				{"man", "men"},
				{"child", "children"},
				{"hoe", "hoes"},
				{"toe", "toes"},
				{"hero", "heroes"},
				{"kidney", "kidneys"},
				{"anatomy", "anatomies"},
				{"axe", "axes"},
				{"elf", "elfs"},
				{"knife", "knives"},
				{"shelf", "shelfs"},
		};
		for (String[] s : strings) {
			assertEquals(s[1], Utils.toEnglishPlural(s[0]));
			assertEquals(s[0], Utils.getEnglishPlural(s[1]).getFirst());
		}
	}

	/**
	 * Testing method Utils.getSuperType
	 */
	@Test
	public void testSuperClass() {
		Class<?>[][] classes = {
				{Object.class, Object.class},
				{String.class, String.class},
				{String.class, Object.class, Object.class},
				{Object.class, String.class, Object.class},
				{String.class, String.class, String.class},
				{Object.class, String.class, Object.class, String.class, Object.class},
				{Double.class, Integer.class, Number.class},
				{UnknownHostException.class, FileNotFoundException.class, IOException.class},
				{SortedMap.class, TreeMap.class, SortedMap.class},
				{LinkedList.class, ArrayList.class, AbstractList.class},
				{List.class, Set.class, Collection.class},
				{ArrayList.class, Set.class, Collection.class},
		};
		for (Class<?>[] cs : classes) {
			assertEquals(cs[cs.length - 1], Utils.getSuperType(Arrays.copyOf(cs, cs.length - 1)));
		}
	}

}
