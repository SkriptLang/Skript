package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Tests how {@link VariableStorageManager} routes changes to the queues of the storages that save them.
 */
public class VariableStorageManagerRoutingTest {

	private static final Function<Object, SerializedVariable.Value> SERIALIZER =
		value -> new SerializedVariable.Value("string", String.valueOf(value).getBytes(StandardCharsets.UTF_8));

	private VariableMap globals;
	private VariableStorageManager manager;

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		manager = managerWith(SERIALIZER);
	}

	private VariableStorageManager managerWith(Function<Object, SerializedVariable.Value> serializer) {
		return new VariableStorageManager(globals, serializer::apply, (type, data) -> null, Runnable::run);
	}

	/**
	 * @param prefixes the prefixes the storage accepts; none to accept every name
	 */
	private static RecordingStorage storage(String... prefixes) {
		return new RecordingStorage().setPrefixesForTest(prefixes.length == 0 ? null : prefixes);
	}

	private void use(VariableStorage... storages) {
		manager.setStorages(List.of(storages));
	}

	/**
	 * What a storage's queue holds: its clears, and its changes with values read back as strings (null if deleted).
	 */
	private record Queued(List<String> clears, Map<String, String> changes) {

		static Queued of(VariableStorage storage) {
			Batch batch = storage.queue().drain();
			Map<String, String> changes = new HashMap<>();
			for (SerializedVariable change : batch.changes()) {
				assertFalse("two changes to " + change.name, changes.containsKey(change.name));
				changes.put(change.name, change.value == null ? null : new String(change.value.data, StandardCharsets.UTF_8));
			}
			return new Queued(batch.clears(), changes);
		}

	}

	private static void assertNothingQueued(VariableStorage storage) {
		assertTrue(storage.queue().drain().isEmpty());
	}

	// ---- single variables ----

	@Test
	public void withoutStoragesChangesOnlyReachTheMap() {
		manager.set("a", "1");
		manager.set("l::*", null);
		manager.setList("l", new String[] {"x"}, new Object[] {"2"});
		assertEquals("1", globals.getVariable("a"));
		assertEquals("2", globals.getVariable("l::x"));
	}

	@Test
	public void aVariableIsQueuedInTheFirstStorageThatAcceptsIt() {
		RecordingStorage kills = storage("kills::"), all = storage(), unused = storage();
		use(kills, all, unused);
		manager.set("kills::steve", 3);
		manager.set("kills", 4); // doesn't start with kills::
		manager.set("deaths::steve", 5);

		assertEquals(Map.of("kills::steve", "3"), Queued.of(kills).changes());
		assertEquals(Map.of("kills", "4", "deaths::steve", "5"), Queued.of(all).changes());
		assertNothingQueued(unused);
		assertEquals(3, globals.getVariable("kills::steve"));
		assertEquals(4, globals.getVariable("kills"));
		assertEquals(5, globals.getVariable("deaths::steve"));
	}

	@Test
	public void unsavedVariablesOnlyReachTheMap() {
		RecordingStorage kills = storage("kills::");
		use(kills);
		manager.set("deaths::steve", 1); // no storage accepts it
		manager.set("-kills::steve", 2); // ephemeral, though the storage would accept the rest
		assertNothingQueued(kills);
		assertEquals(1, globals.getVariable("deaths::steve"));
		assertEquals(2, globals.getVariable("-kills::steve"));
	}

	@Test
	public void ephemeralVariablesAreNeverSerialized() {
		manager = managerWith(value -> {
			throw new AssertionError("serialized an ephemeral variable: " + value);
		});
		RecordingStorage all = storage();
		use(all);
		manager.set("-x", 1);
		assertNothingQueued(all);
		assertEquals(1, globals.getVariable("-x"));
	}

	@Test
	public void deletingQueuesADeletion() {
		RecordingStorage all = storage();
		use(all);
		manager.set("a", 1);
		manager.set("a", null);
		Map<String, String> expected = new HashMap<>();
		expected.put("a", null);
		assertEquals(expected, Queued.of(all).changes());
		assertNull(globals.getVariable("a"));
	}

	@Test
	public void theLastChangeToANameWins() {
		RecordingStorage all = storage();
		use(all);
		manager.set("a", 1);
		manager.set("a", 2);
		assertEquals(Map.of("a", "2"), Queued.of(all).changes());
	}

	@Test
	public void aListCantBeSetToAValue() {
		RecordingStorage all = storage();
		use(all);
		manager.set("a::1", 1);
		assertThrows(IllegalArgumentException.class, () -> manager.set("a::*", 2));
		assertEquals(Map.of("a::1", "1"), Queued.of(all).changes());
		assertEquals(1, globals.getVariable("a::1"));
	}

	// ---- clearing lists ----

	@Test
	public void deletingAListClearsItButNotItsOwnValue() {
		RecordingStorage all = storage();
		use(all);
		manager.set("a::1", 1);
		manager.set("a::b::2", 2);
		manager.set("a", 3);
		manager.set("a::*", null);

		Queued queued = Queued.of(all);
		assertEquals(List.of("a::"), queued.clears());
		assertEquals(Map.of("a", "3"), queued.changes()); // the list's elements were cleared, {a} wasn't
		assertNull(globals.getVariable("a::1"));
		assertNull(globals.getVariable("a::b::2"));
		assertEquals(3, globals.getVariable("a"));
	}

	@Test
	public void aClearReachesAStorageHoldingPartOfTheList() {
		RecordingStorage partB = storage("a::b"), partC = storage("a::c::"), all = storage();
		use(partB, partC, all);
		manager.set("a::*", null);

		assertEquals(List.of("a::"), Queued.of(partB).clears());
		assertEquals(List.of("a::"), Queued.of(partC).clears());
		assertEquals(List.of("a::"), Queued.of(all).clears()); // a::d has no other owner
	}

	@Test
	public void aClearStopsAtTheFirstStorageHoldingTheWholeList() {
		RecordingStorage outside = storage("b::"), kills = storage("kills::"), all = storage(), after = storage();
		use(outside, kills, all, after);
		manager.set("kills::*", null);

		assertNothingQueued(outside); // no name in the list starts with b::
		assertEquals(List.of("kills::"), Queued.of(kills).clears());
		assertNothingQueued(all); // could hold the whole list, but kills:: gets every name in it first
		assertNothingQueued(after);

		manager.set("kills::sub::*", null); // a nested list, held by the outer list's storage
		assertEquals(List.of("kills::sub::"), Queued.of(kills).clears());
		assertNothingQueued(all);
	}

	@Test
	public void aStorageWithAPrefixInsideTheListNameIsReached() {
		RecordingStorage ab = storage("ab"), all = storage();
		use(ab, all);
		manager.set("abc::*", null); // every name in it starts with ab
		assertEquals(List.of("abc::"), Queued.of(ab).clears());
		assertNothingQueued(all);
		manager.set("a::*", null); // a::... doesn't start with ab
		assertNothingQueued(ab);
		assertEquals(List.of("a::"), Queued.of(all).clears());
	}

	@Test
	public void changesBeforeAClearAreDroppedAndChangesAfterAreKept() {
		RecordingStorage all = storage();
		use(all);
		manager.set("a::1", 1);
		manager.set("a::*", null);
		manager.set("a::2", 2);
		Queued queued = Queued.of(all);
		assertEquals(List.of("a::"), queued.clears());
		assertEquals(Map.of("a::2", "2"), queued.changes());
	}

	@Test
	public void clearingAnUnsavedListOnlyReachesTheMap() {
		RecordingStorage kills = storage("kills::"), all = storage();
		use(kills);
		manager.set("deaths::1", 1);
		manager.set("deaths::*", null);
		assertNothingQueued(kills);
		assertNull(globals.getVariable("deaths::1"));

		manager = managerWith(SERIALIZER);
		manager.setStorages(List.of(all));
		manager.set("-temp::1", 1);
		manager.set("-temp::*", null); // ephemeral, though the storage accepts every name
		assertNothingQueued(all);
		assertNull(globals.getVariable("-temp::1"));
	}

	// ---- setting lists ----

	@Test
	public void setListInOneStorageQueuesAClearAndEachElement() {
		RecordingStorage all = storage();
		use(all);
		manager.set("a::old", 0);
		manager.setList("a", new String[] {"x", "y", "x"}, new Object[] {1, 2, 3});

		Queued queued = Queued.of(all);
		assertEquals(List.of("a::"), queued.clears());
		assertEquals(Map.of("a::x", "3", "a::y", "2"), queued.changes()); // the old element was cleared
		assertNull(globals.getVariable("a::old"));
		assertEquals(3, globals.getVariable("a::x"));
		assertEquals(2, globals.getVariable("a::y"));
	}

	@Test
	public void setListSplitBetweenStoragesQueuesEachElementInItsOwner() {
		RecordingStorage xs = storage("a::x"), all = storage();
		use(xs, all);
		manager.setList("a", new String[] {"x1", "y"}, new Object[] {1, 2});

		Queued queuedX = Queued.of(xs), queuedAll = Queued.of(all);
		assertEquals(List.of("a::"), queuedX.clears());
		assertEquals(Map.of("a::x1", "1"), queuedX.changes());
		assertEquals(List.of("a::"), queuedAll.clears());
		assertEquals(Map.of("a::y", "2"), queuedAll.changes());
		assertEquals(1, globals.getVariable("a::x1"));
		assertEquals(2, globals.getVariable("a::y"));
	}

	@Test
	public void setListPartlySavedQueuesOnlyTheSavedElements() {
		RecordingStorage xs = storage("a::x");
		use(xs);
		manager.setList("a", new String[] {"x", "y"}, new Object[] {1, 2});

		Queued queued = Queued.of(xs);
		assertEquals(List.of("a::"), queued.clears());
		assertEquals(Map.of("a::x", "1"), queued.changes());
		assertEquals(2, globals.getVariable("a::y"));
	}

	@Test
	public void setListDoesntSerializeUnsavedElements() {
		manager = managerWith(value -> {
			if (!value.equals(1))
				throw new AssertionError("serialized an element that isn't saved: " + value);
			return SERIALIZER.apply(value);
		});
		RecordingStorage xs = storage("a::x");
		use(xs);
		manager.setList("a", new String[] {"x", "y"}, new Object[] {1, 2});
		assertEquals(Map.of("a::x", "1"), Queued.of(xs).changes());
	}

	@Test
	public void setListOfAnUnsavedListOnlyReachesTheMap() {
		RecordingStorage kills = storage("kills::");
		use(kills);
		manager.setList("deaths", new String[] {"x"}, new Object[] {1});
		manager.setList("-kills", new String[] {"x"}, new Object[] {2});
		assertNothingQueued(kills);
		assertEquals(1, globals.getVariable("deaths::x"));
		assertEquals(2, globals.getVariable("-kills::x"));
	}

	@Test
	public void setListStopsAtTheFirstStorageHoldingTheWholeList() {
		RecordingStorage kills = storage("kills::"), all = storage();
		use(kills, all);
		manager.setList("kills", new String[] {"steve"}, new Object[] {1});
		assertEquals(Map.of("kills::steve", "1"), Queued.of(kills).changes());
		assertNothingQueued(all);
	}

	// ---- concurrency ----

	@Test
	public void changesToListsSplitBetweenStoragesDontDeadlock() throws InterruptedException {
		RecordingStorage xs = storage("a::x"), all = storage();
		use(xs, all);
		int rounds = 20_000;
		Runnable[] tasks = {
			() -> {
				for (int i = 0; i < rounds; i++)
					manager.set("a::*", null);
			},
			() -> {
				for (int i = 0; i < rounds; i++)
					manager.setList("a", new String[] {"x" + (i % 7), "y" + (i % 5)}, new Object[] {i, i});
			},
			() -> {
				for (int i = 0; i < rounds; i++)
					manager.set("a::x" + (i % 7), i);
			},
			() -> {
				for (int i = 0; i < rounds; i++)
					manager.set("a::y" + (i % 5), i);
			},
			() -> {
				for (int i = 0; i < rounds / 100; i++) {
					xs.queue().drain();
					all.queue().drain();
				}
			}
		};

		CountDownLatch start = new CountDownLatch(1);
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread[] threads = new Thread[tasks.length];
		for (int i = 0; i < tasks.length; i++) {
			Runnable task = tasks[i];
			threads[i] = new Thread(() -> {
				try {
					start.await();
					task.run();
				} catch (Throwable t) {
					failure.compareAndSet(null, t);
				}
			}, "routing-test-" + i);
			threads[i].setDaemon(true); // so a deadlock can't keep the test JVM alive
			threads[i].start();
		}
		start.countDown();
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		for (Thread thread : threads) {
			thread.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())));
			assertFalse("deadlocked: " + thread.getName() + " is still running", thread.isAlive());
		}
		if (failure.get() != null)
			throw new AssertionError(failure.get());
	}

}
