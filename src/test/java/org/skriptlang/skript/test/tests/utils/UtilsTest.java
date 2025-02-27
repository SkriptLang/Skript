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
