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
	 * Testing method {@link Utils#getSuperType(Class...)}
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
