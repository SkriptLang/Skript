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
package org.skriptlang.skript.test.tests.config;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import ch.njol.skript.config.Node;
import ch.njol.util.NonNullPair;

public class NodeTest {

	@Test
	public void splitLineTest() {
		String[][] data = {
				{"", "", ""},
				{"ab", "ab", ""},
				{"ab#", "ab", "#"},
				{"ab##", "ab#", ""},
				{"ab###", "ab#", "#"},
				{"#ab", "", "#ab"},
				{"ab#cd", "ab", "#cd"},
				{"ab##cd", "ab#cd", ""},
				{"ab###cd", "ab#", "#cd"},
				{"######", "", "######"},
				{"#######", "", "#######"},
				{"#### # ####", "", "#### # ####"},
				{"##### ####", "", "##### ####"},
				{"#### #####", "", "#### #####"},
				{"#########", "", "#########"},
				{"a##b#c##d#e", "a#b", "#c##d#e"},
				{" a ## b # c ## d # e ", " a # b ", "# c ## d # e "},
				{"a b \"#a  ##\" # b \"", "a b \"#a  ##\" ", "# b \""},
		};
		for (String[] d : data) {
			NonNullPair<String, String> p = Node.splitLine(d[0]);
			assertArrayEquals(d[0], new String[] {d[1], d[2]}, new String[] {p.getFirst(), p.getSecond()});
		}

	}

}
