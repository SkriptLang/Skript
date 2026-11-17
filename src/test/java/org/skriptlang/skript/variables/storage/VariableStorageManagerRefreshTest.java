package org.skriptlang.skript.variables.storage;

import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests how {@link VariableStorageManager} drives the {@link VariableRefresher}: what loading and reported changes
 * tell it, the refresh at the end of each tick, and the last refresh when shutting down.
 * Needs a server, since it logs through Skript.
 */
public class VariableStorageManagerRefreshTest {

	private static final String READABLE = "string";
	// later than any pass could be due, since passes are minutes apart
	private static final long A_DAY = TimeUnit.DAYS.toNanos(1);

	/**
	 * A value that changes in place, like an item renamed by an addon. Saved as its text.
	 */
	private static final class Box {
		String text;

		Box(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private VariableMap globals;
	private VariableStorageManager manager;
	private RecordingStorage storage;

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		manager = new VariableStorageManager(globals,
			value -> value == null ? null : new SerializedVariable.Value(READABLE, String.valueOf(value).getBytes(StandardCharsets.UTF_8)),
			(type, data) -> type.equals(READABLE) ? new String(data, StandardCharsets.UTF_8) : null,
			Runnable::run);
		storage = new RecordingStorage();
		storage.setNameForTest("default");
		storage.intervalMillis = 3_600_000; // only saves when a test flushes, or at the end
	}

	@After
	public void tearDown() {
		close();
	}

	private void load() {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertTrue(manager.loadStorages(List.of(storage)));
		}
	}

	private void close() {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			manager.close();
		}
	}

	private static SerializedVariable.Value value(String text) {
		return new SerializedVariable.Value(READABLE, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * The first tick, which only schedules the first pass, then a tick once it's due, with time for all of it.
	 */
	private void tickThroughAPass() {
		manager.refreshTick(System.nanoTime(), System.nanoTime() + A_DAY);
		manager.refreshTick(System.nanoTime() + A_DAY, System.nanoTime() + A_DAY);
	}

	/**
	 * Every value saved for a variable, in order, across every saved batch.
	 */
	private List<String> savesOf(String name) {
		List<String> saves = new ArrayList<>();
		synchronized (storage.saved) {
			for (VariableStorage.Batch batch : storage.savedBatches) {
				for (SerializedVariable change : batch.changes()) {
					if (change.name.equals(name))
						saves.add(change.value == null ? null : new String(change.value.data, StandardCharsets.UTF_8));
				}
			}
		}
		return saves;
	}

	@Test
	public void loadedVariablesArentSavedAgainByARefresh() {
		storage.saved.put("loaded", value("stored"));
		load();
		tickThroughAPass();
		assertTrue(manager.flushAll());
		assertEquals(List.of(), savesOf("loaded"));
	}

	@Test
	public void aValueChangedInPlaceIsSavedByARefresh() {
		load();
		Box box = new Box("old");
		manager.set("box", box);
		assertTrue(manager.flushAll());
		box.text = "new";
		tickThroughAPass();
		assertTrue(manager.flushAll());
		assertEquals(List.of("old", "new"), savesOf("box"));
		assertEquals("new", new String(storage.saved.get("box").data, StandardCharsets.UTF_8));
	}

	@Test
	public void aValueSetThenChangedBackInPlaceIsSavedByARefresh() {
		load();
		manager.set("box", new Box("A"));
		tickThroughAPass(); // the refresh's table has A now
		Box box = new Box("B");
		manager.set("box", box);
		assertTrue(manager.flushAll());
		box.text = "A";
		tickThroughAPass();
		assertTrue(manager.flushAll());
		assertEquals("A", new String(storage.saved.get("box").data, StandardCharsets.UTF_8));
	}

	@Test
	public void anElementSetInAListThenChangedBackInPlaceIsSavedByARefresh() {
		load();
		manager.setList("list", new String[] {"1"}, new Object[] {new Box("A")});
		tickThroughAPass();
		Box box = new Box("B");
		manager.setList("list", new String[] {"1"}, new Object[] {box});
		assertTrue(manager.flushAll());
		box.text = "A";
		tickThroughAPass();
		assertTrue(manager.flushAll());
		assertEquals("A", new String(storage.saved.get("list::1").data, StandardCharsets.UTF_8));
	}

	@Test
	public void aReportedChangeIsntSavedBack() {
		load();
		storage.reportUpdatedForTest("reported", value("from elsewhere"));
		assertEquals("from elsewhere", globals.getVariable("reported"));
		tickThroughAPass();
		assertTrue(manager.flushAll());
		assertEquals(List.of(), savesOf("reported"));
	}

	@Test
	public void shuttingDownSavesValuesChangedInPlace() {
		load();
		Box box = new Box("old");
		manager.set("box", box);
		assertTrue(manager.flushAll());
		box.text = "changed after the last refresh";
		close();
		assertEquals("changed after the last refresh", new String(storage.saved.get("box").data, StandardCharsets.UTF_8));
	}

	@Test
	public void refreshingStopsOnceClosed() {
		load();
		Box box = new Box("old");
		manager.set("box", box);
		close();
		int batches = storage.savedBatches.size();
		box.text = "new";
		tickThroughAPass();
		assertEquals(batches, storage.savedBatches.size());
		assertEquals("old", new String(storage.saved.get("box").data, StandardCharsets.UTF_8));
	}

}
