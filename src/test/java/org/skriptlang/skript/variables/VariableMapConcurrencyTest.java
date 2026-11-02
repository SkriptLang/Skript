package org.skriptlang.skript.variables;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Hammers a small set of names from several threads, without any outside locking, then checks that the tree
 * still matches the values once everything has settled. Races can't be forced from here, so a pass doesn't prove
 * much on its own, but a small set of names means threads keep landing on the same keys.
 */
public class VariableMapConcurrencyTest {

	private static final int WRITERS = 4;
	// many short rounds, since later writes to the same names repair any damage, so a long round hides it
	private static final int ROUNDS = 10_000;
	private static final int WRITES_PER_ROUND = 100;

	private static final String[] NAMES = {
		"a", "b", "a::1", "a::2", "a::3", "a::x", "a::b", "a::b::1", "a::b::2", "a::b::c", "a::b::c::1", "b::1", "b::2"
	};
	private static final String[] LISTS = {"a", "a::b", "a::b::c", "b"};
	private static final String[] KEYS = {"1", "2", "3", "b", "x"};

	@Test(timeout = 120_000)
	public void writesLeaveTheTreeMatchingTheValues() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(WRITERS + 1);
		try {
			for (int round = 0; round < ROUNDS; round++) {
				VariableMap map = VariableMap.concurrent();
				AtomicBoolean writing = new AtomicBoolean(true);
				CyclicBarrier start = new CyclicBarrier(WRITERS);
				Future<?> reader = executor.submit(() -> read(map, writing));

				List<Future<?>> writers = new ArrayList<>();
				for (int i = 0; i < WRITERS; i++) {
					long seed = round * 31L + i;
					writers.add(executor.submit(() -> {
						start.await(); // line the writers up, or short rounds would barely overlap
						write(map, new Random(seed));
						return null;
					}));
				}
				for (Future<?> writer : writers)
					writer.get();
				writing.set(false);
				reader.get();

				assertTreeMatchesValues(map);
			}
		} finally {
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
		}
	}

	private static void write(VariableMap map, Random random) {
		for (int i = 0; i < WRITES_PER_ROUND; i++) {
			int action = random.nextInt(100);
			if (action < 50) {
				map.setVariable(NAMES[random.nextInt(NAMES.length)], i);
			} else if (action < 85) {
				map.setVariable(NAMES[random.nextInt(NAMES.length)], null);
			} else if (action < 97) {
				List<String> keys = new ArrayList<>();
				for (String key : KEYS) {
					if (random.nextBoolean())
						keys.add(key);
				}
				map.setList(LISTS[random.nextInt(LISTS.length)], keys.toArray(new String[0]), filled(keys.size(), i));
			} else {
				map.setVariable(LISTS[random.nextInt(LISTS.length)] + "::*", null);
			}
		}
	}

	private static Object[] filled(int size, Object value) {
		Object[] values = new Object[size];
		Arrays.fill(values, value);
		return values;
	}

	/**
	 * Views read without the lock, so this is here to catch anything they can't cope with mid-write.
	 */
	private static void read(VariableMap map, AtomicBoolean writing) {
		while (writing.get()) {
			for (String list : LISTS) {
				if (map.getVariable(list + "::*") instanceof Map<?, ?> view) {
					for (Map.Entry<?, ?> entry : view.entrySet())
						assertNotNull(entry.getValue());
				}
			}
		}
	}

	private static void assertTreeMatchesValues(VariableMap map) {
		for (VariableBranch branch : map.allBranches()) {
			String name = branch.fullName;
			assertEquals(name + " is registered", branch, map.branchGet(name));
			assertEquals(name + " childCount", branch.children.size(), branch.childCount);
			assertTrue(name + " is empty", branch.childCount > 0);
			assertEquals(name + " hasValue", map.valueGet(name) != null, branch.hasValue);

			int valueChildren = 0;
			for (Map.Entry<String, Object> child : branch.children.entrySet()) {
				String childName = name + "::" + child.getKey();
				if (child.getValue() instanceof VariableBranch childBranch) {
					assertEquals(childName + " branch", childBranch, map.branchGet(childName));
					assertEquals(childName + " key", child.getKey(), childBranch.key);
					assertEquals(childName + " parent", branch, childBranch.parent);
					if (childBranch.hasValue)
						valueChildren++;
				} else {
					assertEquals(childName + " leaf name", childName, child.getValue());
					assertNotNull(childName + " leaf value", map.valueGet(childName));
					valueChildren++;
				}
			}
			assertEquals(name + " valueChildren", valueChildren, branch.valueChildren);

			int nextIndex = 1;
			while (branch.children.containsKey(String.valueOf(nextIndex)))
				nextIndex++;
			assertEquals(name + " nextIndex", nextIndex, branch.nextIndex);
		}

		for (String name : map.allValues().keySet()) {
			int separator = VariableMap.lastSeparator(name);
			if (separator == -1)
				continue;
			VariableBranch parent = map.branchGet(name.substring(0, separator));
			assertNotNull(name + " has no parent", parent);
			assertNotNull(name + " isn't in its parent", parent.children.get(name.substring(separator + 2)));
		}
	}

}
