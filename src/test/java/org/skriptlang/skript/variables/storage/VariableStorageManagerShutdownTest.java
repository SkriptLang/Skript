package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.skriptlang.skript.variables.storage.ManagerTests.storage;
import static org.skriptlang.skript.variables.storage.ManagerTests.stored;
import static org.skriptlang.skript.variables.storage.ManagerTests.READABLE;

/**
 * Tests shutting down storages with {@link VariableStorageManager#close()}, and {@link VariableStorageManager#flushAll()}.
 * Needs a server, since it logs through Skript.
 */
public class VariableStorageManagerShutdownTest {

	private VariableMap globals;
	private VariableStorageManager manager;

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		manager = ManagerTests.manager(globals, Runnable::run);
	}

	@After
	public void tearDown() {
		close();
	}

	private boolean loadStorages(VariableStorage... storages) {
		return ManagerTests.loadStorages(manager, storages);
	}

	private void close() {
		ManagerTests.close(manager);
	}

	@Test
	public void closingSavesWhatIsLeftAndClosesEachStorage() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));
		manager.set("kills::steve", "1");
		manager.set("a", "2");

		close();
		assertTrue(manager.isClosed());
		assertEquals(Map.of("kills::steve", "1"), stored(kills));
		assertEquals(Map.of("a", "2"), stored(all));
		assertEquals(1, kills.closes.get());
		assertEquals(1, all.closes.get());
	}

	@Test
	public void closingTwiceDoesNothing() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		close();
		close();
		assertEquals(1, all.closes.get());
	}

	@Test
	public void closingBeforeLoadingOnlyStopsSaving() {
		close();
		assertTrue(manager.isClosed());
		manager.set("a", "1");
		assertEquals("1", globals.getVariable("a"));
	}

	@Test
	public void afterClosingChangesOnlyReachTheMap() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		manager.set("kept", "0");
		close();
		int batches = all.savedBatches.size();

		manager.set("a", "1");
		manager.setList("l", new String[] {"x"}, new Object[] {"2"});
		manager.set("kept", null);
		manager.set("m::*", null);
		assertEquals("1", globals.getVariable("a"));
		assertEquals("2", globals.getVariable("l::x"));
		assertNull(globals.getVariable("kept"));
		assertEquals(0, all.queue().count());
		assertEquals(batches, all.savedBatches.size());
		assertEquals(Map.of("kept", "0"), stored(all));
	}

	@Test
	public void reportsAfterClosingAreIgnored() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		close();
		all.reportUpdatedForTest("a", new SerializedVariable.Value(READABLE, "1".getBytes(StandardCharsets.UTF_8)));
		assertNull(globals.getVariable("a"));
	}

	@Test
	public void aStorageThatFailedToLoadIsntClosedAgain() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		kills.failLoad = new IOException("unreachable");
		assertFalse(loadStorages(kills, all));
		assertEquals(1, kills.closes.get());
		close();
		assertEquals(1, kills.closes.get());
		assertEquals(1, all.closes.get());
	}

	@Test
	public void aStorageFailingToCloseDoesntStopTheOthers() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));
		kills.failClose = true;
		manager.set("a", "1");
		close();
		assertEquals(1, kills.closes.get());
		assertEquals(1, all.closes.get());
		assertEquals(Map.of("a", "1"), stored(all));
	}

	@Test(timeout = 20_000)
	public void aStorageWithAHungSaveIsntClosedAndDoesntStopTheOthers() throws InterruptedException {
		RecordingStorage hung = storage("hung", "hung::"), all = storage("default");
		hung.intervalMillis = 10;
		hung.stopTimeoutMillis = 100;
		assertTrue(loadStorages(hung, all));
		CountDownLatch release = new CountDownLatch(1);
		hung.blockSaves = release;
		manager.set("hung::x", "1");
		hung.saveStarted.await();
		manager.set("a", "2");
		try {
			close();
			assertEquals("closed while its saving thread was still saving", 0, hung.closes.get());
			assertEquals(1, all.closes.get());
			assertEquals(Map.of("a", "2"), stored(all));
		} finally {
			release.countDown();
			hung.blockSaves = null;
		}
	}

	@Test
	public void flushAllSavesEveryStorage() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));
		manager.set("kills::steve", "1");
		manager.set("a", "2");
		assertTrue(manager.flushAll());
		assertEquals(Map.of("kills::steve", "1"), stored(kills));
		assertEquals(Map.of("a", "2"), stored(all));
	}

	@Test
	public void flushAllReportsAStorageThatCantSave() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));
		kills.failSaves.set(Integer.MAX_VALUE);
		manager.set("kills::steve", "1");
		manager.set("a", "2");
		assertFalse(manager.flushAll());
		assertEquals(Map.of("a", "2"), stored(all)); // the next storage still saved
		kills.failSaves.set(0);
	}

	@Test
	public void flushAllAfterClosingDoesNothing() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		close();
		int attempts = all.saveAttempts();
		assertTrue(manager.flushAll());
		assertEquals(attempts, all.saveAttempts());
	}

	@Test(timeout = 60_000)
	public void aWriteRacingCloseIsSavedOrOnlyInTheMap() throws InterruptedException {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		AtomicBoolean running = new AtomicBoolean(true);
		AtomicReference<Throwable> failure = new AtomicReference<>();
		CountDownLatch writing = new CountDownLatch(1);
		Thread writer = new Thread(() -> {
			try {
				for (int i = 0; running.get(); i++) {
					manager.set("n::" + (i % 100), String.valueOf(i));
					writing.countDown();
				}
			} catch (Throwable t) {
				failure.set(t);
			}
		});
		writer.start();
		writing.await();
		close();
		Thread.sleep(50); // keeps writing after the final save
		running.set(false);
		writer.join();
		if (failure.get() != null)
			throw new AssertionError(failure.get());

		assertEquals("nothing was queued after the final save", 0, all.queue().count());
		assertTrue(all.queue().drain().isEmpty());
		assertFalse(all.saved.isEmpty());
	}

}
