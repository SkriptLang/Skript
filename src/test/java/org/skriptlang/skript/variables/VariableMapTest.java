package org.skriptlang.skript.variables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Checks the values, branches and list views of {@link VariableMap}, with every test running against both modes.
 * The stress tests are the exception, since local maps are confined to one thread.
 */
@RunWith(Parameterized.class)
public class VariableMapTest {

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

	@Test
	public void testEmptyMap() {
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
		assertNull(map.getVariable("a"));
		assertNull(map.getVariable("a::b"));
	}

	@Test
	public void testSetOverwriteDelete() {
		assertNull(map.setVariable("a", 1));
		assertEquals(1, map.getVariable("a"));
		assertEquals(1, map.size());
		assertFalse(map.isEmpty());

		assertEquals(1, map.setVariable("a", 2));
		assertEquals(2, map.getVariable("a"));
		assertEquals(1, map.size());

		assertEquals(2, map.setVariable("a", null));
		assertNull(map.getVariable("a"));
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	public void testDeleteMissing() {
		assertNull(map.setVariable("a", null));
		assertNull(map.setVariable("a::b", null));
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());

		// the parent exists here, but has no entry for this key
		map.setVariable("a::b", 1);
		assertNull(map.setVariable("a::c", null));
		assertEquals(1, map.getVariable("a::b"));
		assertEquals(1, map.branchCount());
	}

	@Test
	public void testListNameRejectsValue() {
		assertThrows(IllegalArgumentException.class, () -> map.setVariable("a::*", 1));
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testRootNamesStayOutOfTree() {
		map.setVariable("a", 1);
		map.setVariable("b", 2);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testNestedSetCreatesAncestors() {
		map.setVariable("a::b::c", 1);
		assertEquals(1, map.getVariable("a::b::c"));
		assertEquals(1, map.size());
		assertEquals(2, map.branchCount()); // a and a::b
		assertNull(map.getVariable("a"));
		assertNull(map.getVariable("a::b"));
	}

	@Test
	public void testOverwriteLeavesTreeAlone() {
		map.setVariable("a::b", 1);
		map.setVariable("a::b", 2);
		assertEquals(2, map.getVariable("a::b"));
		assertEquals(1, map.size());
		assertEquals(1, map.branchCount());
	}

	@Test
	public void testDeleteNestedLeavesNothing() {
		map.setVariable("a::b::c", 1);
		map.setVariable("a::b::c", null);
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testDeleteStopsAtSharedAncestor() {
		map.setVariable("a::b::c", 1);
		map.setVariable("a::d", 2);
		map.setVariable("a::b::c", null);
		assertEquals(1, map.branchCount()); // a::b is gone, but a still has d
		assertEquals(2, map.getVariable("a::d"));

		map.setVariable("a::d", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testBranchWithValueBecomesLeaf() {
		map.setVariable("x::a", 1);
		map.setVariable("x::a::b", 2);
		assertEquals(2, map.branchCount()); // x and x::a

		map.setVariable("x::a::b", null);
		assertEquals(1, map.branchCount()); // x::a is a leaf again, so only x is left
		assertEquals(1, map.getVariable("x::a"));

		map.setVariable("x::a", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testDeleteBranchValueKeepsChildren() {
		map.setVariable("x::a::b", 1);
		map.setVariable("x::a", 2);
		map.setVariable("x::a", null);
		assertNull(map.getVariable("x::a"));
		assertEquals(1, map.getVariable("x::a::b"));
		assertEquals(2, map.branchCount());

		map.setVariable("x::a::b", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testRootValueSurvivesPrune() {
		map.setVariable("x", 1);
		map.setVariable("x::a", 2);
		assertEquals(1, map.branchCount());

		map.setVariable("x::a", null);
		assertEquals(0, map.branchCount());
		assertEquals(1, map.getVariable("x"));
		assertEquals(1, map.size());
	}

	@Test
	public void testSeparatorEdgeCases() {
		map.setVariable("a:::b", 1); // parent a, key :b
		assertEquals(1, map.getVariable("a:::b"));
		assertEquals(1, map.branchCount());
		map.setVariable("a:::b", null);
		assertEquals(0, map.branchCount());

		map.setVariable("a::::b", 1); // parent a::, key b
		assertEquals(1, map.getVariable("a::::b"));
		assertEquals(2, map.branchCount());
		map.setVariable("a::::b", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testChurnDoesNotLeak() {
		for (int i = 0; i < 10_000; i++) {
			String name = "cooldown::" + i + "::end";
			map.setVariable(name, i);
			map.setVariable(name, null);
		}
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	private VariableList list(String name) {
		Object list = map.getVariable(name + "::*");
		assertNotNull(name + " should be a list", list);
		return (VariableList) list;
	}

	@Test
	public void testMissingListIsNull() {
		assertNull(map.getVariable("x::*"));
		map.setVariable("x", 1);
		assertNull(map.getVariable("x::*")); // has a value, but no children

		map.setVariable("x::a", 2);
		assertNotNull(map.getVariable("x::*"));
		map.setVariable("x::a", null);
		assertNull(map.getVariable("x::*"));
	}

	@Test
	public void testListOrder() {
		for (String key : new String[]{"b", "10", "1", "a", "2", "-1"})
			map.setVariable("x::" + key, key);
		List<String> keys = new ArrayList<>(list("x").keySet());
		assertEquals(List.of("-1", "1", "2", "10", "a", "b"), keys);
		assertEquals("a", list("x").get("a"));
		assertTrue(list("x").containsKey("10"));
		assertFalse(list("x").containsKey("c"));
		assertNull(list("x").get(1)); // not a string, so it can't be a key
	}

	@Test
	public void testOwnValueInView() {
		map.setVariable("x", "own");
		map.setVariable("x::a", 1);
		VariableList list = list("x");

		assertEquals("own", list.ownValue());
		assertEquals("own", list.get(null));
		assertTrue(list.containsKey(null));
		assertEquals(2, list.size());
		Map.Entry<String, Object> first = list.entrySet().iterator().next();
		assertNull(first.getKey());
		assertEquals("own", first.getValue());

		map.setVariable("x", null);
		assertNull(list.ownValue());
		assertFalse(list.containsKey(null));
		assertEquals(1, list.size());
	}

	@Test
	public void testChildrenLeavesOutOwnValue() {
		map.setVariable("x", "own");
		map.setVariable("x::a", 1);
		Map<String, Object> children = list("x").children();

		assertEquals(1, children.size());
		assertNull(children.get(null));
		assertFalse(children.containsKey(null));
		assertEquals(Set.of("a"), children.keySet());
		assertEquals("own", ((VariableList) children).ownValue());
	}

	@Test
	public void testNestedViews() {
		map.setVariable("x::a", "own");
		map.setVariable("x::a::b", 1);
		map.setVariable("x::c", 2);

		VariableList nested = (VariableList) list("x").get("a");
		assertEquals("own", nested.ownValue());
		assertEquals(1, nested.get("b"));
		assertEquals(2, list("x").get("c"));

		map.setVariable("x::a::b", null); // a goes back to being a leaf
		assertEquals("own", list("x").get("a"));
	}

	@Test
	public void testViewIsLive() {
		map.setVariable("x::a", 1);
		VariableList list = list("x");

		map.setVariable("x::b", 2);
		assertEquals(2, list.size());
		assertEquals(2, list.get("b"));

		map.setVariable("x::a", 3);
		assertEquals(3, list.get("a"));

		map.setVariable("x::a", null);
		map.setVariable("x::b", null); // the branch is pruned, so the old view reads as empty
		assertTrue(list.isEmpty());
		assertFalse(list.entrySet().iterator().hasNext());
	}

	@Test
	public void testViewIsUnmodifiable() {
		map.setVariable("x::a", 1);
		VariableList list = list("x");
		assertThrows(UnsupportedOperationException.class, () -> list.put("b", 2));
		assertThrows(UnsupportedOperationException.class, () -> list.remove("a"));
		assertThrows(UnsupportedOperationException.class, list::clear);
		assertThrows(UnsupportedOperationException.class, () -> list.entrySet().iterator().next().setValue(2));
		Iterator<Map.Entry<String, Object>> iterator = list.entrySet().iterator();
		iterator.next();
		assertThrows(UnsupportedOperationException.class, iterator::remove);
		assertThrows(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void testValueCount() {
		map.setVariable("x", "own"); // the list's own value is never counted
		map.setVariable("x::1", 1);
		assertEquals(1, list("x").valueCount());

		map.setVariable("x::a::b", 2); // a only has children, so it isn't counted
		assertEquals(1, list("x").valueCount());
		assertEquals(2, list("x").children().size());

		map.setVariable("x::a", 3);
		assertEquals(2, list("x").valueCount());

		map.setVariable("x::a", null);
		assertEquals(1, list("x").valueCount());

		map.setVariable("x::a", 3);
		map.setVariable("x::a::b", null); // a goes back to being a leaf, which stays counted
		assertEquals(2, list("x").valueCount());

		map.setVariable("x::a", null);
		assertEquals(1, list("x").valueCount());
	}

	@Test
	public void testValueCountWithLeafPromotedToBranch() {
		map.setVariable("x::a", 1);
		map.setVariable("x::a::b", 2); // a was already counted as a leaf
		assertEquals(1, list("x").valueCount());
		assertEquals(1, list("x").children().size());
	}

	@Test
	public void testNextOpenIndexFillsGaps() {
		for (int i = 1; i <= 3; i++)
			map.setVariable("x::" + i, i);
		assertEquals(4, list("x").nextOpenIndex());

		map.setVariable("x::2", null);
		assertEquals(2, list("x").nextOpenIndex());
		map.setVariable("x::3", null);
		assertEquals(2, list("x").nextOpenIndex());
		map.setVariable("x::2", 2);
		assertEquals(3, list("x").nextOpenIndex());
	}

	@Test
	public void testNextOpenIndexIgnoresOtherKeys() {
		for (String key : new String[]{"a", "0", "01", "-1", "1.5", "2147483648"})
			map.setVariable("x::" + key, 1);
		assertEquals(1, list("x").nextOpenIndex());
	}

	@Test
	public void testSublistTakesUpItsIndex() {
		map.setVariable("x::1::a", 1);
		assertEquals(2, list("x").nextOpenIndex());
		map.setVariable("x::1::a", 2);
		map.setVariable("x::1", 3);
		map.setVariable("x::1::a", null); // leaf again, still using the index
		assertEquals(2, list("x").nextOpenIndex());
	}

	@Test
	public void testNextOpenIndexPastTrackedLimit() {
		for (int i = 1; i <= 200; i++)
			map.setVariable("x::" + i, i);
		assertEquals(201, list("x").nextOpenIndex());

		map.setVariable("x::100", null);
		assertEquals(100, list("x").nextOpenIndex());
		map.setVariable("x::100", 100);
		assertEquals(201, list("x").nextOpenIndex());
	}

	@Test
	public void testNextOpenIndexWithUntrackedKeys() {
		for (int i = 200; i >= 1; i--) // starts above the tracked range, so these have to be picked up later
			map.setVariable("x::" + i, i);
		assertEquals(201, list("x").nextOpenIndex());
	}

	@Test
	public void testNextOpenIndexWithLargeKeys() {
		map.setVariable("x::999999999", 1);
		assertEquals(1, list("x").nextOpenIndex());
		map.setVariable("x::1", 1);
		assertEquals(2, list("x").nextOpenIndex());
	}

	@Test
	public void testNextOpenIndexAfterShrinking() {
		for (int i = 1; i <= 1000; i++)
			map.setVariable("x::" + i, i);
		for (int i = 1; i <= 995; i++) // leaves 996 to 1000, which get dropped from the tracked range
			map.setVariable("x::" + i, null);
		assertEquals(1, list("x").nextOpenIndex());

		for (int i = 1; i <= 995; i++)
			map.setVariable("x::" + i, i);
		assertEquals(1001, list("x").nextOpenIndex());
	}

	@Test
	public void testSetListCreatesList() {
		map.setList("x", new String[]{"a", "b"}, new Object[]{1, 2});
		assertEquals(1, map.getVariable("x::a"));
		assertEquals(2, map.getVariable("x::b"));
		assertEquals(2, map.size());
		assertEquals(1, map.branchCount());
		assertEquals(List.of("a", "b"), new ArrayList<>(list("x").keySet()));
		assertEquals(1, list("x").get("a"));
	}

	@Test
	public void testSetListReplacesChildren() {
		map.setVariable("x::a", 1);
		map.setVariable("x::b", 2);
		map.setVariable("x::c", 3);

		map.setList("x", new String[]{"b", "d"}, new Object[]{20, 40});
		assertNull(map.getVariable("x::a"));
		assertEquals(20, map.getVariable("x::b"));
		assertNull(map.getVariable("x::c"));
		assertEquals(40, map.getVariable("x::d"));
		assertEquals(2, map.size());
		assertEquals(List.of("b", "d"), new ArrayList<>(list("x").keySet()));
	}

	@Test
	public void testSetListLeavesOtherListsAlone() {
		map.setVariable("x::a", 1);
		map.setVariable("xy::a", 2);
		map.setVariable("y", 3);
		map.setList("x", new String[]{"b"}, new Object[]{4});
		assertEquals(2, map.getVariable("xy::a"));
		assertEquals(3, map.getVariable("y"));
	}

	@Test
	public void testSetListViewIsLive() {
		map.setVariable("x::a", 1);
		VariableList list = list("x");
		map.setList("x", new String[]{"b", "c"}, new Object[]{2, 3});
		assertEquals(2, list.size());
		assertEquals(2, list.valueCount());
		assertNull(list.get("a"));
		assertEquals(2, list.get("b"));
	}

	@Test
	public void testSetListKeepsOwnValue() {
		map.setVariable("x", "own");
		map.setVariable("x::a", 1);
		map.setList("x", new String[]{"b"}, new Object[]{2});
		assertEquals("own", map.getVariable("x"));
		assertEquals("own", list("x").ownValue());
		assertEquals(1, list("x").valueCount()); // the own value is still not counted
		assertEquals(2, list("x").size());
	}

	@Test
	public void testSetListOnLeafKeepsItsValue() {
		map.setVariable("x::a", "own");
		map.setList("x::a", new String[]{"b"}, new Object[]{1});
		assertEquals("own", map.getVariable("x::a"));
		assertEquals(1, map.getVariable("x::a::b"));
		assertEquals(1, list("x").valueCount()); // a was already counted as a leaf
		assertEquals(1, list("x").children().size());
		assertEquals("own", ((VariableList) list("x").get("a")).ownValue());
	}

	@Test
	public void testSetListSortsKeys() {
		map.setList("x", new String[]{"10", "b", "2", "1", "a"}, new Object[]{10, "b", 2, 1, "a"});
		assertEquals(List.of("1", "2", "10", "a", "b"), new ArrayList<>(list("x").keySet()));
		assertEquals(10, map.getVariable("x::10"));
		assertEquals(5, list("x").valueCount());
		assertEquals(3, list("x").nextOpenIndex());
	}

	@Test
	public void testSetListLastRepeatWins() {
		map.setList("x", new String[]{"b", "a", "b", "b"}, new Object[]{1, 2, 3, 4});
		assertEquals(4, map.getVariable("x::b"));
		assertEquals(2, map.getVariable("x::a"));
		assertEquals(2, map.size());
		assertEquals(2, list("x").valueCount());
		assertEquals(2, list("x").size());
	}

	@Test
	public void testSetListLeavesCallerArraysAlone() {
		String[] keys = {"b", "a", "b"};
		Object[] values = {1, 2, 3};
		map.setList("x", keys, values);
		assertArrayEquals(new String[]{"b", "a", "b"}, keys);
		assertArrayEquals(new Object[]{1, 2, 3}, values);
	}

	@Test
	public void testSetListRejectsBadInput() {
		map.setVariable("x::a", 1);
		assertThrows(IllegalArgumentException.class, () -> map.setList("x", new String[]{"a", "b"}, new Object[]{1}));
		assertThrows(NullPointerException.class, () -> map.setList("x", new String[]{"a", "b"}, new Object[]{2, null}));

		// nothing should have been written
		assertEquals(1, map.getVariable("x::a"));
		assertNull(map.getVariable("x::b"));
		assertEquals(1, map.size());
		assertEquals(1, list("x").valueCount());
	}

	@Test
	public void testSetListKeptKeyThatWasList() {
		map.setVariable("x::a", 1);
		map.setVariable("x::a::b", 2);
		map.setVariable("x::a::b::c", 3);
		assertEquals(3, map.branchCount());

		map.setList("x", new String[]{"a"}, new Object[]{10});
		assertEquals(10, map.getVariable("x::a"));
		assertNull(map.getVariable("x::a::b"));
		assertNull(map.getVariable("x::a::b::c"));
		assertNull(map.getVariable("x::a::*"));
		assertEquals(1, map.size());
		assertEquals(1, map.branchCount());
		assertEquals(10, list("x").get("a")); // a leaf now, not a nested view
		assertEquals(1, list("x").valueCount());

		// the old branches are gone, so the name has to work like any other again
		map.setVariable("x::a::b", 4);
		assertEquals(4, ((VariableList) list("x").get("a")).get("b"));
		assertEquals(10, ((VariableList) list("x").get("a")).ownValue());
		assertEquals(1, list("x").valueCount());
	}

	@Test
	public void testSetListRemovesNestedLists() {
		map.setVariable("x::a", 1);
		map.setVariable("x::a::b", 2);
		map.setVariable("x::c::d::e", 3);
		map.setList("x", new String[]{"z"}, new Object[]{4});
		assertNull(map.getVariable("x::a"));
		assertNull(map.getVariable("x::a::b"));
		assertNull(map.getVariable("x::c::d::e"));
		assertEquals(1, map.size());
		assertEquals(1, map.branchCount());
	}

	@Test
	public void testSetListCreatesAndPrunesParents() {
		map.setList("a::b::c", new String[]{"d"}, new Object[]{1});
		assertEquals(3, map.branchCount());
		assertEquals(1, map.getVariable("a::b::c::d"));
		assertEquals(0, list("a").valueCount()); // b only has children
		assertEquals(1, list("a").size());

		map.setVariable("a::b::c::*", null);
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testClearList() {
		map.setVariable("x::a", 1);
		map.setVariable("x::b::c", 2);
		assertNull(map.setVariable("x::*", null));
		assertNull(map.getVariable("x::*"));
		assertNull(map.getVariable("x::a"));
		assertNull(map.getVariable("x::b::c"));
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testClearMissingList() {
		map.setVariable("x", 1);
		assertNull(map.setVariable("x::*", null));
		assertNull(map.setVariable("y::*", null));
		assertEquals(1, map.getVariable("x"));
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testSetListWithNoKeysClears() {
		map.setVariable("x::a", 1);
		map.setList("x", new String[0], new Object[0]);
		assertNull(map.getVariable("x::*"));
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testClearKeepsOwnValue() {
		map.setVariable("x", "own");
		map.setVariable("x::a", 1);
		map.setVariable("x::*", null);
		assertEquals("own", map.getVariable("x"));
		assertNull(map.getVariable("x::*"));
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testClearTurnsBranchBackIntoLeaf() {
		map.setVariable("x::a", "own");
		map.setVariable("x::a::b", 1);
		map.setVariable("x::a::*", null);
		assertEquals("own", map.getVariable("x::a"));
		assertEquals("own", list("x").get("a"));
		assertEquals(1, list("x").valueCount());
		assertEquals(1, list("x").size());
		assertEquals(1, map.branchCount());
	}

	@Test
	public void testClearPrunesOnlyUpToSharedAncestor() {
		map.setVariable("x::a::b", 1);
		map.setVariable("x::c", 2);
		map.setVariable("x::a::*", null);
		assertEquals(1, map.branchCount());
		assertEquals(1, list("x").size());
		assertEquals(1, list("x").valueCount());
		assertEquals(2, map.getVariable("x::c"));
	}

	@Test
	public void testRepeatedSetListDoesNotLeak() {
		for (int i = 0; i < 1_000; i++) {
			map.setVariable("x::" + i + "::nested::value", i);
			map.setList("x", new String[]{String.valueOf(i)}, new Object[]{i});
			assertEquals(1, map.size());
			assertEquals(1, map.branchCount());
		}
		map.setVariable("x::*", null);
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testSetListCounters() {
		map.setList("x", new String[]{"1", "2", "4"}, new Object[]{1, 2, 4});
		assertEquals(3, list("x").valueCount());
		assertEquals(3, list("x").nextOpenIndex());

		map.setList("x", new String[]{"2", "3"}, new Object[]{2, 3});
		assertEquals(2, list("x").valueCount());
		assertEquals(1, list("x").nextOpenIndex());

		map.setList("x", new String[]{"a", "0", "01"}, new Object[]{1, 2, 3});
		assertEquals(1, list("x").nextOpenIndex());
	}

	@Test
	public void testSetListKeepsTrackingUsable() {
		map.setList("x", new String[]{"1", "2", "4"}, new Object[]{1, 2, 4});
		map.setVariable("x::3", 3);
		assertEquals(5, list("x").nextOpenIndex());
		map.setVariable("x::2", null);
		assertEquals(2, list("x").nextOpenIndex());
		assertEquals(3, list("x").valueCount());
	}

	@Test
	public void testSetListPastTrackedLimit() {
		String[] keys = new String[200];
		Object[] values = new Object[200];
		for (int i = 0; i < 200; i++) {
			keys[i] = String.valueOf(i + 1);
			values[i] = i;
		}
		map.setList("x", keys, values);
		assertEquals(201, list("x").nextOpenIndex());
		assertEquals(200, list("x").valueCount());

		map.setVariable("x::150", null);
		assertEquals(150, list("x").nextOpenIndex());
		map.setVariable("x::150", 1);
		assertEquals(201, list("x").nextOpenIndex());

		// a small replacement has to bring the tracking back down with it
		map.setList("x", new String[]{"1"}, new Object[]{1});
		assertEquals(2, list("x").nextOpenIndex());
		map.setVariable("x::2", 2);
		assertEquals(3, list("x").nextOpenIndex());
	}

	@Test
	public void testSetListWithKeysAboveTrackedRange() {
		map.setList("x", new String[]{"1", "100", "999999999"}, new Object[]{1, 2, 3});
		assertEquals(2, list("x").nextOpenIndex());

		// 100 wasn't tracked, so it has to be picked up once the list grows past it
		for (int i = 2; i < 100; i++)
			map.setVariable("x::" + i, i);
		assertEquals(101, list("x").nextOpenIndex());
	}

	@Test
	public void testNestedViewAfterSetList() {
		map.setVariable("x::a::b", 1);
		VariableList nested = (VariableList) list("x").get("a");
		map.setList("x", new String[]{"c"}, new Object[]{2});

		assertTrue(nested.isEmpty());
		assertEquals(0, nested.size());
		assertEquals(0, nested.valueCount());
		assertNull(nested.get("b"));
		assertFalse(nested.entrySet().iterator().hasNext());
	}

	@Test
	public void testNestedViewWithOwnValueAfterSetList() {
		map.setVariable("x::a", "own");
		map.setVariable("x::a::b", 1);
		VariableList nested = (VariableList) list("x").get("a");
		map.setList("x", new String[]{"c"}, new Object[]{2});

		assertNull(nested.ownValue());
		assertTrue(nested.isEmpty());
	}

	/**
	 * The old children aren't changed by a replace, so anything already iterating them just carries on.
	 */
	@Test
	public void testIterationDuringSetList() {
		map.setVariable("x::1", 1);
		map.setVariable("x::2", 2);
		map.setVariable("x::3", 3);
		Iterator<Map.Entry<String, Object>> iterator = list("x").entrySet().iterator();
		assertEquals(1, iterator.next().getValue());

		map.setList("x", new String[]{"2"}, new Object[]{20});
		assertEquals(20, iterator.next().getValue());
		assertFalse(iterator.hasNext()); // 3 is gone
	}

	@Test
	public void testIterationReachesReplacedSublist() {
		map.setVariable("x::a", 1);
		map.setVariable("x::a::b", 2);
		Iterator<Map.Entry<String, Object>> iterator = list("x").entrySet().iterator();

		map.setList("x", new String[]{"a"}, new Object[]{10});
		assertEquals(10, iterator.next().getValue()); // the value, not an empty view of the old sublist
		assertFalse(iterator.hasNext());
	}

	/**
	 * Applies random changes, comparing every list against a plain sorted map of names after each one.
	 */
	@Test
	public void testAgainstModel() {
		String[] parts = {"a", "b", "1", "2", "3", "10"};
		List<String> names = new ArrayList<>();
		List<String> listNames = new ArrayList<>(); // only these, so the children are all in names
		for (String first : parts) {
			names.add(first);
			listNames.add(first);
			for (String second : parts) {
				names.add(first + "::" + second);
				listNames.add(first + "::" + second);
				for (String third : parts)
					names.add(first + "::" + second + "::" + third);
			}
		}

		for (int seed = 0; seed < 50; seed++) {
			Random random = new Random(seed);
			VariableMap map = factory.get();
			TreeMap<String, Object> model = new TreeMap<>();
			for (int step = 0; step < 300; step++) {
				String name = names.get(random.nextInt(names.size()));
				int action = random.nextInt(12);
				if (action == 0) {
					String listName = listNames.get(random.nextInt(listNames.size()));
					model.keySet().removeIf(full -> full.startsWith(listName + "::"));
					map.setVariable(listName + "::*", null);
				} else if (action <= 2) {
					String listName = listNames.get(random.nextInt(listNames.size()));
					int length = random.nextInt(5);
					String[] keys = new String[length];
					Object[] values = new Object[length];
					model.keySet().removeIf(full -> full.startsWith(listName + "::"));
					for (int i = 0; i < length; i++) {
						keys[i] = parts[random.nextInt(parts.length)];
						values[i] = random.nextInt(100);
						model.put(listName + "::" + keys[i], values[i]);
					}
					map.setList(listName, keys, values);
				} else if (action <= 5) {
					assertEquals(model.remove(name), map.setVariable(name, null));
				} else {
					Object value = random.nextInt(100);
					assertEquals(model.put(name, value), map.setVariable(name, value));
				}
				String context = "seed " + seed + ", step " + step + ", ";
				assertEquals(context + "size", model.size(), map.size());
				for (String checked : names)
					assertEquals(context + checked, model.get(checked), map.getVariable(checked));
				assertListsMatch(map, model, names, context);
			}
		}
	}

	private static void assertListsMatch(VariableMap map, TreeMap<String, Object> model, List<String> names, String context) {
		int branches = 0;
		for (String name : names) {
			// the direct children of this name, as the model sees them
			String prefix = name + "::";
			TreeSet<String> children = new TreeSet<>(VariableNameComparator.INSTANCE);
			for (String full : model.keySet()) {
				if (!full.startsWith(prefix))
					continue;
				String rest = full.substring(prefix.length());
				int separator = rest.indexOf("::");
				children.add(separator == -1 ? rest : rest.substring(0, separator));
			}

			Object raw = map.getVariable(name + "::*");
			if (children.isEmpty()) {
				assertNull(context + name, raw);
				continue;
			}
			branches++;

			VariableList list = (VariableList) raw;
			assertNotNull(context + name, list);
			assertEquals(context + name, model.get(name), list.ownValue());
			assertEquals(context + name, new ArrayList<>(children), new ArrayList<>(list.children().keySet()));
			assertEquals(context + name, children.size() + (model.containsKey(name) ? 1 : 0), list.size());

			int values = 0;
			for (String child : children) {
				if (model.containsKey(prefix + child))
					values++;
			}
			assertEquals(context + "valueCount " + name, values, list.valueCount());

			int free = 1;
			while (children.contains(String.valueOf(free)))
				free++;
			assertEquals(context + "nextOpenIndex " + name, free, list.nextOpenIndex());
		}
		assertEquals(context + "branches", branches, map.branchCount());
	}

	/**
	 * Runs the same work on several threads, starting them all together and rethrowing anything they throw.
	 */
	private static void runThreads(int threads, IntConsumer work) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		try {
			CyclicBarrier start = new CyclicBarrier(threads);
			List<Future<?>> futures = new ArrayList<>();
			for (int thread = 0; thread < threads; thread++) {
				int id = thread;
				futures.add(executor.submit(() -> {
					start.await();
					work.accept(id);
					return null;
				}));
			}
			for (Future<?> future : futures)
				future.get();
		} finally {
			executor.shutdown();
		}
	}

	@Test
	public void testStressDisjointWriters() throws Exception {
		assumeTrue(mode.equals("concurrent"));
		int threads = 4;
		int count = 5_000;
		AtomicBoolean done = new AtomicBoolean();
		Thread reader = new Thread(() -> {
			while (!done.get()) {
				for (int thread = 0; thread < threads; thread++) {
					if (map.getVariable("t" + thread + "::*") instanceof VariableList list) {
						for (Map.Entry<String, Object> entry : list.entrySet())
							assertNotNull(entry.getValue());
					}
				}
			}
		});
		reader.start();

		runThreads(threads, id -> {
			for (int i = 0; i < count; i++) {
				map.setVariable("t" + id + "::" + i, i);
				if (i % 3 == 0)
					map.setVariable("t" + id + "::" + i, null);
			}
		});
		done.set(true);
		reader.join();

		int kept = count - (count + 2) / 3;
		assertEquals(threads * (long) kept, map.size());
		for (int thread = 0; thread < threads; thread++) {
			VariableList list = list("t" + thread);
			assertEquals(kept, list.valueCount());
			assertEquals(kept, list.size());
			assertEquals(3, list.nextOpenIndex());
		}
	}

	/**
	 * One thread sets a leaf while another deletes it, so the deletes keep pruning what the sets just created.
	 */
	@Test
	public void testStressSetAgainstDelete() throws Exception {
		assumeTrue(mode.equals("concurrent"));
		for (int round = 0; round < 1_000; round++) {
			VariableMap map = factory.get();
			runThreads(2, id -> {
				for (int i = 0; i < 100; i++)
					map.setVariable("a::b::c", id == 0 ? i : null);
			});

			map.setVariable("a::b::c", 42);
			assertEquals(1, map.size());
			assertEquals(2, map.branchCount());
			VariableList nested = (VariableList) ((VariableList) map.getVariable("a::*")).get("b");
			assertEquals(42, nested.get("c"));
			assertEquals(1, nested.valueCount());

			map.setVariable("a::b::c", null);
			assertEquals(0, map.branchCount());
		}
	}

	/**
	 * One thread changes {x::a} while the other creates and prunes its child,
	 * which is the race that {@code pruneIfEmpty} has to count a value for.
	 */
	@Test
	public void testStressValueAgainstPrune() throws Exception {
		assumeTrue(mode.equals("concurrent"));
		for (int round = 0; round < 1_000; round++) {
			VariableMap map = factory.get();
			map.setVariable("x::keep", 1);
			runThreads(2, id -> {
				for (int i = 0; i < 100; i++) {
					String name = id == 0 ? "x::a" : "x::a::b";
					map.setVariable(name, i);
					map.setVariable(name, null);
				}
			});
			assertEquals(1, list("x", map).valueCount());
			assertEquals(1, list("x", map).size());
			assertEquals(1, map.branchCount());

			map.setVariable("x::a", 1);
			assertEquals(2, list("x", map).valueCount());
			map.setVariable("x::a::b", 1);
			map.setVariable("x::a::b", null);
			assertEquals(2, list("x", map).valueCount());
			assertEquals(1, map.branchCount());
		}
	}

	private static VariableList list(String name, VariableMap map) {
		return (VariableList) map.getVariable(name + "::*");
	}

	/**
	 * One thread replaces and clears a list while others set and delete names in it, including a nested list
	 * under a key the replacement keeps. Whoever wins, the tree has to match {@code values} once they're done.
	 */
	@Test
	public void testStressSetListAgainstWriters() throws Exception {
		assumeTrue(mode.equals("concurrent"));
		List<String> names = List.of("x", "x::1", "x::2", "x::3", "x::a", "x::a::b", "x::a::c");
		String[] keys = {"1", "3", "a"};
		Object[] values = {1, 3, "a"};
		for (int round = 0; round < 1_000; round++) {
			VariableMap map = factory.get();
			runThreads(3, id -> {
				for (int i = 0; i < 50; i++) {
					switch (id) {
						case 0 -> {
							map.setList("x", keys, values);
							if (i % 3 == 0)
								map.setVariable("x::*", null);
						}
						case 1 -> {
							map.setVariable("x::a::b", i);
							map.setVariable("x::a", i);
							map.setVariable("x::a::b", null);
							map.setVariable("x::a", null);
						}
						default -> {
							map.setVariable("x::1", i);
							map.setVariable("x::2", i);
							map.setVariable("x::1", null);
							map.setVariable("x::a::c", i);
							map.setVariable("x::2", null);
							map.setVariable("x::a::c", null);
						}
					}
				}
			});

			TreeMap<String, Object> model = new TreeMap<>();
			for (String name : names) {
				Object value = map.getVariable(name);
				if (value != null)
					model.put(name, value);
			}
			assertEquals("round " + round, model.size(), map.size());
			assertListsMatch(map, model, names, "round " + round + ", ");

			// nothing should be left behind once everything goes
			map.setVariable("x::*", null);
			assertEquals("round " + round, 0, map.size());
			assertEquals("round " + round, 0, map.branchCount());
		}
	}

	/**
	 * Readers should only ever see real values, even while a list is being replaced underneath them.
	 */
	@Test
	public void testStressSetListAgainstReaders() throws Exception {
		assumeTrue(mode.equals("concurrent"));
		AtomicBoolean done = new AtomicBoolean();
		Thread[] reader = new Thread[1];
		List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
		reader[0] = new Thread(() -> {
			try {
				while (!done.get()) {
					if (map.getVariable("x::*") instanceof VariableList list) {
						for (Map.Entry<String, Object> entry : list.entrySet())
							assertNotNull(entry.getValue());
					}
				}
			} catch (Throwable failure) {
				failures.add(failure);
			}
		});
		reader[0].start();

		String[] keys = new String[200];
		Object[] values = new Object[200];
		for (int i = 0; i < 200; i++) {
			keys[i] = String.valueOf(i + 1);
			values[i] = i;
		}
		for (int i = 0; i < 2_000; i++) {
			map.setList("x", keys, values);
			if (i % 2 == 0)
				map.setVariable("x::*", null);
		}
		done.set(true);
		reader[0].join();
		assertEquals(List.of(), failures);
	}

	@Test
	public void testLastSeparator() {
		assertEquals(-1, VariableMap.lastSeparator("abc"));
		assertEquals(1, VariableMap.lastSeparator("a::b"));
		assertEquals(4, VariableMap.lastSeparator("a::b::c"));
		assertEquals(1, VariableMap.lastSeparator("a:::b"));
		assertEquals(3, VariableMap.lastSeparator("a::::b"));
	}

}
