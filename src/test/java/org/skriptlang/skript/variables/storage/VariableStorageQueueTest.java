package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests the change queue of {@link VariableStorage} on one thread.
 * A plain map stands in for Skript's variables, and the test plays the part of the router for clears.
 */
public class VariableStorageQueueTest {

	private RecordingStorage storage;
	private Map<String, Value> variables;

	@Before
	public void setUp() {
		storage = new RecordingStorage();
		variables = new HashMap<>();
	}

	private static Value value(int id) {
		return new Value("test", new byte[] {(byte) id});
	}

	private void set(String name, Value value) {
		storage.write(name, value, () -> {
			if (value == null) {
				variables.remove(name);
			} else {
				variables.put(name, value);
			}
		});
	}

	/**
	 * Clears a list the way the router does: under the exclusive lock, Skript's variables first.
	 */
	private void clear(String prefix) {
		long stamp = storage.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith(prefix));
			storage.recordClearLocked(prefix, true);
		} finally {
			storage.unlockExclusive(stamp);
		}
	}

	@Test
	public void writesToOneNameKeepTheLast() {
		Value last = value(3);
		set("x", value(1));
		set("x", value(2));
		set("x", last);
		Batch batch = storage.drain();
		assertEquals(Map.of("x", last), RecordingStorage.changes(batch));
		assertTrue(batch.clears().isEmpty());
	}

	@Test
	public void deleteDrainsAsNullValue() {
		set("x", value(1));
		set("x", null);
		Map<String, Value> changes = RecordingStorage.changes(storage.drain());
		assertTrue(changes.containsKey("x"));
		assertNull(changes.get("x"));
	}

	@Test
	public void mapWriteRunsOncePerWrite() {
		AtomicInteger runs = new AtomicInteger();
		for (int i = 0; i < 100; i++)
			storage.write("x", value(i), runs::incrementAndGet);
		assertEquals(100, runs.get());
		assertEquals(100, storage.pendingCalls.get());
	}

	@Test
	public void clearsAndListSetsWakeTheStorageOnceExternalClearsDoNot() {
		clear("a::");
		assertEquals(1, storage.pendingCalls.get());

		long stamp = storage.lockExclusive();
		try {
			storage.recordClearLocked("a::", true);
			storage.putPendingLocked("a::1", value(1));
			storage.putPendingLocked("a::2", value(2));
		} finally {
			storage.unlockExclusive(stamp);
		}
		assertEquals(2, storage.pendingCalls.get());

		stamp = storage.lockExclusive();
		try {
			storage.recordClearLocked("b::", false);
		} finally {
			storage.unlockExclusive(stamp);
		}
		storage.drain();
		assertEquals(2, storage.pendingCalls.get());
	}

	@Test
	public void clearDropsEarlierChangesUnderItsPrefixOnly() {
		Value own = value(1), similar = value(2), other = value(3);
		set("a", own);
		set("ab::x", similar);
		set("b::1", other);
		set("a::1", value(4));
		set("a::b::c", value(5));
		clear("a::");

		Batch batch = storage.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("a", own, "ab::x", similar, "b::1", other), RecordingStorage.changes(batch));
	}

	@Test
	public void writeAfterClearInTheSameBatchSurvives() {
		set("a::1", value(1));
		clear("a::");
		Value after = value(2);
		set("a::1", after);

		Batch batch = storage.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("a::1", after), RecordingStorage.changes(batch));
	}

	@Test
	public void writeBetweenTwoClearsIsDropped() {
		clear("a::");
		set("a::1", value(1));
		clear("a::");

		Batch batch = storage.drain();
		assertEquals(List.of("a::", "a::"), batch.clears());
		assertTrue(batch.changes().isEmpty());
	}

	@Test
	public void clearOfNestedListOnlyDropsThatList() {
		Value kept = value(1);
		set("a::1", kept);
		set("a::b::1", value(2));
		clear("a::b::");
		assertEquals(Map.of("a::1", kept), RecordingStorage.changes(storage.drain()));
	}

	@Test
	public void setListKeepsItsNewEntries() {
		set("a::old", value(1));
		Value one = value(2), two = value(3);
		long stamp = storage.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith("a::"));
			storage.recordClearLocked("a::", true);
			variables.put("a::1", one);
			storage.putPendingLocked("a::1", one);
			variables.put("a::2", two);
			storage.putPendingLocked("a::2", two);
		} finally {
			storage.unlockExclusive(stamp);
		}

		Batch batch = storage.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("a::1", one, "a::2", two), RecordingStorage.changes(batch));
	}

	@Test
	public void changeDrainedBeforeClearIsClearedByTheNextBatch() {
		Map<String, Value> stored = new HashMap<>();
		set("a::1", value(1));
		RecordingStorage.apply(storage.drain(), stored);
		assertTrue(stored.containsKey("a::1"));

		clear("a::");
		Batch batch = storage.drain();
		assertEquals(List.of("a::"), batch.clears());
		RecordingStorage.apply(batch, stored);
		assertTrue(stored.isEmpty());
	}

	@Test
	public void externalWriteDropsTheLocalChange() {
		set("x", value(1));
		Value external = value(2);
		int callsBefore = storage.pendingCalls.get();
		storage.externalWrite("x", () -> variables.put("x", external));

		assertEquals(external, variables.get("x"));
		assertEquals(callsBefore, storage.pendingCalls.get());
		assertTrue(storage.drain().isEmpty());
	}

	@Test
	public void externalClearDropsLocalChangesButSavesNothing() {
		Value kept = value(2);
		set("a::1", value(1));
		set("b::1", kept);
		long stamp = storage.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith("a::"));
			storage.recordClearLocked("a::", false);
		} finally {
			storage.unlockExclusive(stamp);
		}

		Batch batch = storage.drain();
		assertTrue(batch.clears().isEmpty());
		assertEquals(Map.of("b::1", kept), RecordingStorage.changes(batch));
	}

	@Test
	public void drainEmptiesTheQueue() {
		set("x", value(1));
		clear("a::");
		assertFalse(storage.drain().isEmpty());
		assertTrue(storage.drain().isEmpty());

		// the epoch carries on across drains: a clear still drops a change made before it in the same batch
		set("a::1", value(2));
		clear("a::");
		Batch batch = storage.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertTrue(batch.changes().isEmpty());
	}

	@Test
	public void drainedBatchesMatchTheVariables() {
		Map<String, Value> stored = new HashMap<>();
		set("a::1", value(1));
		set("a::2", value(2));
		set("b", value(3));
		RecordingStorage.apply(storage.drain(), stored);
		clear("a::");
		set("a::2", value(4));
		set("b", null);
		set("a::b::1", value(5));
		clear("a::b::");
		set("c", value(6));
		RecordingStorage.apply(storage.drain(), stored);
		assertEquals(variables, stored);
	}

}
