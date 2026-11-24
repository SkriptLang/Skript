package org.skriptlang.skript.variables.storage.flatfile;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.test.utils.ExpectedExceptions;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.ManagerTests;
import org.skriptlang.skript.variables.storage.VariableStorageManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

/**
 * Tests {@link FlatFileStorage} through a {@link VariableStorageManager}, by saving variables to a file and loading them back.
 * Needs a server, since it logs through Skript.
 */
public class FlatFileStorageTest {

	private static final String HEADER = "# version: 2.9.0\n";

	private Path folder;
	private Path file;
	private final List<VariableStorageManager> managers = new ArrayList<>();

	@Before
	public void setUp() throws IOException {
		folder = Files.createTempDirectory("skript-csv-test");
		file = folder.resolve("variables.csv");
	}

	@After
	public void tearDown() throws IOException {
		managers.forEach(ManagerTests::close);
		try (Stream<Path> paths = Files.walk(folder)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
				Files.delete(path);
		}
	}

	// ---- helpers ----

	/**
	 * Loads the file into a new manager, with values stored as UTF-8 text, and checks it loaded without problems.
	 */
	private VariableStorageManager load(VariableMap globals) throws IOException {
		VariableStorageManager manager = loadExpecting(globals, true);
		assertNotNull(manager);
		return manager;
	}

	private VariableStorageManager loadExpecting(VariableMap globals, boolean successful) throws IOException {
		return loadExpecting(globals, successful, file);
	}

	private VariableStorageManager loadExpecting(VariableMap globals, boolean successful, Path file) throws IOException {
		VariableStorageManager manager = ManagerTests.manager(globals, Runnable::run);
		manager.register(null, FlatFileStorage.class, FlatFileStorage::new, "csv");
		SectionNode databases = ManagerTests.databases(
			"\tdefault:\n\t\ttype: csv\n\t\tfile: " + file.toString().replace('\\', '/') + "\n");
		managers.add(manager);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertEquals(successful, manager.load(databases));
		}
		return manager;
	}

	/**
	 * Closes the manager and loads the file again.
	 */
	private Map<String, Object> reloaded(VariableStorageManager manager) throws IOException {
		ManagerTests.close(manager);
		return loadedFromFile();
	}

	/**
	 * Loads the file into a new manager, and returns what it holds.
	 */
	private Map<String, Object> loadedFromFile() throws IOException {
		VariableMap globals = VariableMap.concurrent();
		load(globals);
		return globals.getAll();
	}

	private static String row(String name, String text) {
		StringBuilder row = new StringBuilder();
		CsvFormat.appendRow(row, name, ManagerTests.value(text));
		return row.toString();
	}

	private static String deletion(String name) {
		StringBuilder row = new StringBuilder();
		CsvFormat.appendRow(row, name, null);
		return row.toString();
	}

	/**
	 * Changes the file's text, as if someone edited it by hand.
	 */
	private void editFile(UnaryOperator<String> edit) throws IOException {
		Files.writeString(file, edit.apply(Files.readString(file)));
	}

	private List<String> rows() throws IOException {
		return rows(Files.readString(file));
	}

	private static List<String> rows(String text) {
		return text.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).map(String::strip).toList();
	}

	private static FlatFileStorage storage(VariableStorageManager manager) {
		return (FlatFileStorage) manager.storages().get(0);
	}

	private List<Path> backups() throws IOException {
		Path backups = folder.resolve("backups");
		if (!Files.exists(backups))
			return List.of();
		try (Stream<Path> files = Files.list(backups)) {
			return files.toList();
		}
	}

	private static String read(Path backup) throws IOException {
		try (InputStream in = Files.newInputStream(backup)) {
			InputStream content = backup.toString().endsWith(".gz") ? new GZIPInputStream(in) : in;
			return new String(content.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	// ---- saving and loading ----

	@Test
	public void variablesSurviveARestart() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("name with spaces, and a comma", "2");
		manager.set("ünïcödé", "3");
		manager.setList("l", new String[] {"x", "y"}, new Object[] {"4", "5"});
		assertEquals(Map.of("a", "1", "name with spaces, and a comma", "2", "ünïcödé", "3", "l::x", "4", "l::y", "5"),
			reloaded(manager));
	}

	@Test
	public void changesAreInTheFileOnceFlushed() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		// read without closing, as after a crash
		assertEquals(List.of(row("a", "1").trim()), rows());
		assertTrue(Files.readString(file).contains(CsvFormat.VERSION_HEADER));
		assertEquals(Map.of("a", "1"), loadedFromFileOf(manager));
	}

	/**
	 * Loads a copy of the file while the manager still has it open, as if the server had crashed.
	 */
	private Map<String, Object> loadedFromFileOf(VariableStorageManager running) throws IOException {
		Path crashed = folder.resolve("crashed.csv");
		Files.copy(file, crashed);
		VariableMap globals = VariableMap.concurrent();
		loadExpecting(globals, true, crashed);
		return globals.getAll();
	}

	@Test
	public void deletesAndClearsSurviveARestart() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("b", "2");
		manager.setList("l", new String[] {"x", "y"}, new Object[] {"3", "4"});
		manager.set("l::z::1", "5");
		assertTrue(manager.flushAll());

		manager.set("a", null);
		manager.set("l::*", null);
		manager.set("l::y", "6");
		assertEquals(Map.of("b", "2", "l::y", "6"), reloaded(manager));
	}

	@Test
	public void aClearedListIsOneRow() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.setList("l", new String[] {"1", "2", "3"}, new Object[] {"a", "b", "c"});
		manager.set("l::4::nested", "d");
		assertTrue(manager.flushAll());
		int before = rows().size();
		manager.set("l::*", null);
		assertTrue(manager.flushAll());
		List<String> rows = rows();
		assertEquals(before + 1, rows.size());
		assertEquals(deletion("l::*").trim(), rows.get(before));
	}

	@Test
	public void namesWithLineBreaksSurviveARestart() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("line\nbreak", "1");
		manager.set("carriage\rreturn", "2");
		manager.set("broken\nlist::x", "3");
		manager.set("broken\nlist::y", "4");
		manager.set("gone\nsoon", "5");
		manager.set("literal \\n", "6");
		assertTrue(manager.flushAll());
		manager.set("broken\nlist::*", null);
		manager.set("broken\nlist::z", "7");
		manager.set("gone\nsoon", null);
		assertTrue(manager.flushAll());
		Map<String, Object> expected = Map.of("line\nbreak", "1", "carriage\rreturn", "2", "broken\nlist::z", "7",
			"literal \\n", "6");
		// read the appended rows, as after a crash
		assertEquals(expected, loadedFromFileOf(manager));
		// and the compacted rows, which compacting reads back too, so it would notice an unreadable one
		assertEquals(expected, reloaded(manager));
		assertTrue("nothing had to be left out, so there's no backup", backups().isEmpty());
	}

	@Test
	public void deletingWhatTheFileDoesntHoldIsDroppedOnShutdown() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("l::*", null);
		manager.set("b", null);
		assertTrue(manager.flushAll());
		assertEquals(Set.of(row("a", "1").trim(), deletion("l::*").trim(), deletion("b").trim()), Set.copyOf(rows()));
		ManagerTests.close(manager);
		assertEquals(List.of(row("a", "1").trim()), rows());
	}

	@Test
	public void rowsAreAppliedInFileOrder() throws IOException {
		Files.writeString(file, HEADER
			+ row("l::a", "old")
			+ row("l::b::c", "old")
			+ row("x", "old")
			+ deletion("l::*")
			+ row("l::b::d", "new")
			+ row("x", "new")
			+ row("y", "old")
			+ deletion("y")
			+ row("quoted, name", "1"));
		assertEquals(Map.of("l::b::d", "new", "x", "new", "quoted, name", "1"), loadedFromFile());
	}

	@Test
	public void valuesTheServerCantReadAreKept() throws IOException {
		Files.writeString(file, HEADER + "a, removed type, 01\n" + row("b", "1"));
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("b", "2");
		assertEquals(Map.of("b", "2"), reloaded(manager));
		assertTrue(rows().stream().anyMatch(row -> Arrays.equals(new String[] {"a", "removed type", "01"}, CsvFormat.splitCSV(row))));
	}

	@Test
	public void aFileWithoutAHeaderGetsOne() throws IOException {
		Files.writeString(file, row("a", "1"));
		assertEquals(Map.of("a", "1"), loadedFromFile());
		assertTrue(Files.readString(file).contains(CsvFormat.VERSION_HEADER));
		assertEquals(List.of(row("a", "1").trim()), rows());
		assertTrue(backups().isEmpty());
	}

	@Test
	public void anInvalidVersionHeaderIsIgnored() throws IOException {
		Files.writeString(file, "# version: not a version\n" + row("a", "1"));
		assertEquals(Map.of("a", "1"), loadedFromFile());
	}

	@Test
	public void filesFromBefore2_1AreRefusedAndLeftAlone() throws IOException {
		String old = "# version: 2.0\n" + "a, string, text\n";
		Files.writeString(file, old);
		VariableMap globals = VariableMap.concurrent();
		loadExpecting(globals, false);
		assertTrue(globals.getAll().isEmpty());
		assertEquals(old, Files.readString(file));
	}

	// ---- damaged files ----

	@Test
	public void damagedRowsAreSkippedAndTheFileIsBackedUp() throws IOException {
		String damaged = HEADER
			+ "a row, with, too many, commas\n"
			+ "bad hex, " + ManagerTests.READABLE + ", XYZ\n"
			+ "odd hex, " + ManagerTests.READABLE + ", ABC\n"
			+ row("l::*", "a list holding a value")
			+ row("kept", "1");
		Files.writeString(file, damaged);
		assertEquals(Map.of("kept", "1"), loadedFromFile());
		assertEquals(List.of(row("kept", "1").trim()), rows());

		List<Path> backups = backups();
		assertEquals(1, backups.size());
		assertEquals(damaged, read(backups.get(0)));
	}

	@Test
	public void aCutOffLastRowIsDiscarded() throws IOException {
		String full = row("a", "1");
		String cutOff = row("a", "2");
		Files.writeString(file, HEADER + full + cutOff.substring(0, cutOff.length() - 3)); // still valid hex
		assertEquals(Map.of("a", "1"), loadedFromFile());
		assertTrue(Files.readString(file).endsWith("\n"));
		assertEquals(List.of(full.trim()), rows());
		assertEquals(1, backups().size());
	}

	@Test
	public void aCutOffDeletionDoesntDeleteAnything() throws IOException {
		Files.writeString(file, HEADER + row("a", "1") + "a, null,");
		assertEquals(Map.of("a", "1"), loadedFromFile());
	}

	@Test
	public void aCutOffMultiByteCharacterDoesntStopLoading() throws IOException {
		byte[] start = (HEADER + row("a", "1")).getBytes(StandardCharsets.UTF_8);
		byte[] cut = "ü".getBytes(StandardCharsets.UTF_8);
		byte[] content = new byte[start.length + 1];
		System.arraycopy(start, 0, content, 0, start.length);
		content[start.length] = cut[0];
		Files.write(file, content);
		assertEquals(Map.of("a", "1"), loadedFromFile());
	}

	// ---- rewriting ----

	@Test
	public void theFileIsCompactedOnceTheAppendedRowsOutnumberTheBase() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		String[] keys = new String[500];
		Object[] values = new Object[keys.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = String.valueOf(i);
			values[i] = "kept " + i;
		}
		manager.setList("kept", keys, values);
		assertTrue(manager.flushAll());
		// setting a list clears it first, which is a row of its own
		assertEquals("fewer than 1000 rows appended", 501, rows().size());

		churn(manager, 1100);
		// loading rewrote the file, so the interval has only just started. but the 1100 sets make 1601 appended rows,
		// more than ten times the empty base and 1000 more: compacted to the 1600 variables.
		// the 1100 deletions after that are fewer than the new base, so they're appended
		assertEquals(1600 + 1100, rows().size());

		churn(manager, 1100);
		// 3300 appended rows, but too soon after the last rewrite, and fewer than ten times the base
		assertEquals(1600 + 1100 + 2200, rows().size());

		ManagerTests.close(manager);
		assertEquals("compacted on shutdown", 500, rows().size());
		assertEquals(500, loadedFromFile().size());
	}

	@Test
	public void theFileIsCompactedOnceTooManyRowsWereAppended() throws IOException {
		// a base bigger than the cap, so only the cap can make it compact
		VariableStorageManager writer = load(VariableMap.concurrent());
		for (int i = 0; i < 300_000; i++)
			writer.set("base::" + i, "1");
		ManagerTests.close(writer);

		VariableStorageManager manager = load(VariableMap.concurrent());
		for (int i = 0; i < 250_000; i++)
			manager.set("base::" + i, "2");
		assertTrue(manager.flushAll());
		// compacted: the base still holds every variable once, and few rows (if any) were appended after
		assertEquals(300_000, rows(compactedPart()).size());
		assertTrue(rows().size() < 300_000 + 250_000);
		assertTrue(rows(compactedPart()).contains(row("base::0", "2").trim()));
	}

	@Test
	public void compactingMergesTheAppendedRowsIntoTheBase() throws IOException {
		Files.writeString(file, HEADER
			+ row("a", "1")
			+ row("b", "1")
			+ row("l::1", "1")
			+ row("l::2", "1")
			+ row("l::x::1", "1")
			+ row("m", "1")
			+ row("quoted, \"name\"", "1")
			+ row("z", "1"));
		VariableStorageManager manager = load(VariableMap.concurrent());
		assertEquals(8, rows().size());

		manager.set("a", null);
		manager.set("b", "2");
		manager.set("c", "new");
		manager.set("quoted, \"name\"", "2");
		manager.set("l::x::*", null);
		assertTrue(manager.flushAll());
		manager.set("l::*", null);
		assertTrue(manager.flushAll());
		manager.set("l::3", "3");
		manager.set("l::x::2", "2");
		manager.set("0 first", "new");
		assertTrue(manager.flushAll());

		Map<String, Object> expected = Map.of("0 first", "new", "b", "2", "c", "new", "l::3", "3", "l::x::2", "2",
			"m", "1", "quoted, \"name\"", "2", "z", "1");
		ManagerTests.close(manager);
		assertEquals(List.of(row("0 first", "new"), row("b", "2"), row("c", "new"), row("l::3", "3"), row("l::x::2", "2"),
			row("m", "1"), row("quoted, \"name\"", "2"), row("z", "1")).stream().map(String::trim).toList(), rows());
		assertEquals(expected, loadedFromFile());
	}

	@Test
	public void anUnsortedFileIsRewrittenSortedOnLoad() throws IOException {
		Files.writeString(file, HEADER + row("b", "1") + row("a", "1"));
		assertEquals(Map.of("a", "1", "b", "1"), loadedFromFile());
		assertEquals(List.of(row("a", "1").trim(), row("b", "1").trim()), rows());
		assertTrue(backups().isEmpty());
	}

	@Test
	public void aCrashedFileIsLoadedWithoutBeingRewritten() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("b", "1");
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		manager.set("b", "2");
		manager.set("a", null);
		assertTrue(manager.flushAll());
		String before = Files.readString(file);
		assertEquals(Map.of("b", "2"), loadedFromFileOf(manager));
		assertEquals(before, Files.readString(folder.resolve("crashed.csv")));
	}

	// ---- the end of the compacted rows ----

	/**
	 * Writes a compacted file holding the given variables, by loading an empty file, setting them, and shutting down.
	 */
	private void compactedFile(Map<String, String> variables) throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		variables.forEach(manager::set);
		ManagerTests.close(manager);
		managers.remove(manager);
	}

	/**
	 * @return the offset the header gives for the end of the compacted rows
	 */
	private long compactedEnd() throws IOException {
		String header = Files.readString(file).lines()
			.filter(line -> line.startsWith(CsvFormat.COMPACTED_HEADER))
			.findFirst().orElseThrow();
		return Long.parseLong(header.substring(CsvFormat.COMPACTED_HEADER.length()).trim());
	}

	/**
	 * @return the file up to the end of the compacted rows
	 */
	private String compactedPart() throws IOException {
		byte[] bytes = Files.readAllBytes(file);
		return new String(bytes, 0, (int) compactedEnd(), StandardCharsets.UTF_8);
	}

	@Test
	public void theHeaderSaysWhereTheCompactedRowsEnd() throws IOException {
		compactedFile(Map.of("a", "1", "ünïcödé", "2"));
		assertEquals(Files.size(file), compactedEnd());
		assertTrue(compactedPart().endsWith("\n" + CsvFormat.COMPACTED_END + "\n"));

		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("b", "3");
		assertTrue(manager.flushAll());
		assertTrue(compactedEnd() < Files.size(file));
		assertEquals(List.of(row("a", "1").trim(), row("ünïcödé", "2").trim()), rows(compactedPart()));
	}

	@Test
	public void aStreamedLoadAppliesTheAppendedRows() throws IOException {
		compactedFile(Map.of("a", "1", "b", "1", "l::1", "1", "l::x::1", "1", "m", "1", "z", "1"));
		String appended = deletion("a")
			+ row("b", "2")
			+ row("l::x::2", "old")
			+ deletion("l::*")
			+ row("l::2", "2")
			+ row("0 first", "new")
			+ row("zz", "new")
			+ deletion("zz")
			+ deletion("missing");
		Files.writeString(file, appended, StandardOpenOption.APPEND);
		String before = Files.readString(file);
		assertEquals(Map.of("b", "2", "l::2", "2", "0 first", "new", "m", "1", "z", "1"), loadedFromFile());
		assertEquals("the file is kept as it is", before, Files.readString(file));
		assertTrue(backups().isEmpty());
	}

	@Test
	public void deletingACompactedRowByHandReadsTheWholeFile() throws IOException {
		compactedFile(Map.of("a", "1", "b", "2", "c", "3"));
		editFile(text -> text.replace(row("b", "2"), ""));
		assertEquals(Map.of("a", "1", "c", "3"), loadedFromFile());
		assertTrue(backups().isEmpty());
		// rewritten, so the header is right again
		assertEquals(Files.size(file), compactedEnd());
		assertEquals(List.of(row("a", "1").trim(), row("c", "3").trim()), rows());
	}

	@Test
	public void deletingAnAppendedRowByHandIsFine() throws IOException {
		compactedFile(Map.of("a", "1"));
		Files.writeString(file, row("b", "1") + row("c", "1"), StandardOpenOption.APPEND);
		editFile(text -> text.replace(row("b", "1"), ""));
		String before = Files.readString(file);
		assertEquals(Map.of("a", "1", "c", "1"), loadedFromFile());
		assertEquals(before, Files.readString(file));
	}

	@Test
	public void damagedAppendedRowsAreSkippedAndTheFileIsBackedUp() throws IOException {
		compactedFile(Map.of("a", "1", "b", "1"));
		String cutOff = row("a", "3");
		Files.writeString(file, "bad hex, " + ManagerTests.READABLE + ", XYZ\n" + row("b", "2") + cutOff.substring(0, cutOff.length() - 3),
			StandardOpenOption.APPEND);
		String damaged = Files.readString(file);
		assertEquals(Map.of("a", "1", "b", "2"), loadedFromFile());
		assertEquals(List.of(row("a", "1").trim(), row("b", "2").trim()), rows());
		assertEquals(Files.size(file), compactedEnd());
		List<Path> backups = backups();
		assertEquals(1, backups.size());
		assertEquals(damaged, read(backups.get(0)));
	}

	@Test
	public void damagedCompactedRowsAreSkippedAndTheFileIsBackedUp() throws IOException {
		compactedFile(Map.of("a", "1", "b", "2", "c", "3"));
		// the same length, so the end of the compacted rows is still where the header says
		String bad = row("b", "2");
		editFile(text -> text.replace(bad, bad.substring(0, bad.length() - 2) + "Z\n"));
		Files.writeString(file, row("c", "4"), StandardOpenOption.APPEND);
		assertEquals(Map.of("a", "1", "c", "4"), loadedFromFile());
		assertEquals(List.of(row("a", "1").trim(), row("c", "4").trim()), rows());
		assertEquals(1, backups().size());
	}

	@Test
	public void compactedRowsOutOfOrderAreDiscarded() throws IOException {
		compactedFile(Map.of("a", "1", "c", "3"));
		// swapped, keeping the length
		String text = Files.readString(file);
		Files.writeString(file, text.replace(row("a", "1") + row("c", "3"), row("c", "3") + row("a", "1")));
		assertEquals(Map.of("c", "3"), loadedFromFile());
		assertEquals(List.of(row("c", "3").trim()), rows());
		assertEquals(1, backups().size());
	}

	// ---- something else changing the file while running ----

	@Test
	public void aRowDeletedByHandWhileRunningIsSavedAgain() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("b", "2");
		assertTrue(manager.flushAll());
		editFile(text -> text.replace(row("a", "1"), ""));
		manager.set("c", "3");
		assertTrue(manager.flushAll());
		// read again as it is, later changes are still saved after it, and the resave puts a back
		assertEquals(Map.of("a", "1", "b", "2", "c", "3"), reloaded(manager));
		assertTrue("nothing had to be left out, so there's no backup", backups().isEmpty());
	}

	@Test
	public void rowsAddedByHandWhileRunningAreKept() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		Files.writeString(file, row("b", "2"), StandardOpenOption.APPEND);
		manager.set("c", "3");
		assertTrue(manager.flushAll());
		assertEquals(Map.of("a", "1", "b", "2", "c", "3"), loadedFromFileOf(manager));
	}

	@Test
	public void anOlderCopyWrittenOverTheFileWhileRunningIsReadAgain() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		String older = Files.readString(file);
		manager.set("b", "2");
		assertTrue(manager.flushAll());
		Files.writeString(file, older);
		manager.set("c", "3");
		assertTrue(manager.flushAll());
		// b was written over, and the resave puts it back
		assertEquals(Map.of("a", "1", "b", "2", "c", "3"), reloaded(manager));
	}

	@Test
	public void aCompactedRowDamagedWhileRunningIsReportedWhenCompacting() throws IOException {
		compactedFile(Map.of("a", "1", "b", "2", "c", "3"));
		VariableStorageManager manager = load(VariableMap.concurrent());
		// the same length, so only reading the rows back notices it
		String bad = row("b", "2");
		editFile(text -> text.replace(bad, bad.substring(0, bad.length() - 2) + "Z\n"));
		manager.set("d", "4");
		assertTrue(manager.flushAll());
		String damaged = Files.readString(file);
		ManagerTests.close(manager); // compacts
		assertEquals(Map.of("a", "1", "c", "3", "d", "4"), loadedFromFile());
		List<Path> backups = backups();
		assertEquals("the row was left out, so the file was backed up first", 1, backups.size());
		// closing refreshes before it compacts, which may save d again, so the backup can have more rows after these
		assertTrue(read(backups.get(0)).startsWith(damaged));
	}

	@Test
	public void aFileDeletedWhileRunningIsStartedAgain() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		Files.delete(file);
		manager.set("b", "2");
		assertTrue(manager.flushAll());
		assertEquals(Map.of("a", "1", "b", "2"), reloaded(manager));
	}

	@Test
	public void aFileDeletedWhileIdleIsSavedAgainOnShutdown() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("b", "2");
		assertTrue(manager.flushAll());
		Files.delete(file);
		// nothing is saved before shutting down, so only the check while closing notices
		assertEquals(Map.of("a", "1", "b", "2"), reloaded(manager));
	}

	/**
	 * Sets and then deletes some variables, flushing after each step.
	 */
	private static void churn(VariableStorageManager manager, int count) {
		for (int i = 0; i < count; i++)
			manager.set("churn::" + i, "x");
		assertTrue(manager.flushAll());
		for (int i = 0; i < count; i++)
			manager.set("churn::" + i, null);
		assertTrue(manager.flushAll());
	}

	/**
	 * Closes the channel the storage appends with, so its next append fails, as if a write had failed.
	 */
	private static void breakAppender(VariableStorageManager manager) throws ReflectiveOperationException, IOException {
		Field file = FlatFileStorage.class.getDeclaredField("file");
		file.setAccessible(true);
		Field appender = VariableFile.class.getDeclaredField("appender");
		appender.setAccessible(true);
		((FileChannel) appender.get(file.get(storage(manager)))).close();
	}

	@Test
	public void aFailedAppendIsSavedAgainByCompacting() throws Exception {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		breakAppender(manager);
		manager.set("b", "2");
		try (BlockingLogHandler ignored = new BlockingLogHandler().start();
			 ExpectedExceptions exceptions = ExpectedExceptions.start()) {
			assertFalse("the append fails", manager.flushAll());
			exceptions.assertReported("Could not save 1 variable changes to 'default'");
			assertTrue("the retry compacts the file instead", manager.flushAll());
		}
		assertEquals(List.of(row("a", "1").trim(), row("b", "2").trim()), rows());
		// and it's open for appending again
		manager.set("c", "3");
		assertTrue(manager.flushAll());
		assertEquals(Map.of("a", "1", "b", "2", "c", "3"), loadedFromFileOf(manager));
	}

	@Test
	public void shutdownCompactsTheFile() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		for (int i = 0; i < 3; i++) {
			manager.set("a", String.valueOf(i));
			assertTrue(manager.flushAll());
		}
		assertEquals(3, rows().size());
		ManagerTests.close(manager);
		assertEquals(List.of(row("a", "2").trim()), rows());
		assertFalse(Files.exists(folder.resolve("variables.csv.temp")));
	}

	@Test
	public void aFileNothingWasAppendedToIsntRewrittenOnShutdown() throws IOException {
		Files.writeString(file, HEADER + row("a", "1"));
		VariableStorageManager manager = load(VariableMap.concurrent());
		// a rewrite replaces the file, giving it a new modification time. a marker written into the file would count
		// as an outside change, which resaves and so rewrites it
		FileTime marker = FileTime.fromMillis(0);
		Files.setLastModifiedTime(file, marker);
		ManagerTests.close(manager);
		assertEquals(marker, Files.getLastModifiedTime(file));
	}

	@Test
	public void aBackupHoldsOnlyTheCurrentVariables() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("b", "2");
		assertTrue(manager.flushAll());
		manager.set("a", "3");
		manager.set("b", null);
		assertTrue(manager.flushAll());

		ByteArrayOutputStream backup = new ByteArrayOutputStream();
		storage(manager).writeBackup(backup);
		String text = backup.toString(StandardCharsets.UTF_8);
		assertTrue(text.contains(CsvFormat.VERSION_HEADER));
		assertFalse("a backup is read whole", text.contains(CsvFormat.COMPACTED_HEADER));
		assertEquals(List.of(row("a", "3").trim()), rows(text));
	}

}
