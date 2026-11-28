package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;
import org.skriptlang.skript.variables.storage.VariableStorage.ReportPolicy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests {@link ChangeQueue} on one thread.
 * A plain map stands in for Skript's variables, and the test plays the part of the router for clears.
 */
public class ChangeQueueTest {

	private ChangeQueue queue;
	private Map<String, Value> variables;

	@Before
	public void setUp() {
		queue = new ChangeQueue();
		variables = new HashMap<>();
	}

	private static Value value(int id) {
		return new Value("test", new byte[] {(byte) id});
	}

	private void set(String name, Value value) {
		queue.write(name, value, () -> {
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
		long stamp = queue.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith(prefix));
			queue.recordClear(prefix, true);
		} finally {
			queue.unlockExclusive(stamp);
		}
	}

	/**
	 * Applies a clear the storage reported, the way the manager does: Skript's variables are cleared,
	 * but the clear is only recorded to drop local changes, not to be saved.
	 */
	private void externalClear(String prefix) {
		long stamp = queue.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith(prefix));
			queue.recordClear(prefix, false);
		} finally {
			queue.unlockExclusive(stamp);
		}
	}

	/**
	 * Applies a change the storage reported, with the given policy.
	 *
	 * @return whether the change reached Skript's variables
	 */
	private boolean report(String name, Value value, ReportPolicy policy) {
		return queue.externalWrite(name, policy, () -> variables.put(name, value));
	}

	@Test
	public void writesToOneNameKeepTheLast() {
		Value last = value(3);
		set("x", value(1));
		set("x", value(2));
		set("x", last);
		Batch batch = queue.drain();
		assertEquals(Map.of("x", last), RecordingStorage.changes(batch));
		assertTrue(batch.clears().isEmpty());
	}

	@Test
	public void deleteDrainsAsNullValue() {
		set("x", value(1));
		set("x", null);
		Map<String, Value> changes = RecordingStorage.changes(queue.drain());
		assertTrue(changes.containsKey("x"));
		assertNull(changes.get("x"));
	}

	@Test
	public void mapWriteRunsOncePerWrite() {
		AtomicInteger runs = new AtomicInteger();
		for (int i = 0; i < 100; i++)
			queue.write("x", value(i), runs::incrementAndGet);
		assertEquals(100, runs.get());
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

		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("a", own, "ab::x", similar, "b::1", other), RecordingStorage.changes(batch));
	}

	@Test
	public void writeAfterClearInTheSameBatchSurvives() {
		set("a::1", value(1));
		clear("a::");
		Value after = value(2);
		set("a::1", after);

		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("a::1", after), RecordingStorage.changes(batch));
	}

	@Test
	public void writeBetweenTwoClearsIsDropped() {
		clear("a::");
		set("a::1", value(1));
		clear("a::");

		Batch batch = queue.drain();
		assertEquals(List.of("a::", "a::"), batch.clears());
		assertTrue(batch.changes().isEmpty());
	}

	@Test
	public void clearOfNestedListOnlyDropsThatList() {
		Value kept = value(1);
		set("a::1", kept);
		set("a::b::1", value(2));
		clear("a::b::");
		assertEquals(Map.of("a::1", kept), RecordingStorage.changes(queue.drain()));
	}

	@Test
	public void setListKeepsItsNewEntries() {
		set("a::old", value(1));
		Value one = value(2), two = value(3);
		long stamp = queue.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith("a::"));
			queue.recordClear("a::", true);
			variables.put("a::1", one);
			queue.putPending("a::1", one);
			variables.put("a::2", two);
			queue.putPending("a::2", two);
		} finally {
			queue.unlockExclusive(stamp);
		}

		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("a::1", one, "a::2", two), RecordingStorage.changes(batch));
	}

	@Test
	public void changeDrainedBeforeClearIsClearedByTheNextBatch() {
		Map<String, Value> stored = new HashMap<>();
		set("a::1", value(1));
		RecordingStorage.apply(queue.drain(), stored);
		assertTrue(stored.containsKey("a::1"));

		clear("a::");
		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		RecordingStorage.apply(batch, stored);
		assertTrue(stored.isEmpty());
	}

	@Test
	public void externalWriteDropsTheLocalChange() {
		set("x", value(1));
		Value external = value(2);
		assertTrue(report("x", external, ReportPolicy.REPLACE_LOCAL));

		assertEquals(external, variables.get("x"));
		assertTrue(queue.drain().isEmpty());
	}

	@Test
	public void replaceLocalStillAppliesUnderALocalClear() {
		clear("a::");
		Value external = value(1);
		assertTrue(report("a::1", external, ReportPolicy.REPLACE_LOCAL));

		assertEquals(external, variables.get("a::1"));
		// the clear is still saved after the reported change, as before the policies existed
		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertTrue(batch.changes().isEmpty());
	}

	// ---- KEEP_LOCAL ----

	@Test
	public void keepLocalSkipsAnUnsavedChange() {
		Value local = value(1);
		set("x", local);
		assertFalse(report("x", value(2), ReportPolicy.KEEP_LOCAL));

		assertEquals(local, variables.get("x"));
		assertEquals(Map.of("x", local), RecordingStorage.changes(queue.drain()));
	}

	@Test
	public void keepLocalSkipsAnUnsavedDeletion() {
		set("x", value(1));
		set("x", null);
		assertFalse(report("x", value(2), ReportPolicy.KEEP_LOCAL));

		assertNull(variables.get("x"));
		Batch batch = queue.drain();
		assertEquals(1, batch.changes().size());
		assertNull(batch.changes().iterator().next().value);
	}

	@Test
	public void keepLocalSkipsUnderAnUnsavedLocalClear() {
		clear("a::");
		assertFalse(report("a::b::1", value(1), ReportPolicy.KEEP_LOCAL));

		assertNull(variables.get("a::b::1"));
		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertTrue(batch.changes().isEmpty());
	}

	@Test
	public void keepLocalIgnoresClearsOfOtherLists() {
		clear("b::");
		clear("a::b::");
		Value external = value(1);
		assertTrue(report("a::1", external, ReportPolicy.KEEP_LOCAL));
		assertTrue(report("a::bc", external, ReportPolicy.KEEP_LOCAL));
		assertTrue(report("a", external, ReportPolicy.KEEP_LOCAL));

		assertEquals(external, variables.get("a::1"));
		assertEquals(external, variables.get("a::bc"));
		assertEquals(external, variables.get("a"));
	}

	@Test
	public void keepLocalAppliesWhenNothingIsUnsaved() {
		Value external = value(1);
		assertTrue(report("x", external, ReportPolicy.KEEP_LOCAL));

		assertEquals(external, variables.get("x"));
		assertTrue(queue.drain().isEmpty());
	}

	@Test
	public void keepLocalAppliesOnceTheChangeIsDrained() {
		set("x", value(1));
		queue.drain();
		Value external = value(2);
		assertTrue(report("x", external, ReportPolicy.KEEP_LOCAL));

		assertEquals(external, variables.get("x"));
		assertTrue(queue.drain().isEmpty());
	}

	@Test
	public void keepLocalAppliesOnceTheClearIsDrained() {
		clear("a::");
		queue.drain();
		Value external = value(1);
		assertTrue(report("a::1", external, ReportPolicy.KEEP_LOCAL));

		assertEquals(external, variables.get("a::1"));
	}

	@Test
	public void keepLocalAppliesWhenAReportedClearDroppedTheLocalChange() {
		set("a::1", value(1));
		externalClear("a::"); // the local change is still pending, but draining drops it
		Value external = value(2);
		assertTrue(report("a::1", external, ReportPolicy.KEEP_LOCAL));

		assertEquals(external, variables.get("a::1"));
		assertTrue(queue.drain().isEmpty());
	}

	@Test
	public void keepLocalSkipsAChangeMadeAfterAReportedClear() {
		externalClear("a::");
		Value local = value(1);
		set("a::1", local);
		assertFalse(report("a::1", value(2), ReportPolicy.KEEP_LOCAL));

		assertEquals(local, variables.get("a::1"));
		assertEquals(Map.of("a::1", local), RecordingStorage.changes(queue.drain()));
	}

	@Test
	public void keepLocalSkipsAChangeMadeBetweenTwoReportedClears() {
		externalClear("a::");
		Value local = value(1);
		set("a::1", local);
		externalClear("b::"); // newer, but doesn't cover a::1, so the local change is still live
		assertFalse(report("a::1", value(2), ReportPolicy.KEEP_LOCAL));

		assertEquals(local, variables.get("a::1"));
	}

	@Test
	public void keepLocalAppliesAfterClosing() {
		set("x", value(1));
		queue.close();
		Value external = value(2);
		assertTrue(report("x", external, ReportPolicy.KEEP_LOCAL));

		assertEquals(external, variables.get("x"));
	}

	@Test
	public void changesQueuedBeforeClosingAreStillDrained() {
		Value before = value(1);
		set("x", before);
		clear("a::");
		queue.close();
		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(Map.of("x", before), RecordingStorage.changes(batch));
		assertEquals(0, queue.count());
	}

	@Test
	public void afterClosingChangesOnlyReachTheVariables() {
		queue.close();
		Value after = value(1);
		set("x", after);
		clear("a::");
		long stamp = queue.lockExclusive();
		try {
			queue.putPending("y", value(2));
		} finally {
			queue.unlockExclusive(stamp);
		}
		Value external = value(3);
		assertTrue(report("z", external, ReportPolicy.REPLACE_LOCAL));

		assertEquals(after, variables.get("x"));
		assertEquals(external, variables.get("z"));
		assertEquals(0, queue.count());
		assertTrue(queue.drain().isEmpty());
	}

	@Test
	public void externalClearDropsLocalChangesButSavesNothing() {
		Value kept = value(2);
		set("a::1", value(1));
		set("b::1", kept);
		externalClear("a::");

		Batch batch = queue.drain();
		assertTrue(batch.clears().isEmpty());
		assertEquals(Map.of("b::1", kept), RecordingStorage.changes(batch));
	}

	@Test
	public void drainEmptiesTheQueue() {
		set("x", value(1));
		clear("a::");
		assertFalse(queue.drain().isEmpty());
		assertTrue(queue.drain().isEmpty());

		// the epoch carries on across drains: a clear still drops a change made before it in the same batch
		set("a::1", value(2));
		clear("a::");
		Batch batch = queue.drain();
		assertEquals(List.of("a::"), batch.clears());
		assertTrue(batch.changes().isEmpty());
	}

	@Test
	public void drainedBatchesMatchTheVariables() {
		Map<String, Value> stored = new HashMap<>();
		set("a::1", value(1));
		set("a::2", value(2));
		set("b", value(3));
		RecordingStorage.apply(queue.drain(), stored);
		clear("a::");
		set("a::2", value(4));
		set("b", null);
		set("a::b::1", value(5));
		clear("a::b::");
		set("c", value(6));
		RecordingStorage.apply(queue.drain(), stored);
		assertEquals(variables, stored);
	}

	// ---- presizing the replacement map (decaying peak of drain sizes) ----

	private int sizeHint() {
		try {
			Field field = ChangeQueue.class.getDeclaredField("sizeHint");
			field.setAccessible(true);
			return field.getInt(queue);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private void writeMany(String prefix, int count) {
		for (int i = 0; i < count; i++)
			set(prefix + i, value(i));
	}

	@Test
	public void drainAfterABigDrainHasExactlyTheNewChanges() {
		writeMany("big::", 10_000);
		assertEquals(10_000, queue.drain().changes().size());

		// the next map was presized and had a dummy entry put in and removed again
		Value overwritten = value(1);
		set("big::5", overwritten);
		writeMany("small::", 50);
		Map<String, Value> changes = RecordingStorage.changes(queue.drain());
		assertEquals(51, changes.size());
		assertEquals(overwritten, changes.get("big::5"));
		assertFalse("the allocation dummy leaked into a batch", changes.containsKey(""));
	}

	@Test
	public void emptyDrainAfterABigDrainIsEmpty() {
		writeMany("big::", 10_000);
		queue.drain();
		assertTrue(queue.drain().isEmpty());
		// still empty, and still working, after the presized map was discarded unused
		Value later = value(1);
		set("later", later);
		assertEquals(Map.of("later", later), RecordingStorage.changes(queue.drain()));
	}

	@Test
	public void sizeHintRisesAtOnce() {
		writeMany("a::", 1_000);
		queue.drain();
		assertEquals(1_000, sizeHint());

		writeMany("b::", 5_000);
		queue.drain();
		assertEquals(5_000, sizeHint());
	}

	@Test
	public void sizeHintFallsByAQuarterPerSmallDrain() {
		writeMany("a::", 1_000);
		queue.drain();
		set("x", value(1));
		queue.drain();
		assertEquals(750, sizeHint());
	}

	@Test
	public void sizeHintFallsDuringEmptyDrains() {
		writeMany("a::", 1_000);
		queue.drain();
		for (int i = 0; i < 20; i++)
			queue.drain(); // nothing queued
		assertTrue("hint should have decayed on empty drains, but is " + sizeHint(), sizeHint() <= 64);
	}

	@Test
	public void sizeHintDecaysToSmallWithinAMinuteOfQuietAfterABurst() {
		writeMany("burst::", 100_000);
		queue.drain();
		int drains = 0;
		while (sizeHint() > 64 && drains < 120) {
			set("quiet", value(drains));
			queue.drain();
			drains++;
		}
		assertTrue("took " + drains + " one-second drains to decay", drains <= 60);
	}

	// ---- what a reported clear keeps ----

	/**
	 * @return what {@link ChangeQueue#unsavedUnder} gives for the prefix, by name, or null if a local clear covers it
	 */
	private Map<String, Value> unsavedUnder(String prefix) {
		long stamp = queue.lockExclusive();
		try {
			List<SerializedVariable> kept = queue.unsavedUnder(prefix);
			if (kept == null)
				return null;
			Map<String, Value> byName = new HashMap<>();
			for (SerializedVariable change : kept)
				byName.put(change.name, change.value);
			return byName;
		} finally {
			queue.unlockExclusive(stamp);
		}
	}

	@Test
	public void aReportedClearKeepsPendingChangesInTheList() {
		Value kept = value(1);
		set("a::1", kept);
		set("a::sub::2", null);
		set("a", value(3)); // the list's own name isn't in it
		set("ab::1", value(4));

		Map<String, Value> expected = new HashMap<>();
		expected.put("a::1", kept);
		expected.put("a::sub::2", null); // a pending deletion is kept too, so it's still saved
		assertEquals(expected, unsavedUnder("a::"));
	}

	@Test
	public void aReportedClearOfAListALocalClearCoversIsIgnored() {
		clear("a::");
		set("a::1", value(1));
		assertNull(unsavedUnder("a::"));
		assertNull("a clear of the list it's in covers it too", unsavedUnder("a::b::"));
	}

	@Test
	public void aLocalClearInsideTheListOnlyDropsWhatItCovers() {
		Value outside = value(2), after = value(3);
		set("a::b::1", value(1));
		set("a::c", outside);
		clear("a::b::");
		set("a::b::2", after);

		assertEquals(Map.of("a::c", outside, "a::b::2", after), unsavedUnder("a::"));
	}

	@Test
	public void changesAnEarlierReportedClearDroppedArentKept() {
		set("a::1", value(1));
		externalClear("a::"); // drops a::1 when draining
		Value after = value(2);
		set("a::2", after);

		assertEquals(Map.of("a::2", after), unsavedUnder("a::"));
	}

	@Test
	public void aReportedClearKeepsNothingOnceClosed() {
		set("a::1", value(1));
		queue.close();
		assertEquals(Map.of(), unsavedUnder("a::"));
	}

}
