package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;

/**
 * Tests the saving of {@link VariableStorage}: the saving thread, retrying failed saves,
 * {@link VariableStorage#flush()} and {@link VariableStorage#stopSaving()}.
 */
public class VariableStorageSaveLoopTest {

	private static final long TIMEOUT_MILLIS = 5_000;

	private RecordingStorage storage;
	private Map<String, Value> variables;

	@Before
	public void setUp() {
		storage = new RecordingStorage();
		storage.setNameForTest("test");
		variables = new HashMap<>();
	}

	@After
	public void tearDown() {
		storage.failSaves.set(0);
		storage.errorSaves.set(0);
		storage.stopSaving();
	}

	private void set(String name, Value value) {
		storage.write(name, value, () -> variables.put(name, value));
	}

	private static Value value(int id) {
		return new Value("test", new byte[] {(byte) id});
	}

	/**
	 * Polls until the condition holds, failing after {@link #TIMEOUT_MILLIS}.
	 */
	private static void await(String what, BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MILLIS);
		while (!condition.getAsBoolean()) {
			if (System.nanoTime() > deadline)
				fail("timed out waiting until " + what);
			Thread.sleep(5);
		}
	}

	private boolean savedEquals(Map<String, Value> expected) {
		synchronized (storage.saved) {
			return storage.saved.equals(expected);
		}
	}

	private Map<String, Value> savedCopy() {
		synchronized (storage.saved) {
			return new HashMap<>(storage.saved);
		}
	}

	/**
	 * Starts saving with an interval long enough that the saving thread never runs a second cycle during a test,
	 * and waits until its first cycle, which runs straight away, is over.
	 */
	private void startSavingIdle() throws InterruptedException {
		storage.intervalMillis = 60_000;
		storage.startSaving();
		Thread.sleep(50);
	}

	private boolean reported(String part) {
		return storage.reportedFailures.stream().anyMatch(message -> message.contains(part));
	}

	// ---- the saving thread ----

	@Test(timeout = 20_000)
	public void changesAreSavedOnTheirOwn() throws InterruptedException {
		storage.startSaving();
		set("x", value(1));
		set("a::1", value(2));
		await("both changes are saved", () -> savedEquals(variables));

		// and it keeps saving later changes
		set("x", value(3));
		await("the later change is saved", () -> savedEquals(variables));
	}

	@Test(timeout = 20_000)
	public void saveRunsOnTheStoragesOwnThread() throws InterruptedException {
		storage.startSaving();
		set("x", value(1));
		await("the change is saved", () -> savedEquals(variables));
		String thread = storage.lastSaveThread;
		assertNotNull(thread);
		assertTrue("saved on '" + thread + "'", thread.startsWith("Skript variable storage test"));
		assertNotEquals(Thread.currentThread().getName(), thread);
	}

	@Test(timeout = 20_000)
	public void nothingIsSavedWhenNothingChanged() throws InterruptedException {
		storage.startSaving();
		Thread.sleep(200); // about 20 intervals
		assertEquals("save should only be called for non-empty batches", 0, storage.saveAttempts());
	}

	@Test(timeout = 20_000)
	public void lastValueWinsAcrossSeveralBatches() throws InterruptedException {
		storage.startSaving();
		for (int i = 0; i < 200; i++) {
			set("x", value(i));
			if (i % 20 == 0)
				Thread.sleep(15); // let some intervals pass, so the changes span several batches
		}
		await("the last value is saved", () -> savedEquals(variables));
		synchronized (storage.saved) {
			assertTrue("expected several batches, got " + storage.savedBatches.size(), storage.savedBatches.size() > 1);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void startingTwiceThrows() {
		storage.startSaving();
		storage.startSaving();
	}

	@Test
	public void zeroIntervalIsRejected() {
		storage.intervalMillis = 0;
		try {
			storage.startSaving();
			fail("a save interval of 0 should be rejected");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(RecordingStorage.class.getName()));
		}
	}

	// ---- failing saves ----

	@Test(timeout = 20_000)
	public void failedSaveIsRetriedUntilItWorks() throws InterruptedException {
		storage.failSaves.set(2);
		storage.startSaving();
		set("x", value(1));
		await("the change is saved after two failures", () -> savedEquals(variables));
		assertEquals(3, storage.saveAttempts());
		assertEquals(List.of(2), storage.reportedRecoveries);
	}

	@Test(timeout = 20_000)
	public void retriesBackOff() throws InterruptedException {
		storage.failSaves.set(2);
		storage.startSaving();
		set("x", value(1));
		await("the change is saved after two failures", () -> savedEquals(variables));

		List<Long> times = storage.saveAttemptTimes;
		long firstGap = TimeUnit.NANOSECONDS.toMillis(times.get(1) - times.get(0));
		long secondGap = TimeUnit.NANOSECONDS.toMillis(times.get(2) - times.get(1));
		// 1 s after the first failure, 2 s after the second, rather than the 10 ms interval
		assertTrue("first retry came after " + firstGap + " ms", firstGap >= 900);
		assertTrue("second retry came after " + secondGap + " ms", secondGap >= 1_900);
	}

	@Test(timeout = 20_000)
	public void failuresInARowAreReportedOnlyOccasionally() {
		storage.failSaves.set(25);
		set("x", value(1));
		for (int i = 0; i < 25; i++)
			assertFalse(storage.flush());
		assertTrue(storage.flush());
		// the 1st, 10th and 20th failure
		assertEquals(storage.reportedFailures.toString(), 3, storage.reportedFailures.size());
		assertEquals(List.of(25), storage.reportedRecoveries);
	}

	@Test(timeout = 20_000)
	public void failedBatchIsSavedBeforeNewerChanges() {
		storage.failSaves.set(1);
		Value oldX = value(1), oldY = value(1);
		set("x", oldX);
		set("y", oldY);
		assertFalse(storage.flush());

		Value newer = value(2);
		set("x", newer);
		assertTrue(storage.flush());

		synchronized (storage.saved) {
			assertEquals(2, storage.savedBatches.size());
			assertEquals(Map.of("x", oldX, "y", oldY), RecordingStorage.changes(storage.savedBatches.get(0)));
			assertEquals(Map.of("x", newer), RecordingStorage.changes(storage.savedBatches.get(1)));
		}
		assertEquals(variables, savedCopy());
	}

	@Test(timeout = 20_000)
	public void noNewChangesAreDrainedWhileABatchKeepsFailing() {
		storage.failSaves.set(1);
		set("x", value(1));
		assertFalse(storage.flush());

		storage.failSaves.set(1);
		set("y", value(2));
		assertFalse(storage.flush());
		assertEquals("the newer change should stay queued", 1, storage.countPending());
	}

	@Test(timeout = 20_000)
	public void errorFromSaveKeepsTheBatch() {
		storage.errorSaves.set(1);
		set("x", value(1));
		assertThrows(Error.class, storage::flush);

		assertTrue(storage.flush());
		assertEquals(variables, savedCopy());
	}

	@Test(timeout = 20_000)
	public void savingThreadSurvivesAnError() throws InterruptedException {
		storage.errorSaves.set(1);
		storage.startSaving();
		set("x", value(1));
		await("the change is saved after the error", () -> savedEquals(variables));
		assertTrue(storage.reportedFailures.toString(), reported("Unexpected error"));

		set("y", value(2));
		await("later changes are still saved", () -> savedEquals(variables));
	}

	// ---- flush ----

	@Test(timeout = 20_000)
	public void flushSavesWithoutWaitingForTheInterval() throws InterruptedException {
		startSavingIdle();
		set("x", value(1));
		assertTrue(storage.flush());
		assertEquals(variables, savedCopy());
		assertEquals(Thread.currentThread().getName(), storage.lastSaveThread);
	}

	@Test(timeout = 20_000)
	public void flushWorksWithoutTheSavingThread() {
		set("x", value(1));
		assertTrue(storage.flush());
		assertEquals(variables, savedCopy());
	}

	@Test(timeout = 20_000)
	public void flushWithNothingQueuedSavesNothing() {
		assertTrue(storage.flush());
		assertEquals(0, storage.saveAttempts());
	}

	@Test(timeout = 20_000)
	public void flushRetriesDuringBackoff() throws InterruptedException {
		storage.failSaves.set(1);
		storage.startSaving();
		set("x", value(1));
		await("the first save fails", () -> storage.saveAttempts() == 1);

		// the saving thread now waits 1 s before retrying; flush doesn't
		assertTrue(storage.flush());
		assertEquals(variables, savedCopy());
	}

	// ---- stopping ----

	@Test(timeout = 20_000)
	public void stopSavingSavesWhatIsLeft() throws InterruptedException {
		startSavingIdle();
		set("x", value(1));
		set("a::1", value(2));
		storage.stopSaving();
		assertEquals(variables, savedCopy());
		assertTrue(storage.reportedFailures.toString(), storage.reportedFailures.isEmpty());
	}

	@Test(timeout = 20_000)
	public void stopSavingStopsTheThread() throws InterruptedException {
		storage.startSaving();
		storage.stopSaving();
		assertTrue("the saving thread is still running",
			Thread.getAllStackTraces().keySet().stream()
				.noneMatch(thread -> thread.getName().startsWith("Skript variable storage test")));
	}

	@Test(timeout = 20_000)
	public void stopSavingRetriesAFailingSave() throws InterruptedException {
		startSavingIdle();
		storage.failSaves.set(2);
		set("x", value(1));
		storage.stopSaving();
		assertEquals(variables, savedCopy());
	}

	@Test(timeout = 20_000)
	public void stopSavingReportsWhatIsLost() throws InterruptedException {
		startSavingIdle();
		storage.failSaves.set(Integer.MAX_VALUE);
		set("x", value(1));
		set("y", value(2));
		set("z", value(3));
		long start = System.nanoTime();
		storage.stopSaving();
		long took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

		assertEquals("three final attempts", 3, storage.saveAttempts());
		assertTrue(storage.reportedFailures.toString(), reported("Could not save 3 variable changes to the database 'test' before shutting down"));
		assertTrue("final attempts shouldn't back off, but stopping took " + took + " ms", took < 1_000);
	}

	@Test
	public void stopSavingTwiceIsHarmless() throws InterruptedException {
		startSavingIdle();
		storage.stopSaving();
		storage.stopSaving();
	}

}
