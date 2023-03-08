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
package org.skriptlang.skript.test.tests.localization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.njol.skript.util.Utils;

public class UtilsPlurals {

	/**
	 * Testing method {@link Utils#getEnglishPlural(String)}
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

}
