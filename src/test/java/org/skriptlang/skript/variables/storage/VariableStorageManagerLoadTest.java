package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests loading storages with {@link VariableStorageManager}: reading the {@code databases} section,
 * loading each storage's variables, and what happens when a storage fails.
 * Needs a server, since it logs through Skript.
 */
public class VariableStorageManagerLoadTest {

	private static final String READABLE = "string";

	private VariableMap globals;
	private VariableStorageManager manager;
	private final List<RecordingStorage> created = new ArrayList<>();

	@Before
	public void setUp() {
		globals = VariableMap.concurrent();
		manager = new VariableStorageManager(globals,
			value -> value == null ? null : value(String.valueOf(value)),
			(type, data) -> type.equals(READABLE) ? new String(data, StandardCharsets.UTF_8) : null,
			Runnable::run);
	}

	@After
	public void tearDown() {
		for (VariableStorage storage : manager.storages())
			storage.stopSaving();
	}

	private static SerializedVariable.Value value(String text) {
		return new SerializedVariable.Value(READABLE, text.getBytes(StandardCharsets.UTF_8));
	}

	private static RecordingStorage storage(String name, String... prefixes) {
		RecordingStorage storage = new RecordingStorage().setPrefixesForTest(prefixes.length == 0 ? null : prefixes);
		storage.setNameForTest(name);
		storage.intervalMillis = 3_600_000; // so the saving thread doesn't drain the queue before a test reads it
		return storage;
	}

	private boolean loadStorages(VariableStorage... storages) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) { // keeps expected errors out of the log
			return manager.loadStorages(List.of(storages));
		}
	}

	private boolean load(String config) throws IOException {
		manager.register(null, RecordingStorage.class, (addon, type) -> {
			RecordingStorage storage = new RecordingStorage();
			created.add(storage);
			return storage;
		}, "recording");
		SectionNode databases = databases(config);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			return manager.load(databases);
		}
	}

	private static SectionNode databases(String config) throws IOException {
		Config parsed = new Config(new ByteArrayInputStream(("databases:\n" + config).getBytes(StandardCharsets.UTF_8)),
			"test.sk", false, true, ":");
		return (SectionNode) parsed.getMainNode().get("databases");
	}

	private List<String> storageNames() {
		return manager.storages().stream().map(VariableStorage::getName).toList();
	}

	// stops the storage's saving, which saves everything left, and returns every batch it saved as one.
	// reading the queue instead would race the saving thread, which saves once as soon as it starts
	static Batch savedBy(RecordingStorage storage) {
		storage.stopSaving();
		List<String> clears = new ArrayList<>();
		List<SerializedVariable> changes = new ArrayList<>();
		synchronized (storage.saved) {
			for (Batch batch : storage.savedBatches) {
				clears.addAll(batch.clears());
				changes.addAll(batch.changes());
			}
		}
		return new Batch(clears, changes);
	}

	static List<String> namesOf(Batch batch) {
		return batch.changes().stream().map(change -> change.name).toList();
	}

	private static void awaitSaved(RecordingStorage storage, String name) throws InterruptedException {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (System.nanoTime() < deadline) {
			synchronized (storage.saved) {
				if (storage.saved.containsKey(name))
					return;
			}
			Thread.sleep(5);
		}
		fail(name + " was never saved to " + storage.getName());
	}

	// ---- loading storages ----

	@Test
	public void loadedVariablesReachTheMapAndSavingStarts() throws InterruptedException {
		RecordingStorage storage = storage("default");
		storage.intervalMillis = 10;
		storage.saved.put("a", value("1"));
		storage.saved.put("l::x", value("2"));

		assertTrue(loadStorages(storage));
		assertEquals("1", globals.getVariable("a"));
		assertEquals("2", globals.getVariable("l::x"));
		assertEquals(List.of(storage), manager.storages());

		manager.set("b", "3");
		awaitSaved(storage, "b"); // the saving thread runs
	}

	@Test
	public void unreadableVariablesAreSkippedButKept() {
		RecordingStorage storage = storage("default");
		SerializedVariable.Value unreadable = new SerializedVariable.Value("removed addon type", new byte[] {1});
		storage.saved.put("a", unreadable);
		storage.saved.put("b", value("1"));

		assertTrue(loadStorages(storage));
		assertNull(globals.getVariable("a"));
		assertEquals("1", globals.getVariable("b"));
		synchronized (storage.saved) {
			assertSame(unreadable, storage.saved.get("a"));
		}
	}

	@Test
	public void variablesNamedLikeListsAreSkipped() {
		RecordingStorage storage = storage("default");
		storage.saved.put("a::*", value("1"));
		storage.saved.put("a::1", value("2"));

		assertTrue(loadStorages(storage));
		assertEquals("2", globals.getVariable("a::1"));
	}

	@Test
	public void storagesLoadInConfigOrder() {
		RecordingStorage first = storage("first", "a::"), second = storage("second");
		assertTrue(loadStorages(first, second));
		assertEquals(List.of(first, second), manager.storages());
	}

	@Test
	public void aStorageThatFailsToLoadIsClosedAndKeepsItsNames() {
		RecordingStorage kills = storage("kills", "kills::"), all = storage("default");
		kills.saved.put("kills::steve", value("1"));
		kills.failLoad = new IOException("corrupt file");

		assertFalse(loadStorages(kills, all));
		assertTrue(kills.loadFailed);
		assertEquals(1, kills.closes.get());
		assertFalse(all.loadFailed);
		assertEquals(List.of(kills, all), manager.storages()); // still decides who owns kills::
		assertEquals("1", globals.getVariable("kills::steve")); // what it loaded before failing is kept

		// its names are only changed in the map, never saved to the storage after it
		manager.set("kills::alex", "2");
		manager.set("kills::*", null);
		manager.setList("kills", new String[] {"bob"}, new Object[] {"3"});
		manager.set("deaths::steve", "4");
		assertEquals("3", globals.getVariable("kills::bob"));
		assertTrue(kills.queue().drain().isEmpty()); // never started saving, so nothing reads its queue
		Batch batch = savedBy(all);
		assertEquals(List.of(), batch.clears());
		assertEquals(List.of("deaths::steve"), namesOf(batch));
	}

	@Test
	public void aFailedStorageInsideAListOnlyKeepsItsOwnPart() {
		RecordingStorage partB = storage("b", "a::b"), all = storage("default");
		partB.failLoad = new IOException("unreachable");

		assertFalse(loadStorages(partB, all));
		manager.setList("a", new String[] {"b1", "c"}, new Object[] {"1", "2"});
		assertTrue(partB.queue().drain().isEmpty());
		Batch batch = savedBy(all);
		assertEquals(List.of("a::"), batch.clears());
		assertEquals(List.of("a::c"), namesOf(batch));
	}

	@Test
	public void noStoragesIsAnError() {
		assertFalse(loadStorages());
		assertTrue(manager.storages().isEmpty());
	}

	@Test
	public void storagesWithoutACatchAllStillLoad() {
		RecordingStorage kills = storage("kills", "kills::");
		assertTrue(loadStorages(kills)); // only a warning
		manager.set("deaths::steve", "1");
		assertTrue(savedBy(kills).isEmpty());
	}

	@Test
	public void typesCantBeRegisteredOnceLoadingStarted() {
		assertTrue(loadStorages(storage("default")));
		assertThrows(IllegalStateException.class,
			() -> manager.register(null, RecordingStorage.class, (addon, type) -> new RecordingStorage(), "recording"));
	}

	@Test
	public void variablesLoadOnlyOnce() throws IOException {
		assertTrue(loadStorages(storage("default")));
		SectionNode databases = databases("");
		assertThrows(IllegalStateException.class, () -> manager.loadStorages(List.of()));
		assertThrows(IllegalStateException.class, () -> manager.load(databases));
	}

	// ---- reading the databases section ----

	@Test
	public void sectionsBecomeStoragesInConfigOrder() throws IOException {
		assertTrue(load("""
			\tfirst:
			\t\ttype: recording
			\t\tprefix: a::
			\tsecond:
			\t\ttype: RECORDING
			"""));
		assertEquals(List.of("first", "second"), storageNames());
		assertEquals(List.of("a::"), manager.storages().get(0).getPrefixes());
		assertEquals(2, created.size());
	}

	@Test
	public void disabledSectionsAreSkipped() throws IOException {
		assertTrue(load("""
			\toff:
			\t\ttype: disabled
			\talso off:
			\t\ttype: none
			\tdefault:
			\t\ttype: recording
			"""));
		assertEquals(List.of("default"), storageNames());
	}

	@Test
	public void anUnknownTypeIsAnErrorButOthersStillLoad() throws IOException {
		assertFalse(load("""
			\ttypo:
			\t\ttype: cvs
			\tdefault:
			\t\ttype: recording
			"""));
		assertEquals(List.of("default"), storageNames());
	}

	@Test
	public void aMissingTypeIsAnError() throws IOException {
		assertFalse(load("""
			\tuntyped:
			\t\tprefix: a::
			\tdefault:
			\t\ttype: recording
			"""));
		assertEquals(List.of("default"), storageNames());
	}

	@Test
	public void aLineThatIsntASectionIsAnError() throws IOException {
		assertFalse(load("""
			\tstray: value
			\tdefault:
			\t\ttype: recording
			"""));
		assertEquals(List.of("default"), storageNames());
	}

	@Test
	public void anInvalidConfigIsAnError() throws IOException {
		assertFalse(load("""
			\tbad:
			\t\ttype: recording
			\t\tpattern: db[0-9]::.*
			\tdefault:
			\t\ttype: recording
			"""));
		assertEquals(List.of("default"), storageNames());
	}

	@Test
	public void namesSharingABackupFolderAreAnError() throws IOException {
		assertFalse(load("""
			\ta b:
			\t\ttype: recording
			\t\tprefix: x::
			\tA_b:
			\t\ttype: recording
			"""));
		assertEquals(List.of("a b"), storageNames());
	}

	@Test
	public void anEmptyDatabasesSectionIsAnError() throws IOException {
		assertFalse(load(""));
		assertTrue(manager.storages().isEmpty());
	}

}
