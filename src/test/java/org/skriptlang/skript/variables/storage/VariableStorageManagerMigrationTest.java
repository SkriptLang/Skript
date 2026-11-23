package org.skriptlang.skript.variables.storage;

import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.skriptlang.skript.variables.storage.ManagerTests.value;
import static org.skriptlang.skript.variables.storage.ManagerTests.storage;
import static org.skriptlang.skript.variables.storage.VariableStorageManagerLoadTest.savedBy;

/**
 * Tests what {@link VariableStorageManager} does with variables found in the wrong storage at startup,
 * and with changes storages report from outside the server.
 * Needs a server, since it logs through Skript.
 */
public class VariableStorageManagerMigrationTest {

	private VariableMap globals;
	private VariableStorageManager manager;
	private final List<Runnable> mainThreadTasks = new ArrayList<>();

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		manager = ManagerTests.manager(globals, mainThreadTasks::add);
	}

	@After
	public void tearDown() {
		for (VariableStorage storage : manager.storages())
			storage.stopSaving();
	}

	private boolean loadStorages(VariableStorage... storages) {
		boolean loaded = ManagerTests.loadStorages(manager, storages);
		RecordingStorage.awaitSavingIdle(manager); // so the first save cycles can't drain what the test queues next
		return loaded;
	}

	private void runMainThreadTasks() {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			List<Runnable> tasks = new ArrayList<>(mainThreadTasks);
			mainThreadTasks.clear();
			tasks.forEach(Runnable::run);
		}
	}

	// what a storage holds once it has stopped saving, values read back as text
	private static Map<String, String> stored(RecordingStorage storage) {
		storage.stopSaving();
		return ManagerTests.stored(storage);
	}

	// ---- moving variables at startup ----

	@Test
	public void aMisplacedVariableMovesToItsOwner() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		SerializedVariable.Value steve = value("3");
		all.saved.put("kills::steve", steve);
		all.saved.put("deaths::steve", value("1"));

		assertTrue(loadStorages(kills, all));
		assertEquals("3", globals.getVariable("kills::steve"));
		synchronized (kills.saved) {
			assertSame("the stored bytes are moved, not serialized again", steve, kills.saved.get("kills::steve"));
		}
		assertEquals(Map.of("deaths::steve", "1"), stored(all));
	}

	@Test
	public void theOwnersCopyWinsWhenItLoadsAfterAMisplacedOne() {
		RecordingStorage bank = storage("bank", "bank::"), kills = storage("kills", "kills::");
		bank.saved.put("kills::steve", value("stale")); // bank loads first, but doesn't own it
		kills.saved.put("kills::steve", value("owner"));

		assertTrue(loadStorages(bank, kills));
		assertEquals("owner", globals.getVariable("kills::steve"));
		assertEquals(Map.of("kills::steve", "owner"), stored(kills));
		assertEquals(Map.of(), stored(bank));
	}

	@Test
	public void theOwnersCopyWinsWhenItLoadsBeforeAMisplacedOne() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		kills.saved.put("kills::steve", value("owner"));
		all.saved.put("kills::steve", value("stale"));

		assertTrue(loadStorages(kills, all));
		assertEquals("owner", globals.getVariable("kills::steve"));
		assertEquals(Map.of("kills::steve", "owner"), stored(kills));
		assertEquals(Map.of(), stored(all));
	}

	@Test
	public void withTwoMisplacedCopiesTheFirstMovesAndBothAreDeleted() {
		RecordingStorage kills = storage("kills", "kills::"), bank = storage("bank", "bank::"), all = storage("default");
		bank.saved.put("kills::steve", value("first"));
		all.saved.put("kills::steve", value("second"));

		assertTrue(loadStorages(kills, bank, all));
		assertEquals("first", globals.getVariable("kills::steve"));
		assertEquals(Map.of("kills::steve", "first"), stored(kills));
		assertEquals(Map.of(), stored(bank));
		assertEquals(Map.of(), stored(all));
	}

	@Test
	public void ifTheOwnerCantSaveTheCopyIsKept() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		kills.failSaves.set(Integer.MAX_VALUE);
		all.saved.put("kills::steve", value("3"));

		assertTrue(loadStorages(kills, all));
		assertEquals("3", globals.getVariable("kills::steve"));
		assertEquals(Map.of("kills::steve", "3"), stored(all));
	}

	@Test
	public void ifTheOwnerFailedToLoadTheCopyStaysWhereItIs() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		kills.failLoad = new IOException("unreachable");
		all.saved.put("kills::steve", value("3"));

		assertFalse(loadStorages(kills, all));
		assertEquals("3", globals.getVariable("kills::steve"));
		assertEquals(Map.of("kills::steve", "3"), stored(all));
	}

	@Test
	public void variablesNoStorageOwnsStayWhereTheyAre() {
		RecordingStorage kills = storage("kills", "kills::");
		kills.saved.put("deaths::steve", value("1"));
		kills.saved.put("-temp", value("2"));

		assertTrue(loadStorages(kills));
		assertEquals("1", globals.getVariable("deaths::steve"));
		assertEquals("2", globals.getVariable("-temp"));
		assertEquals(Map.of("deaths::steve", "1", "-temp", "2"), stored(kills));
	}

	@Test
	public void unreadableMisplacedVariablesStayWhereTheyAre() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		SerializedVariable.Value unreadable = new SerializedVariable.Value("removed addon type", new byte[] {1});
		all.saved.put("kills::steve", unreadable);

		assertTrue(loadStorages(kills, all));
		assertTrue(stored(kills).isEmpty());
		synchronized (all.saved) {
			assertSame(unreadable, all.saved.get("kills::steve"));
		}
	}

	// ---- reported changes ----

	@Test
	public void aReportedUpdateIsAppliedOnTheMainThread() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));

		kills.reportUpdatedForTest("kills::steve", value("1"));
		kills.reportUpdatedForTest("kills::steve", value("2"));
		assertNull("applied only on the main thread", globals.getVariable("kills::steve"));
		runMainThreadTasks();
		assertEquals("2", globals.getVariable("kills::steve")); // in the order reported

		kills.reportUpdatedForTest("kills::steve", null);
		runMainThreadTasks();
		assertNull(globals.getVariable("kills::steve"));
	}

	@Test
	public void manyReportsShareOneMainThreadTask() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));

		for (int i = 0; i < 1000; i++)
			kills.reportUpdatedForTest("kills::" + i, value(String.valueOf(i)));
		assertEquals(1, mainThreadTasks.size());
		runMainThreadTasks();
		assertTrue(mainThreadTasks.isEmpty());
		assertEquals("999", globals.getVariable("kills::999"));
	}

	@Test
	public void reportsPastTheTimeLimitWaitForTheNextTask() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));

		List<Integer> ran = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			int task = i;
			kills.runAfterReportsForTest(() -> {
				ran.add(task);
				sleep(3); // two of these are over the 5 ms limit
			});
		}
		kills.reportUpdatedForTest("kills::steve", value("1"));
		runMainThreadTasks();
		assertTrue("stops once over the limit: " + ran, ran.size() < 4);
		assertEquals("the rest are left for the next task", 1, mainThreadTasks.size());
		assertNull(globals.getVariable("kills::steve"));

		while (!mainThreadTasks.isEmpty())
			runMainThreadTasks();
		assertEquals(List.of(0, 1, 2, 3), ran); // still in order
		assertEquals("1", globals.getVariable("kills::steve"));
	}

	@Test
	public void aFailingReportTaskDoesntStopTheRest() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));

		kills.runAfterReportsForTest(() -> {
			throw new IllegalStateException("broken on purpose");
		});
		kills.reportUpdatedForTest("kills::steve", value("1"));
		runMainThreadTasks();
		assertEquals("1", globals.getVariable("kills::steve"));

		kills.reportUpdatedForTest("kills::steve", value("2")); // and the queue still runs later reports
		runMainThreadTasks();
		assertEquals("2", globals.getVariable("kills::steve"));
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	@Test
	public void reportedChangesArentSavedAgain() {
		RecordingStorage kills = storage("kills", "kills::");
		assertTrue(loadStorages(kills));
		kills.reportUpdatedForTest("kills::steve", value("1"));
		kills.reportClearedForTest("kills::*");
		runMainThreadTasks();
		assertTrue(savedBy(kills).isEmpty());
	}

	@Test
	public void anUpdateFromAStorageThatDoesntSaveTheVariableIsIgnored() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));
		manager.set("kills::steve", "local");

		all.reportUpdatedForTest("kills::steve", value("remote"));
		all.reportUpdatedForTest("-temp", value("remote"));
		runMainThreadTasks();
		assertEquals("local", globals.getVariable("kills::steve"));
		assertNull(globals.getVariable("-temp"));
	}

	@Test
	public void anUnreadableUpdateIsIgnored() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		manager.set("a", "local");
		all.reportUpdatedForTest("a", new SerializedVariable.Value("removed addon type", new byte[] {1}));
		runMainThreadTasks();
		assertEquals("local", globals.getVariable("a"));
	}

	@Test
	public void aKeepLocalReportDoesntReplaceAnUnsavedChange() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		manager.set("a", "local");

		all.reportUpdatedForTest("a", value("remote"), VariableStorage.ReportPolicy.KEEP_LOCAL);
		runMainThreadTasks();
		assertEquals("local", globals.getVariable("a"));
		assertEquals(Map.of("a", "local"), stored(all)); // still saved, after the reported change
	}

	@Test
	public void aKeepLocalReportUnderAnUnsavedClearIsIgnored() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		manager.set("a::1", "1");
		assertTrue(all.flush());
		manager.set("a::*", null);

		all.reportUpdatedForTest("a::1", value("remote"), VariableStorage.ReportPolicy.KEEP_LOCAL);
		runMainThreadTasks();
		assertNull(globals.getVariable("a::1"));
		assertTrue(stored(all).isEmpty()); // the clear is still saved
	}

	@Test
	public void aKeepLocalReportIsAppliedOnceTheChangeIsSaved() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		manager.set("a", "local");
		assertTrue(all.flush());

		all.reportUpdatedForTest("a", value("remote"), VariableStorage.ReportPolicy.KEEP_LOCAL);
		runMainThreadTasks();
		assertEquals("remote", globals.getVariable("a"));
		assertEquals(Map.of("a", "local"), stored(all)); // the reported value isn't saved back
	}

	@Test
	public void aReportedClearOfAWholeListClearsIt() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		manager.set("a::1", "1");
		manager.set("a::b::2", "2");
		manager.set("a", "own value");

		all.reportClearedForTest("a::*");
		assertEquals("1", globals.getVariable("a::1"));
		runMainThreadTasks();
		assertNull(globals.getVariable("a::1"));
		assertNull(globals.getVariable("a::b::2"));
		assertEquals("own value", globals.getVariable("a"));
	}

	@Test
	public void aReportedClearOnlyRemovesWhatTheStorageSaves() {
		RecordingStorage partB = storage("b", "a::b"), all = storage("default");
		assertTrue(loadStorages(partB, all));
		manager.set("a::b1", "1");
		manager.set("a::b::x", "2");
		manager.set("a::b", "3");
		manager.set("a::c", "4");
		manager.set("a::c::b", "5"); // starts with a::c, so it's not partB's

		partB.reportClearedForTest("a::*");
		runMainThreadTasks();
		assertNull(globals.getVariable("a::b1"));
		assertNull(globals.getVariable("a::b::x"));
		assertNull(globals.getVariable("a::b"));
		assertEquals("4", globals.getVariable("a::c"));
		assertEquals("5", globals.getVariable("a::c::b"));
	}

	@Test
	public void aClearFromAStorageThatDoesntSaveTheListIsIgnored() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		assertTrue(loadStorages(kills, all));
		manager.set("kills::steve", "1");
		all.reportClearedForTest("kills::*");
		runMainThreadTasks();
		assertEquals("1", globals.getVariable("kills::steve"));
	}

	@Test
	public void reportsAfterStoppingAreIgnored() {
		RecordingStorage all = storage("default");
		assertTrue(loadStorages(all));
		all.stopSaving();
		all.reportUpdatedForTest("a", value("1"));
		runMainThreadTasks();
		assertNull(globals.getVariable("a"));
	}

	@Test
	public void aFailedStorageReceivesNothing() {
		RecordingStorage kills = storage("kills", "kills::");
		kills.failLoad = new IOException("unreachable");
		assertFalse(loadStorages(kills));
		kills.reportUpdatedForTest("kills::steve", value("1")); // ignored rather than thrown
		assertTrue(mainThreadTasks.isEmpty());
	}

}
