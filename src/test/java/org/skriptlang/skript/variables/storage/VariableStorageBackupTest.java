package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests how {@link VariableStorage}'s saving thread uses {@link StorageBackups}: that it backs up on its own,
 * only when saving is going well, and never from {@link VariableStorage#flush()} or
 * {@link VariableStorage#stopSaving()}. The backups themselves are tested in {@link StorageBackupsTest}.
 */
public class VariableStorageBackupTest {

	private static final long TIMEOUT_MILLIS = 5_000;
	private static final long BACKUP_INTERVAL_MILLIS = 50;

	private RecordingStorage storage;
	private Path folder;

	@Before
	public void setUp() throws IOException {
		folder = Files.createTempDirectory("skript-backup-test");
		storage = new RecordingStorage();
		storage.setNameForTest("test");
		storage.backupFolder = folder;
	}

	@After
	public void tearDown() throws IOException {
		storage.failSaves.set(0);
		storage.failBackups.set(0);
		storage.stopSaving();
		try (Stream<Path> files = Files.walk(folder)) {
			for (Path path : files.sorted((a, b) -> b.compareTo(a)).toList())
				Files.deleteIfExists(path);
		}
	}

	private void set(String name) {
		storage.queue().write(name, new Value("test", new byte[] {1}), () -> {});
	}

	private static void await(String what, BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MILLIS);
		while (!condition.getAsBoolean()) {
			if (System.nanoTime() > deadline)
				fail("timed out waiting until " + what);
			Thread.sleep(5);
		}
	}

	private List<String> files() {
		if (!Files.isDirectory(folder))
			return List.of();
		try (Stream<Path> files = Files.list(folder)) {
			return files.map(path -> path.getFileName().toString())
				.filter(name -> !name.endsWith(".tmp")) // a backup being written; it only counts once renamed
				.sorted().toList();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private boolean isSaved(String name) {
		synchronized (storage.saved) {
			return storage.saved.containsKey(name);
		}
	}

	@Test(timeout = 20_000)
	public void savingThreadBacksUpOnItsOwn() throws InterruptedException, IOException {
		storage.setBackupOptions(BACKUP_INTERVAL_MILLIS, -1);
		storage.startSaving();
		set("x");
		set("a::1");
		await("a backup is made", () -> files().size() == 1);
		assertEquals("a::1\nx\n", Files.readString(folder.resolve(files().get(0))));
	}

	@Test(timeout = 20_000)
	public void savingThreadMakesNoBackupWhileASaveIsFailing() throws InterruptedException {
		storage.setBackupOptions(BACKUP_INTERVAL_MILLIS, -1);
		storage.failSaves.set(Integer.MAX_VALUE);
		storage.startSaving();
		set("x");
		await("the save fails", () -> storage.saveAttempts() >= 1);
		Thread.sleep(BACKUP_INTERVAL_MILLIS * 6);
		assertEquals(0, storage.backupAttempts.get());
	}

	@Test(timeout = 20_000)
	public void backupsAreOffByDefault() throws InterruptedException {
		storage.startSaving();
		set("x");
		await("the change is saved", () -> storage.saveAttempts() == 1);
		Thread.sleep(200);
		assertEquals(0, storage.backupAttempts.get());
		assertEquals(List.of(), files());
	}

	@Test(timeout = 20_000)
	public void flushAndStopSavingNeverBackUp() throws InterruptedException {
		storage.intervalMillis = 60_000; // the saving thread only runs its first cycle
		storage.setBackupOptions(1, -1);
		storage.startSaving();
		Thread.sleep(50);
		int before = storage.backupAttempts.get();

		set("x");
		assertTrue(storage.flush());
		set("y");
		storage.stopSaving();
		assertEquals(before, storage.backupAttempts.get());
	}

	@Test(timeout = 20_000)
	public void failedBackupDoesNotStopSaving() throws InterruptedException {
		storage.setBackupOptions(BACKUP_INTERVAL_MILLIS, -1);
		storage.failBackups.set(1);
		storage.startSaving();
		set("x");
		await("the backup fails", () -> storage.reportedFailures.stream()
			.anyMatch(message -> message.contains("Could not back up the database 'test'")));
		set("y");
		await("y is saved", () -> isSaved("y"));
		await("the backup is retried", () -> files().size() == 1);
	}

	@Test(timeout = 20_000)
	public void keepingNoneDeletesOldBackupsWhenSavingStarts() throws IOException {
		Files.writeString(folder.resolve("test_2020-01-01_00-00-00-000.csv"), "old");
		Files.writeString(folder.resolve("notes.txt"), "keep me");
		storage.setBackupOptions(BACKUP_INTERVAL_MILLIS, 0);
		assertEquals(2, files().size()); // nothing is deleted while configuring
		storage.startSaving();
		assertEquals(List.of("notes.txt"), files());
	}

}
