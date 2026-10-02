package ch.njol.skript.variables;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MySQLJournalTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void restartPreservesNamesTypesLargeValuesAndDeletes() throws IOException {
		Path path = folder.getRoot().toPath().resolve("pending.bin");
		Map<String, SerializedVariable> pending = new LinkedHashMap<>();
		byte[] large = new byte[100_000];
		for (int i = 0; i < large.length; i++)
			large[i] = (byte) i;
		for (String name : new String[]{"foo", "foo::bar", "list::1", "list::2::child", "玩家::😀", "x".repeat(100_000)})
			pending.put(name, new SerializedVariable(name, new SerializedVariable.Value("custom", large)));
		pending.put("deleted::1", new SerializedVariable("deleted::1", null));
		new MySQLJournal(path, "server/db/table").write(pending);
		Map<String, SerializedVariable> loaded = new MySQLJournal(path, "server/db/table").read();
		assertEquals(pending.keySet(), loaded.keySet());
		for (SerializedVariable variable : loaded.values()) {
			if (variable.name.equals("deleted::1")) {
				assertNull(variable.value);
			} else {
				assertEquals("custom", variable.value.type);
				assertArrayEquals(large, variable.value.data);
			}
		}
	}

	@Test
	public void missingJournalDoesNotCreateFiles() throws IOException {
		Path path = folder.getRoot().toPath().resolve("absent.bin");
		assertTrue(new MySQLJournal(path, "target").read().isEmpty());
		assertFalse(Files.exists(path));
	}

	@Test
	public void pendingDataCannotBeReplayedIntoDifferentDatabase() throws IOException {
		Path path = folder.getRoot().toPath().resolve("pending.bin");
		new MySQLJournal(path, "original").write(Map.of("x", new SerializedVariable("x", null)));
		byte[] original = Files.readAllBytes(path);
		assertThrows(IOException.class, () -> new MySQLJournal(path, "different").read());
		assertArrayEquals(original, Files.readAllBytes(path));
	}

	@Test
	public void acknowledgedSnapshotAllowsNewTarget() throws IOException {
		Path path = folder.getRoot().toPath().resolve("pending.bin");
		MySQLJournal journal = new MySQLJournal(path, "original");
		journal.write(Map.of("x", new SerializedVariable("x", null)));
		journal.write(Map.of());
		assertTrue(new MySQLJournal(path, "different").read().isEmpty());
	}

	@Test
	public void truncatedSnapshotIsRejectedWithoutModification() throws IOException {
		Path path = folder.getRoot().toPath().resolve("pending.bin");
		new MySQLJournal(path, "target").write(Map.of("x", new SerializedVariable("x", null)));
		byte[] bytes = Files.readAllBytes(path);
		Files.write(path, java.util.Arrays.copyOf(bytes, bytes.length - 1));
		assertThrows(IOException.class, () -> new MySQLJournal(path, "target").read());
		assertEquals(bytes.length - 1, Files.size(path));
	}

	@Test
	public void corruptedSnapshotIsRejected() throws IOException {
		Path path = folder.getRoot().toPath().resolve("pending.bin");
		MySQLJournal journal = new MySQLJournal(path, "target");
		journal.write(Map.of("x", new SerializedVariable("x", new SerializedVariable.Value("number", new byte[]{7}))));
		byte[] bytes = Files.readAllBytes(path);
		bytes[bytes.length - 9] ^= 1;
		Files.write(path, bytes);
		assertThrows(IOException.class, journal::read);
	}

	@Test
	public void interruptedReplacementLeavesPreviousSnapshotUsable() throws IOException {
		Path path = folder.getRoot().toPath().resolve("pending.bin");
		MySQLJournal journal = new MySQLJournal(path, "target");
		journal.write(Map.of("old", new SerializedVariable("old", null)));
		Files.write(path.resolveSibling("pending.bin.tmp"), new byte[]{1, 2, 3});
		assertTrue(journal.read().containsKey("old"));
	}
}
