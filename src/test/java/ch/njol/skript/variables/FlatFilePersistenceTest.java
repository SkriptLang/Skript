package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class FlatFilePersistenceTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private SectionNode configuration(Path file) throws Exception {
		Config config = new Config(new ByteArrayInputStream(new byte[0]), "csv-test", false, false, ":");
		SectionNode node = new SectionNode("csv-test", "", config.getMainNode(), 0);
		node.add(new EntryNode("pattern", ".*", node));
		node.add(new EntryNode("file", file.toString(), node));
		node.add(new EntryNode("backup interval", "0", node));
		node.add(new EntryNode("save delay", "1 second", node));
		node.add(new EntryNode("save period", "2 seconds", node));
		node.add(new EntryNode("required changes per save", "1000000", node));
		return node;
	}

	@Test
	public void csvValuesSurviveReopen() throws Exception {
		Path file = folder.getRoot().toPath().resolve("variables.csv");
		FlatFileStorage first = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(first.loadConfig(configuration(file)));
		try {
			assertEquals(20, first.saveTaskDelay);
			assertEquals(40, first.saveTaskPeriod);
			first.setVariable("quoted, name", "value");
			first.setVariable("list::1", 42L);
			assertTrue(first.flush());
		} finally {
			first.close();
		}
		FlatFileStorage second = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(second.loadConfig(configuration(file)));
		try {
			assertEquals("value", second.getVariable("quoted, name"));
			assertEquals(42L, second.getVariable("list::1"));
			second.setVariable("list::*", null);
			assertTrue(second.flush());
		} finally {
			second.close();
		}
	}

	@Test
	public void duplicateRowsUseLastValueAndDeletion() throws Exception {
		Path file = folder.getRoot().toPath().resolve("duplicates.csv");
		String encoded = FlatFileStorage.encode(ch.njol.skript.registrations.Classes.serialize("first").data());
		Files.writeString(file, "entry, string, " + encoded + "\nentry, null, \n");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			assertNull(storage.getVariable("entry"));
		} finally {
			storage.close();
		}
	}

	@Test
	public void rejectsMalformedHexadecimal() {
		assertThrows(IllegalArgumentException.class, () -> FlatFileStorage.decode("F"));
		assertThrows(IllegalArgumentException.class, () -> FlatFileStorage.decode("GG"));
	}

	@Test
	public void invalidSavePeriodDoesNotLoad() throws Exception {
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		SectionNode config = configuration(folder.getRoot().toPath().resolve("invalid.csv"));
		config.set("save period", "0");
		try {
			assertFalse(storage.loadConfig(config));
		} finally {
			storage.close();
		}
	}
	@Test
	public void backupRetentionPreservesUnrelatedFiles() throws Exception {
		Path backupDirectory = folder.getRoot().toPath().resolve("backups");
		class BackedStorage extends FlatFileStorage {
			BackedStorage() { super(Skript.instance(), "csv"); }
			@Override
			protected Path getBackupDirectory() { return backupDirectory; }
		}
		FlatFileStorage storage = new BackedStorage();
		assertTrue(storage.loadConfig(configuration(folder.getRoot().toPath().resolve("backed.csv"))));
		try {
			storage.backupIntervalMillis = 1;
			storage.backupsToKeep = 2;
			Files.createDirectories(backupDirectory);
			Path unrelated = backupDirectory.resolve("keep.txt");
			Files.writeString(unrelated, "untouched");
			for (int i = 0; i < 4; i++) {
				storage.lastBackup = 0;
				storage.setVariable("value", "value " + i);
				assertTrue(storage.flush());
			}
			try (var files = Files.list(backupDirectory)) {
				assertEquals(3, files.count());
			}
			assertEquals("untouched", Files.readString(unrelated));
		} finally {
			storage.close();
		}
	}

	@Test
	public void legacyCsvIsBackedUpBeforeConversion() throws Exception {
		for (String version : new String[]{"2.0-beta3", "2.1"}) {
			Path directory = folder.newFolder().toPath();
			Path file = directory.resolve("legacy.csv");
			boolean legacy = version.equals("2.0-beta3");
			String value = legacy ? "hello" : FlatFileStorage.encode(
				ch.njol.skript.registrations.Classes.serialize("hello").data());
			Files.writeString(file, "# version: " + version + "\nentry, string, " + value + "\n");
			FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
			assertTrue(storage.loadConfig(configuration(file)));
			try {
				assertEquals("hello", storage.getVariable("entry"));
				if (legacy) {
					try (var backups = Files.list(directory.resolve("backups"))) {
						assertEquals(1, backups.count());
					}
					assertTrue(storage.flush());
					assertFalse(Files.readString(file).contains("entry, string, hello"));
				}
			} finally {
				storage.close();
			}
		}
	}

	@Test
	public void failedDuplicateLoadDoesNotReleaseAnotherStoragesFile() throws Exception {
		Path file = folder.getRoot().toPath().resolve("shared.csv");
		FlatFileStorage owner = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(owner.loadConfig(configuration(file)));
		try {
			for (int i = 0; i < 2; i++) {
				FlatFileStorage duplicate = new FlatFileStorage(Skript.instance(), "csv");
				try {
					assertFalse(duplicate.loadConfig(configuration(file)));
				} finally {
					duplicate.close();
				}
			}
			owner.setVariable("entry", "value");
			assertTrue(owner.flush());
		} finally {
			owner.close();
		}
	}

}
