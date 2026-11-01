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

	/**
	 * Applies random changes, comparing every list against a plain sorted map of names after each one.
	 */
	@Test
	public void testAgainstModel() {
		String[] parts = {"a", "b", "1", "2", "3", "10"};
		List<String> names = new ArrayList<>();
		for (String first : parts) {
			names.add(first);
			for (String second : parts) {
				names.add(first + "::" + second);
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
				if (random.nextInt(3) == 0) {
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

	@Test
	public void testLastSeparator() {
		assertEquals(-1, VariableMap.lastSeparator("abc"));
		assertEquals(1, VariableMap.lastSeparator("a::b"));
		assertEquals(4, VariableMap.lastSeparator("a::b::c"));
		assertEquals(1, VariableMap.lastSeparator("a:::b"));
		assertEquals(3, VariableMap.lastSeparator("a::::b"));
	}

}
