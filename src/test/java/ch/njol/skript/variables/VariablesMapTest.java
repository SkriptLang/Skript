package ch.njol.skript.variables;

import org.junit.Test;
import java.util.Map;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class VariablesMapTest {
	@Test
	public void listsAreSortedIndependentSnapshots() {
		VariablesMap variables = new VariablesMap();
		variables.setVariable("list::10", "ten");
		variables.setVariable("list::2", "two");
		variables.setVariable("list::2::child", "child");
		Map<?, ?> snapshot = (Map<?, ?>) variables.getVariable("list::*");
		assertArrayEquals(new Object[]{"2", "10"}, snapshot.keySet().toArray());
		assertEquals("two", ((Map<?, ?>) snapshot.get("2")).get(null));
		variables.setVariable("list::*", null);
		assertEquals(2, snapshot.size());
		assertNull(variables.getVariable("list::*"));
		assertEquals(0, variables.size());
		try {
			snapshot.clear();
			fail("Snapshot must be immutable");
		} catch (UnsupportedOperationException expected) {}
	}

	@Test
	public void copiesAndPruningPreserveValues() {
		VariablesMap variables = new VariablesMap(1);
		variables.setVariable("parent", "root");
		variables.setVariable("parent::child", "leaf");
		VariablesMap copy = variables.copy();
		variables.setVariable("parent::*", null);
		variables.prune();
		assertEquals("root", variables.getVariable("parent"));
		assertEquals("leaf", copy.getVariable("parent::child"));
		assertEquals(2, copy.size());
	}

	@Test(timeout = 10000)
	public void concurrentUpdatesAreRetained() throws Exception {
		VariablesMap variables = new VariablesMap();
		ExecutorService executor = Executors.newFixedThreadPool(4);
		try {
			Future<?>[] work = new Future<?>[4];
			for (int thread = 0; thread < 4; thread++) {
				int id = thread;
				work[thread] = executor.submit(() -> {
					for (int i = 0; i < 500; i++)
						variables.setVariable("data::" + id + "::" + i, i);
				});
			}
			for (Future<?> future : work)
				future.get();
			assertEquals(2000, variables.size());
		} finally {
			executor.shutdownNow();
		}
	}
}
