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
	public void snapshotUsesAssignmentBytesAndFlushesOffMainThread() throws Exception {
		Path file = folder.getRoot().toPath().resolve("cached-bytes.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			var item = new ch.njol.skript.aliases.ItemType(org.bukkit.Material.STONE);
			item.setAmount(1);
			storage.setVariable("item", item);
			assertTrue(storage.flush());
			String original = Files.readString(file);
			item.setAmount(5);
			// The test runs on the main thread: a worker needing main-thread serialization would time out.
			assertTrue(java.util.concurrent.CompletableFuture.supplyAsync(storage::flush)
				.get(5, java.util.concurrent.TimeUnit.SECONDS));
			assertEquals(original, Files.readString(file));
			storage.setVariable("item", item);
			assertTrue(storage.flush());
			assertNotEquals(original, Files.readString(file));
		} finally {
			storage.close();
		}
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertEquals(5, ((ch.njol.skript.aliases.ItemType) reopened.getVariable("item")).getAmount());
		} finally {
			reopened.close();
		}
	}

	@Test
	public void backupsRequireChangesAndZeroRetentionRemovesExistingBackups() throws Exception {
		Path directory = folder.newFolder("retention-zero").toPath();
		long before = System.currentTimeMillis();
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv") {
			@Override
			protected Path getBackupDirectory() { return directory; }
		};
		assertTrue(storage.lastBackup >= before);
		assertTrue(storage.lastBackup <= System.currentTimeMillis());
		assertTrue(storage.loadConfig(configuration(folder.getRoot().toPath().resolve("no-change.csv"))));
		try {
			storage.backupIntervalMillis = 1;
			storage.lastBackup = 0;
			assertFalse(storage.isBackupDue());
			storage.setVariable("entry", "value");
			assertTrue(storage.isBackupDue());
			storage.backupIfDue();
			storage.lastBackup = 0;
			assertFalse(storage.isBackupDue());
			storage.backupIfDue();
			try (var files = Files.list(directory)) {
				assertEquals(1, files.count());
			}
			Files.writeString(directory.resolve("1-00000000-0000-0000-0000-000000000000.csv"), "old");
			Files.writeString(directory.resolve("keep.txt"), "unrelated");
			storage.backupsToKeep = 0;
			storage.backupIntervalMillis = 0;
			storage.backupIfDue();
			try (var files = Files.list(directory)) {
				assertEquals(java.util.List.of(directory.resolve("keep.txt")), files.toList());
			}
		} finally {
			storage.close();
		}
	}

	@Test
	public void automaticSaveRequiresThresholdEvenWhenBackupIsDue() throws Exception {
		Path file = folder.getRoot().toPath().resolve("threshold.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("entry", "small batch");
			String before = Files.readString(file);
			var request = FlatFileStorage.class.getDeclaredMethod("saveAsync");
			request.setAccessible(true);
			var executorField = FlatFileStorage.class.getDeclaredField("saveExecutor");
			executorField.setAccessible(true);
			var executor = (java.util.concurrent.ExecutorService) executorField.get(storage);
			request.invoke(storage);
			executor.submit(() -> {}).get(5, java.util.concurrent.TimeUnit.SECONDS);
			assertEquals(before, Files.readString(file));
			storage.backupIntervalMillis = 1;
			storage.lastBackup = 0;
			request.invoke(storage);
			executor.submit(() -> {}).get(5, java.util.concurrent.TimeUnit.SECONDS);
			assertTrue(storage.lastBackup > 0);
			assertEquals(before, Files.readString(file));
			storage.requiredChangesForResave = 1;
			request.invoke(storage);
			executor.submit(() -> {}).get(5, java.util.concurrent.TimeUnit.SECONDS);
			assertNotEquals(before, Files.readString(file));
		} finally {
			storage.close();
		}
	}

	@Test
	public void flatFileBackupIsGzipAndPreservesSnapshotBytes() throws Exception {
		Path file = folder.getRoot().toPath().resolve("compressed.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv") {
			@Override
			protected String backupExtension() { return ".csv.gz"; }
		};
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("entry", "hello");
			assertTrue(storage.flush());
			Path backup = folder.getRoot().toPath().resolve("snapshot.csv.gz");
			storage.writeBackup(backup);
			assertEquals(".csv.gz", storage.backupExtension());
			try (var gzip = new java.util.zip.GZIPInputStream(Files.newInputStream(backup))) {
				assertArrayEquals(Files.readAllBytes(file), gzip.readAllBytes());
			}
		} finally {
			storage.close();
		}
	}

	@Test
	public void uncheckedSaveFailureRestoresPendingCountIncludingNewChanges() throws Exception {
		Path file = folder.getRoot().toPath().resolve("unchecked-failure.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv") {
			@Override
			protected void writeBackup(Path target) {
				setVariable("during-save", "new");
				throw new IllegalStateException("Injected unchecked save failure");
			}
		};
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("entry", "pending");
			storage.backupIntervalMillis = 1;
			storage.lastBackup = 0;
			assertThrows(IllegalStateException.class, storage::flush);
			var field = FlatFileStorage.class.getDeclaredField("changes");
			field.setAccessible(true);
			assertEquals(2, ((java.util.concurrent.atomic.AtomicInteger) field.get(storage)).get());
			storage.backupIntervalMillis = 0;
			assertTrue(storage.flush());
			assertEquals(0, ((java.util.concurrent.atomic.AtomicInteger) field.get(storage)).get());
		} finally {
			storage.backupIntervalMillis = 0;
			storage.close();
		}
	}

	@Test
	public void failedAppendRecoversThroughSnapshotAndResumesJournaling() throws Exception {
		Path file = folder.getRoot().toPath().resolve("append-failure.csv");
		Path journal = Path.of(file + ".journal");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			Files.createDirectory(journal);
			storage.setVariable("entry", "retained");
			assertEquals("retained", storage.getVariable("entry"));
			Files.delete(journal);
			assertTrue(storage.flush());
			storage.setVariable("entry", "journal works again");
			assertEquals(1, new CsvJournal(journal).read().size());
		} finally {
			storage.close();
		}
	}

	@Test
	public void journalRecoversWithoutSnapshotOrShutdown() throws Exception {
		Path file = folder.getRoot().toPath().resolve("journal.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		Path recovered = folder.getRoot().toPath().resolve("recovered.csv");
		try {
			storage.setVariable("entry", "first");
			storage.setVariable("entry", "last");
			storage.setVariable("list::1", "one");
			storage.setVariable("list::2", "two");
			storage.setVariable("list::*", null);
			storage.setVariable("list::2", "restored");
			storage.setVariable("deleted", "old");
			storage.setVariable("deleted", null);
			// Copy only on-disk data before close() or flush() can save a snapshot.
			Files.copy(file, recovered);
			Files.copy(Path.of(file + ".journal"), Path.of(recovered + ".journal"));
		} finally {
			storage.close();
		}
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(recovered)));
		try {
			assertEquals("last", reopened.getVariable("entry"));
			assertNull(reopened.getVariable("list::1"));
			assertEquals("restored", reopened.getVariable("list::2"));
			assertNull(reopened.getVariable("deleted"));
			assertTrue(reopened.flush());
			assertEquals("", Files.readString(Path.of(recovered + ".journal")));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void checkpointSkipsOldJournalAndRecoversIncompleteTail() throws Exception {
		Path file = folder.getRoot().toPath().resolve("checkpoint.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		String oldJournal;
		try {
			storage.setVariable("entry", "old");
			oldJournal = Files.readString(Path.of(file + ".journal"));
			storage.setVariable("entry", "new");
			assertTrue(storage.flush());
		} finally {
			storage.close();
		}
		// Simulate a crash after snapshot replacement but before journal compaction.
		Files.writeString(Path.of(file + ".journal"), oldJournal + "3\tpartial");
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertEquals("new", reopened.getVariable("entry"));
			assertTrue(Files.exists(Path.of(file + ".journal.incomplete")));
			reopened.setVariable("entry", "latest");
			assertTrue(reopened.flush());
		} finally {
			reopened.close();
		}
	}

	@Test
	public void compactionKeepsChangesMadeAfterSnapshotCapture() throws Exception {
		Path file = folder.getRoot().toPath().resolve("during-save.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv") {
			@Override
			protected void writeBackup(Path target) throws java.io.IOException {
				// Backup occurs after snapshot serialization and before publication.
				setVariable("entry", "newer");
				super.writeBackup(target);
			}
		};
		assertTrue(storage.loadConfig(configuration(file)));
		Path recovered = folder.getRoot().toPath().resolve("during-save-recovered.csv");
		try {
			storage.setVariable("entry", "snapshot");
			storage.backupIntervalMillis = 1;
			storage.lastBackup = 0;
			assertTrue(storage.flush());
			Files.copy(file, recovered);
			Files.copy(Path.of(file + ".journal"), Path.of(recovered + ".journal"));
		} finally {
			storage.backupIntervalMillis = 0;
			storage.close();
		}
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(recovered)));
		try {
			assertEquals("newer", reopened.getVariable("entry"));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void finalSaveWaitsForLockHeldLongerThanFiveSeconds() throws Exception {
		Path file = folder.getRoot().toPath().resolve("slow-shutdown.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		storage.setVariable("entry", "final");
		var field = FlatFileStorage.class.getDeclaredField("saveLock");
		field.setAccessible(true);
		var lock = (java.util.concurrent.locks.ReentrantLock) field.get(storage);
		var acquired = new java.util.concurrent.CountDownLatch(1);
		Thread writer = new Thread(() -> {
			lock.lock();
			try {
				acquired.countDown();
				Thread.sleep(6000);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
			} finally {
				lock.unlock();
			}
		});
		writer.start();
		try {
			assertTrue(acquired.await(5, java.util.concurrent.TimeUnit.SECONDS));
			storage.close();
		} finally {
			writer.interrupt();
			writer.join();
			storage.close();
		}
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertEquals("final", reopened.getVariable("entry"));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void backupFailureDoesNotPreventFlushOrFinalSave() throws Exception {
		Path file = folder.getRoot().toPath().resolve("backup-failure.csv");
		Path backupDirectory = folder.getRoot().toPath().resolve("failed-backups");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv") {
			@Override
			protected Path getBackupDirectory() { return backupDirectory; }
			@Override
			protected void writeBackup(Path target) throws java.io.IOException {
				throw new java.io.IOException("Injected backup failure");
			}
		};
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.backupIntervalMillis = 1;
			storage.lastBackup = 0;
			storage.setVariable("entry", "flushed");
			assertTrue(storage.flush());
			assertTrue(Files.readString(file).contains(FlatFileStorage.encode(
				ch.njol.skript.registrations.Classes.serialize("flushed").data())));
			storage.setVariable("entry", "final");
		} finally {
			storage.close();
		}
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertEquals("final", reopened.getVariable("entry"));
		} finally {
			reopened.close();
		}
	}

	@Test
	public void automaticSaveCooldownRetainsChangesAndAllowsExplicitFlush() throws Exception {
		Path file = folder.getRoot().toPath().resolve("cooldown.csv");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			storage.setVariable("entry", "before");
			assertTrue(storage.flush());
			String saved = Files.readString(file);
			storage.requiredChangesForResave = 1;
			storage.setVariable("entry", "after");
			var saving = FlatFileStorage.class.getDeclaredField("isSaving");
			saving.setAccessible(true);
			assertFalse(((java.util.concurrent.atomic.AtomicBoolean) saving.get(storage)).get());
			var changes = FlatFileStorage.class.getDeclaredField("changes");
			changes.setAccessible(true);
			assertEquals(1, ((java.util.concurrent.atomic.AtomicInteger) changes.get(storage)).get());
			assertEquals(saved, Files.readString(file));
			assertTrue(storage.flush());
			assertNotEquals(saved, Files.readString(file));
		} finally {
			storage.close();
		}
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
			storage.lastBackup = 0;
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
	public void legacyDuplicatesAndDeletionsSurviveConversion() throws Exception {
		Path file = folder.getRoot().toPath().resolve("legacy-duplicates.csv");
		Files.writeString(file, "# version: 2.0-beta3\n"
			+ "entry, string, first\nentry, string, last\n"
			+ "deleted, string, old\ndeleted, null, \n"
			+ "restored, null, \nrestored, string, restored\n");
		FlatFileStorage storage = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(storage.loadConfig(configuration(file)));
		try {
			assertEquals("last", storage.getVariable("entry"));
			assertNull(storage.getVariable("deleted"));
			assertEquals("restored", storage.getVariable("restored"));
			assertTrue(storage.flush());
		} finally {
			storage.close();
		}
		FlatFileStorage reopened = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(reopened.loadConfig(configuration(file)));
		try {
			assertEquals("last", reopened.getVariable("entry"));
			assertNull(reopened.getVariable("deleted"));
			assertEquals("restored", reopened.getVariable("restored"));
		} finally {
			reopened.close();
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
