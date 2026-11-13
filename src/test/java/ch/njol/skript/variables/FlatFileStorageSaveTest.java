package ch.njol.skript.variables;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.VariableStorageManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

/**
 * Tests {@link FlatFileStorage} through a {@link VariableStorageManager}, by saving variables to a file and loading them back.
 * Needs a server, since it logs through Skript.
 */
public class FlatFileStorageSaveTest {

	private static final String TEXT = "text";

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
		managers.forEach(this::close);
		try (Stream<Path> paths = Files.walk(folder)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
				Files.delete(path);
		}
	}

	/**
	 * Loads the file into a new manager, with values stored as UTF-8 text.
	 */
	private VariableStorageManager load(VariableMap globals) throws IOException {
		VariableStorageManager manager = new VariableStorageManager(globals,
			value -> value == null ? null : new SerializedVariable.Value(TEXT, String.valueOf(value).getBytes(StandardCharsets.UTF_8)),
			(type, data) -> type.equals(TEXT) ? new String(data, StandardCharsets.UTF_8) : null,
			Runnable::run);
		manager.register(null, FlatFileStorage.class, FlatFileStorage::new, "csv");
		String config = "databases:\n\tdefault:\n\t\ttype: csv\n\t\tfile: " + file.toString().replace('\\', '/') + "\n";
		SectionNode databases = (SectionNode) new Config(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)),
			"test.sk", false, true, ":").getMainNode().get("databases");
		managers.add(manager);
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			assertTrue(manager.load(databases));
		}
		return manager;
	}

	private void close(VariableStorageManager manager) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			manager.close();
		}
	}

	/**
	 * Closes the manager and loads the file again.
	 */
	private Map<String, Object> reloaded(VariableStorageManager manager) throws IOException {
		close(manager);
		VariableMap globals = VariableMap.concurrent();
		load(globals);
		return globals.getAll();
	}

	private List<String> variableLines() throws IOException {
		return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
			.filter(line -> !line.isBlank() && !line.startsWith("#"))
			.toList();
	}

	private static String hex(String text) {
		return FlatFileStorage.encode(text.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void variablesSurviveARestart() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		manager.set("name with spaces, and a comma", "2");
		manager.setList("l", new String[] {"x", "y"}, new Object[] {"3", "4"});
		assertEquals(Map.of("a", "1", "name with spaces, and a comma", "2", "l::x", "3", "l::y", "4"), reloaded(manager));
	}

	@Test
	public void changesAreInTheFileOnceFlushed() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("a", "1");
		assertTrue(manager.flushAll());
		assertEquals(List.of("a, " + TEXT + ", " + hex("1")), variableLines());
		assertTrue(Files.readString(file).contains("# version:"));
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
	public void readsFilesFromOlderVersions() throws IOException {
		Files.writeString(file, String.join("\n",
			"# === Skript's variable storage ===",
			"# version: 2.9.0",
			"",
			"a, " + TEXT + ", " + hex("old"),
			"b, " + TEXT + ", " + hex("2"),
			"\"quoted, name\", " + TEXT + ", " + hex("3"),
			"a, " + TEXT + ", " + hex("new"),
			"b, null, ",
			""));
		VariableMap globals = VariableMap.concurrent();
		load(globals);
		assertEquals(Map.of("a", "new", "quoted, name", "3"), globals.getAll());
	}

	@Test
	public void unreadableValuesAreKept() throws IOException {
		Files.writeString(file, "# version: 2.9.0\na, removed type, 01\nb, " + TEXT + ", " + hex("1") + "\n");
		VariableStorageManager manager = load(VariableMap.concurrent());
		manager.set("b", "2");
		assertEquals(Map.of("b", "2"), reloaded(manager));
		assertTrue(variableLines().stream().anyMatch(line -> line.startsWith("a, ")));
	}

	@Test
	public void invalidLinesAreBackedUpAndDropped() throws IOException {
		String broken = "a line, with, too many, commas";
		Files.writeString(file, "# version: 2.9.0\n" + broken + "\nb, " + TEXT + ", " + hex("1") + "\n");
		VariableMap globals = VariableMap.concurrent();
		load(globals);
		assertEquals(Map.of("b", "1"), globals.getAll());
		assertFalse(variableLines().contains(broken));

		try (Stream<Path> backups = Files.list(folder.resolve("backups"))) {
			List<Path> files = backups.toList();
			assertEquals(1, files.size());
			Path backup = files.get(0);
			try (InputStream in = Files.newInputStream(backup)) {
				InputStream content = backup.toString().endsWith(".gz") ? new GZIPInputStream(in) : in;
				assertTrue(new String(content.readAllBytes(), StandardCharsets.UTF_8).contains(broken));
			}
		}
	}

	@Test
	public void theFileIsRewrittenOnceMostOfItIsOutdated() throws IOException {
		VariableStorageManager manager = load(VariableMap.concurrent());
		String[] keys = new String[600];
		Object[] values = new Object[keys.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = String.valueOf(i);
			values[i] = "value " + i;
		}
		manager.setList("l", keys, values);
		manager.set("kept", "1");
		assertTrue(manager.flushAll());
		assertEquals(601, variableLines().size());

		// 600 deletions on top of 601 lines leave 1200 outdated lines, which is too many
		manager.set("l::*", null);
		assertTrue(manager.flushAll());
		assertEquals(List.of("kept, " + TEXT + ", " + hex("1")), variableLines());

		manager.set("after", "2");
		assertEquals(Map.of("kept", "1", "after", "2"), reloaded(manager));
	}

	@Test
	public void aFileWithoutAHeaderGetsOne() throws IOException {
		Files.writeString(file, "a, " + TEXT + ", " + hex("1") + "\n");
		VariableMap globals = VariableMap.concurrent();
		load(globals);
		assertEquals(Map.of("a", "1"), globals.getAll());
		assertTrue(Files.readString(file).contains("# version:"));
		assertEquals(List.of("a, " + TEXT + ", " + hex("1")), variableLines());
	}

}
