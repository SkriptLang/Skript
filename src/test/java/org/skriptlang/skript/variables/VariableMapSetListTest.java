package org.skriptlang.skript.variables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Checks that {@link VariableMap#setList} leaves the map in exactly the state that deleting the list
 * and then setting each element in turn with {@link VariableMap#setVariable} would, in both modes.
 */
@RunWith(Parameterized.class)
public class VariableMapSetListTest {

	@Parameters(name = "{0}")
	public static List<Object[]> modes() {
		return List.of(
			new Object[]{"concurrent", (Supplier<VariableMap>) VariableMap::concurrent},
			new Object[]{"local", (Supplier<VariableMap>) VariableMap::local}
		);
	}

	@Parameter
	public String mode;

	@Parameter(1)
	public Supplier<VariableMap> factory;

	private VariableMap map;

	@Before
	public void setUp() {
		map = factory.get();
	}

	/**
	 * Sets a list the slow way, one element at a time, as {@link VariableMap#setList} must match.
	 */
	private static void setListOneByOne(VariableMap map, String listName, String[] keys, Object[] values) {
		map.setVariable(listName + "::*", null);
		for (int i = 0; i < keys.length; i++)
			map.setVariable(listName + "::" + keys[i], values[i]);
	}

	private static VariableList list(VariableMap map, String name) {
		Object list = map.getVariable(name + "::*");
		assertNotNull(name + "::* is not a list", list);
		return (VariableList) list;
	}

	private static String[] keys(String... keys) {
		return keys;
	}

	private static Object[] values(Object... values) {
		return values;
	}

	@Test
	public void setsAList() {
		map.setList("l", keys("1", "2", "3"), values("a", "b", "c"));

		assertEquals("a", map.getVariable("l::1"));
		assertEquals("c", map.getVariable("l::3"));
		assertEquals(Map.of("l::1", "a", "l::2", "b", "l::3", "c"), map.getAll());
		assertEquals(4, list(map, "l").nextOpenIndex());
	}

	@Test
	public void replacesTheOldListAndItsSublists() {
		map.setVariable("l::1", "old");
		map.setVariable("l::x", "old");
		map.setVariable("l::x::y", "nested");
		map.setVariable("l::x::y::z", "deeper");

		map.setList("l", keys("2"), values("new"));

		assertNull(map.getVariable("l::1"));
		assertNull(map.getVariable("l::x"));
		assertNull(map.getVariable("l::x::y"));
		assertNull(map.getVariable("l::x::y::z"));
		assertEquals(Map.of("l::2", "new"), map.getAll());
	}

	@Test
	public void keepsTheListsOwnValue() {
		map.setVariable("l", "self");
		map.setVariable("l::1", "old");

		map.setList("l", keys("1", "2"), values("a", "b"));

		assertEquals("self", map.getVariable("l"));
		assertEquals("self", list(map, "l").ownValue());
		assertEquals("a", map.getVariable("l::1"));

		map.setVariable("l::*", null);
		assertEquals("self", map.getVariable("l"));
	}

	@Test
	public void keepsAPlainValueThatWasInTheWay() {
		map.setVariable("l", "plain");

		map.setList("l", keys("1"), values("a"));

		assertEquals("plain", map.getVariable("l"));
		assertEquals("plain", list(map, "l").ownValue());
		assertEquals("a", map.getVariable("l::1"));
	}

	@Test
	public void keepsAPlainValueInTheWayOfANestedList() {
		map.setVariable("a::b", "plain");

		map.setList("a::b::c", keys("1"), values("x"));

		assertEquals("plain", map.getVariable("a::b"));
		assertEquals("plain", list(map, "a::b").ownValue());
		assertEquals("x", map.getVariable("a::b::c::1"));
	}

	@Test
	public void sortsKeysAndLastRepeatWins() {
		map.setList("l", keys("b", "10", "2", "b", "a", "2"), values("b1", "ten", "two1", "b2", "a", "two2"));

		assertEquals(List.of("2", "10", "a", "b"), new ArrayList<>(list(map, "l").keySet()));
		assertEquals("b2", map.getVariable("l::b"));
		assertEquals("two2", map.getVariable("l::2"));
		assertEquals(4, map.size());
	}

	@Test
	public void noKeysDeletesTheList() {
		map.setVariable("l", "self");
		map.setVariable("l::1", "a");

		map.setList("l", keys(), values());

		assertNull(map.getVariable("l::1"));
		assertEquals("self", map.getVariable("l"));
		assertEquals(Map.of("l", "self"), map.getAll());
	}

	@Test
	public void addingAfterwardsUsesTheFirstGap() {
		map.setList("l", keys("1", "3"), values("a", "c"));
		assertEquals(2, list(map, "l").nextOpenIndex());
	}

	@Test
	public void doesNotModifyTheArrays() {
		String[] keys = keys("b", "a", "a");
		Object[] values = values("1", "2", "3");

		map.setList("l", keys, values);

		assertArrayEquals(keys("b", "a", "a"), keys);
		assertArrayEquals(values("1", "2", "3"), values);
	}

	// --- randomized comparison against setting one element at a time ---

	private static final String[] NAME_PARTS = {"a", "b", "1", "2", "x"};
	private static final String[] KEYS = {"1", "2", "3", "4", "10", "100", "01", "0", "-1", "1.5", "a", "b", "key", "2147483648"};

	@Test
	public void matchesSettingEachElement() {
		int operations = 0;
		for (long seed = 0; seed < 20; seed++) {
			Random random = new Random(seed);
			VariableMap bulk = factory.get();
			VariableMap reference = factory.get();

			for (int op = 0; op < 300; op++) {
				String name = randomName(random);
				String description;
				int choice = random.nextInt(10);
				if (choice < 4) {
					int size = random.nextInt(4) == 0 ? random.nextInt(150) : random.nextInt(6);
					String[] keys = new String[size];
					Object[] values = new Object[size];
					for (int i = 0; i < size; i++) {
						keys[i] = randomKey(random, i);
						values[i] = "v" + op + "." + i;
					}
					bulk.setList(name, keys, values);
					setListOneByOne(reference, name, keys, values);
					description = "set " + name + "::* to " + Arrays.toString(keys);
				} else if (choice < 8) {
					// single writes, often inside lists the bulk writes made, to check their tracking
					String element = random.nextBoolean() ? name : name + "::" + randomKey(random, 0);
					Object value = "s" + op;
					bulk.setVariable(element, value);
					reference.setVariable(element, value);
					description = "set " + element;
				} else if (choice < 9) {
					String element = random.nextBoolean() ? name : name + "::" + randomKey(random, 0);
					bulk.setVariable(element, null);
					reference.setVariable(element, null);
					description = "delete " + element;
				} else {
					bulk.setVariable(name + "::*", null);
					reference.setVariable(name + "::*", null);
					description = "delete " + name + "::*";
				}
				assertSameState(reference, bulk, "seed " + seed + ", op " + op + " (" + description + ")");
				operations++;
			}
		}
		assertTrue(operations > 0);
	}

	private static String randomName(Random random) {
		StringBuilder name = new StringBuilder(NAME_PARTS[random.nextInt(2)]); // a or b at the root
		int depth = random.nextInt(3);
		for (int i = 0; i < depth; i++)
			name.append("::").append(NAME_PARTS[random.nextInt(NAME_PARTS.length)]);
		return name.toString();
	}

	/**
	 * Mostly ascending indices, as copying a list gives, with some out of order or repeated.
	 */
	private static String randomKey(Random random, int position) {
		return switch (random.nextInt(6)) {
			case 0, 1 -> KEYS[random.nextInt(KEYS.length)];
			case 2 -> String.valueOf(1 + random.nextInt(200));
			default -> String.valueOf(position + 1);
		};
	}

	/**
	 * Compares the two maps through what callers can see, since the same state should mean every list reads the same.
	 */
	private static void assertSameState(VariableMap expected, VariableMap actual, String context) {
		Map<String, Object> all = expected.getAll();
		assertEquals(context + ": values differ", all, actual.getAll());
		assertEquals(context + ": branches differ", expected.branchCount(), actual.branchCount());

		// every parent of a set name has to be a list
		Set<String> lists = new TreeSet<>();
		for (String name : all.keySet()) {
			for (int separator = VariableMap.lastSeparator(name); separator != -1; separator = VariableMap.lastSeparator(name)) {
				name = name.substring(0, separator);
				lists.add(name);
			}
		}
		for (String name : lists)
			assertSameList(list(expected, name), list(actual, name), context, name);
	}

	private static void assertSameList(VariableList expected, VariableList actual, String context, String name) {
		assertEquals(context + ": keys differ at '" + name + "'", new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()));
		assertEquals(context + ": next index differs at '" + name + "'", expected.nextOpenIndex(), actual.nextOpenIndex());
		assertEquals(context + ": value count differs at '" + name + "'", expected.valueCount(), actual.valueCount());
		assertEquals(context + ": size differs at '" + name + "'", expected.size(), actual.size());
		assertEquals(context + ": own value differs at '" + name + "'", expected.ownValue(), actual.ownValue());
		for (Map.Entry<String, Object> entry : expected.entrySet()) {
			Object expectedValue = entry.getValue();
			Object actualValue = actual.get(entry.getKey());
			if (expectedValue instanceof VariableList) { // nested lists get compared on their own
				assertTrue(context + ": not a list at '" + name + "::" + entry.getKey() + "'", actualValue instanceof VariableList);
			} else {
				assertEquals(context + ": value differs at '" + name + "::" + entry.getKey() + "'", expectedValue, actualValue);
			}
		}
	}

}
