package ch.njol.skript.util;

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

/**
 * Test methods from the Utils class.
 */
public class UtilsTest {

	/**
	 * Testing method {@link Utils#getSuperType(Class...)}.
	 */
	@Test
	public void testGetSuperType() {
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

	/**
	 * Testing methods {@link Utils#toEnglishPlural(String)} and {@link Utils#isPlural(String)}.
	 */
	@Test
	public void testIsPlural() {
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
			{"knife", "knives"},
			{"elf", "elves"},
			{"shelf", "shelves"},
			{"self", "selves"},
			{"gui", "guis"},
		};
		for (String[] s : strings) {
			assertEquals(s[1], Utils.toEnglishPlural(s[0]));
			assertEquals(s[0], Utils.isPlural(s[1]).updated());
		}
	}

}
