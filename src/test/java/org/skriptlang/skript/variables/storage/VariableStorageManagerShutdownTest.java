package org.skriptlang.skript.variables.storage;

import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Tests shutting down storages with {@link VariableStorageManager#close()}, and {@link VariableStorageManager#flushAll()}.
 * Needs a server, since it logs through Skript.
 */
public class VariableStorageManagerShutdownTest {

	private static final String READABLE = "string";

	private VariableMap globals;
	private VariableStorageManager manager;

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		manager = new VariableStorageManager(globals,
			value -> value == null ? null : new SerializedVariable.Value(READABLE, String.valueOf(value).getBytes(StandardCharsets.UTF_8)),
			(type, data) -> type.equals(READABLE) ? new String(data, StandardCharsets.UTF_8) : null,
			Runnable::run);
	}

	@After
	public void tearDown() {
		close();
	}

	private static RecordingStorage storage(String name, String... prefixes) {
		RecordingStorage storage = new RecordingStorage().setPrefixesForTest(prefixes.length == 0 ? null : prefixes);
		storage.setNameForTest(name);
		storage.intervalMillis = 3_600_000; // only the final save, unless a test flushes
		return storage;
	}

	private boolean loadStorages(VariableStorage... storages) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			return manager.loadStorages(List.of(storages));
		}
	}

	private void close() {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			manager.close();
		}
	}

	private static Map<String, String> stored(RecordingStorage storage) {
		Map<String, String> stored = new HashMap<>();
		synchronized (storage.saved) {
			storage.saved.forEach((name, value) -> stored.put(name, new String(value.data, StandardCharsets.UTF_8)));
		}
		return stored;
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
