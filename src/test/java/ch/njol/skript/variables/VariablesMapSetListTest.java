package ch.njol.skript.variables;

import org.junit.Test;
import org.skriptlang.skript.util.IndexTrackingTreeMap;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Checks that {@link VariablesMap#setList} leaves the map in exactly the state that deleting the list
 * and then setting each element in turn with {@link VariablesMap#setVariable} would.
 */
public class VariablesMapSetListTest {

	/**
	 * Sets a list the slow way, one element at a time, as {@link VariablesMap#setList} must match.
	 */
	private static void setListOneByOne(VariablesMap map, String listName, String[] keys, Object[] values) {
		map.setVariable(listName + "::*", null);
		for (int i = 0; i < keys.length; i++)
			map.setVariable(listName + "::" + keys[i], values[i]);
	}

	/**
	 * The value a list holds under its null key, i.e. {@code {name}} as stored in the tree,
	 * as {@link VariablesMap#getVariable} reads a plain value from the hash map instead.
	 */
	private static Object listValue(VariablesMap map, String name) {
		Map<?, ?> list = (Map<?, ?>) map.getVariable(name + "::*");
		assertNotNull(name + "::* is not a list", list);
		return list.get(null);
	}

	private static String[] keys(String... keys) {
		return keys;
	}

	private static Object[] values(Object... values) {
		return values;
	}

	@Test
	public void setsAList() {
		VariablesMap map = new VariablesMap();
		map.setList("l", keys("1", "2", "3"), values("a", "b", "c"));

		assertEquals("a", map.getVariable("l::1"));
		assertEquals("c", map.getVariable("l::3"));
		assertEquals(Map.of("l::1", "a", "l::2", "b", "l::3", "c"), map.hashMap);
		IndexTrackingTreeMap<?> list = (IndexTrackingTreeMap<?>) map.getVariable("l::*");
		assertNotNull(list);
		assertEquals(4, list.nextOpenIndex());
	}

	@Test
	public void replacesTheOldListAndItsSublists() {
		VariablesMap map = new VariablesMap();
		map.setVariable("l::1", "old");
		map.setVariable("l::x", "old");
		map.setVariable("l::x::y", "nested");
		map.setVariable("l::x::y::z", "deeper");

		map.setList("l", keys("2"), values("new"));

		assertNull(map.getVariable("l::1"));
		assertNull(map.getVariable("l::x"));
		assertNull(map.getVariable("l::x::y"));
		assertNull(map.getVariable("l::x::y::z"));
		assertEquals(Map.of("l::2", "new"), map.hashMap);
	}

	@Test
	public void keepsTheListsOwnValue() {
		VariablesMap map = new VariablesMap();
		map.setVariable("l", "self");
		map.setVariable("l::1", "old");

		map.setList("l", keys("1", "2"), values("a", "b"));

		assertEquals("self", map.getVariable("l"));
		assertEquals("self", listValue(map, "l"));
		assertEquals("a", map.getVariable("l::1"));

		map.setVariable("l::*", null);
		assertEquals("self", map.getVariable("l"));
	}

	@Test
	public void keepsAPlainValueThatWasInTheWay() {
		VariablesMap map = new VariablesMap();
		map.setVariable("l", "plain");

		map.setList("l", keys("1"), values("a"));

		assertEquals("plain", map.getVariable("l"));
		assertEquals("plain", listValue(map, "l"));
		assertEquals("a", map.getVariable("l::1"));
	}

	@Test
	public void keepsAPlainValueInTheWayOfANestedList() {
		VariablesMap map = new VariablesMap();
		map.setVariable("a::b", "plain");

		map.setList("a::b::c", keys("1"), values("x"));

		assertEquals("plain", map.getVariable("a::b"));
		assertEquals("plain", listValue(map, "a::b"));
		assertEquals("x", map.getVariable("a::b::c::1"));
	}

	@Test
	public void sortsKeysAndLastRepeatWins() {
		VariablesMap map = new VariablesMap();
		map.setList("l", keys("b", "10", "2", "b", "a", "2"), values("b1", "ten", "two1", "b2", "a", "two2"));

		Map<?, ?> list = (Map<?, ?>) map.getVariable("l::*");
		assertNotNull(list);
		assertEquals(List.of("2", "10", "a", "b"), new ArrayList<>(list.keySet()));
		assertEquals("b2", map.getVariable("l::b"));
		assertEquals("two2", map.getVariable("l::2"));
		assertEquals(4, map.hashMap.size());
	}

	@Test
	public void noKeysDeletesTheList() {
		VariablesMap map = new VariablesMap();
		map.setVariable("l", "self");
		map.setVariable("l::1", "a");

		map.setList("l", keys(), values());

		assertNull(map.getVariable("l::1"));
		assertEquals("self", map.getVariable("l"));
		assertEquals(Map.of("l", "self"), map.hashMap);
	}

	@Test
	public void addingAfterwardsUsesTheFirstGap() {
		VariablesMap map = new VariablesMap();
		map.setList("l", keys("1", "3"), values("a", "c"));

		IndexTrackingTreeMap<?> list = (IndexTrackingTreeMap<?>) map.getVariable("l::*");
		assertNotNull(list);
		assertEquals(2, list.nextOpenIndex());
	}

	@Test
	public void doesNotModifyTheArrays() {
		VariablesMap map = new VariablesMap();
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
		for (long seed = 0; seed < 300; seed++) {
			Random random = new Random(seed);
			VariablesMap bulk = new VariablesMap();
			VariablesMap reference = new VariablesMap();

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

	private static void assertSameState(VariablesMap expected, VariablesMap actual, String context) {
		assertEquals(context + ": hash maps differ", expected.hashMap, actual.hashMap);
		assertSameTree(expected.treeMap, actual.treeMap, context, "");
	}

	@SuppressWarnings("unchecked")
	private static void assertSameTree(Map<String, Object> expected, Map<String, Object> actual, String context, String path) {
		assertEquals(context + ": keys differ at '" + path + "'", new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()));
		assertEquals(context + ": map types differ at '" + path + "'", expected.getClass(), actual.getClass());
		if (expected instanceof IndexTrackingTreeMap<?> expectedTracking) {
			IndexTrackingTreeMap<?> actualTracking = (IndexTrackingTreeMap<?>) actual;
			assertEquals(context + ": next index differs at '" + path + "'",
				expectedTracking.nextOpenIndex(), actualTracking.nextOpenIndex());
			assertEquals(context + ": consecutive differs at '" + path + "'",
				expectedTracking.consecutive(), actualTracking.consecutive());
			assertEquals(context + ": map indices differ at '" + path + "'",
				new HashSet<>(expectedTracking.mapIndices()), new HashSet<>(actualTracking.mapIndices()));
		}
		for (Map.Entry<String, Object> entry : expected.entrySet()) {
			Object expectedValue = entry.getValue();
			Object actualValue = actual.get(entry.getKey());
			String childPath = path + "::" + entry.getKey();
			if (expectedValue instanceof Map && actualValue instanceof Map) {
				assertSameTree((Map<String, Object>) expectedValue, (Map<String, Object>) actualValue, context, childPath);
			} else {
				assertEquals(context + ": value differs at '" + childPath + "'", expectedValue, actualValue);
			}
		}
	}

}
