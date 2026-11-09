package org.skriptlang.skript.variables.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

/**
 * Tests {@link StorageBackups} directly, with made-up times: when backups are due, how their files are written,
 * and which old ones are deleted.
 */
public class StorageBackupsTest {

	private static final long INTERVAL = 1_000; // in made-up nanoseconds

	private Path folder;
	private String contents = "x\n";
	private int failWrites;
	private int writes;
	private final List<String> reports = new ArrayList<>();

	@Before
	public void setUp() throws IOException {
		folder = Files.createTempDirectory("skript-backups-test");
	}

	@After
	public void tearDown() throws IOException {
		try (Stream<Path> files = Files.walk(folder)) {
			for (Path path : files.sorted((a, b) -> b.compareTo(a)).toList())
				Files.deleteIfExists(path);
		}
	}

	/**
	 * A clock that moves on by a millisecond every time it is read, so every backup gets its own file name.
	 */
	private static Clock tickingClock() {
		AtomicLong millis = new AtomicLong(1_700_000_000_000L);
		return new Clock() {
			@Override
			public ZoneId getZone() {
				return ZoneOffset.UTC;
			}

			@Override
			public Clock withZone(ZoneId zone) {
				return this;
			}

			@Override
			public Instant instant() {
				return Instant.ofEpochMilli(millis.getAndIncrement());
			}
		};
	}

	/**
	 * @return backups for a storage named "test", created at made-up time 0, so the first is due at {@link #INTERVAL}
	 */
	private StorageBackups backups(int toKeep, boolean compress) {
		return new StorageBackups("test", folder, compress, INTERVAL, toKeep, 0, tickingClock(),
			out -> {
				writes++;
				out.write(contents.getBytes(StandardCharsets.UTF_8));
				if (failWrites > 0) {
					failWrites--;
					throw new IOException("failing on purpose");
				}
			},
			(t, message) -> reports.add(message));
	}

	private StorageBackups backups(int toKeep) {
		return backups(toKeep, false);
	}

	private List<String> files() throws IOException {
		try (Stream<Path> files = Files.list(folder)) {
			return files.map(path -> path.getFileName().toString()).sorted().toList();
		}
	}

	private List<String> backupFiles() throws IOException {
		return files().stream()
			.filter(name -> name.startsWith("test_") && (name.endsWith(".csv") || name.endsWith(".csv.gz")))
			.toList();
	}

	private String read(String fileName) throws IOException {
		Path path = folder.resolve(fileName);
		try (InputStream in = fileName.endsWith(".gz")
			? new GZIPInputStream(Files.newInputStream(path))
			: Files.newInputStream(path)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private void create(String fileName) throws IOException {
		Files.writeString(folder.resolve(fileName), "existing");
	}

	/**
	 * Makes {@code count} backups, one interval apart, each with something new saved in between.
	 */
	private void makeBackups(StorageBackups backups, int count) {
		for (int i = 1; i <= count; i++) {
			contents = "v" + i + "\n";
			backups.changed();
			backups.backupIfDue(i * INTERVAL);
		}
	}

	// ---- when backups are due ----

	@Test
	public void firstBackupIsDueOneIntervalAfterCreation() throws IOException {
		StorageBackups backups = backups(-1);
		backups.backupIfDue(INTERVAL - 1);
		assertEquals(0, writes);
		backups.backupIfDue(INTERVAL);
		assertEquals(1, backupFiles().size());
		assertEquals("x\n", read(backupFiles().get(0)));
	}

	@Test
	public void firstBackupHappensEvenIfNothingChanged() {
		StorageBackups backups = backups(-1);
		backups.backupIfDue(INTERVAL);
		assertEquals(1, writes);
	}

	@Test
	public void noBackupWhenNothingChangedSinceTheLast() {
		StorageBackups backups = backups(-1);
		backups.backupIfDue(INTERVAL);
		backups.backupIfDue(10 * INTERVAL);
		assertEquals(1, writes);

		backups.changed();
		backups.backupIfDue(10 * INTERVAL);
		assertEquals(2, writes);
	}

	@Test
	public void nextBackupIsDueOneIntervalAfterTheLast() {
		StorageBackups backups = backups(-1);
		backups.backupIfDue(INTERVAL + 500); // late, so the next is due at 2.5 intervals, not 2
		backups.changed();
		backups.backupIfDue(2 * INTERVAL + 499);
		assertEquals(1, writes);
		backups.backupIfDue(2 * INTERVAL + 500);
		assertEquals(2, writes);
	}

	@Test
	public void zeroIntervalMeansNoBackups() {
		StorageBackups backups = new StorageBackups("test", folder, false, 0, -1, 0, tickingClock(),
			out -> writes++, (t, message) -> reports.add(message));
		assertFalse(backups.isOn());
		backups.backupIfDue(Long.MAX_VALUE / 2);
		assertEquals(0, writes);
	}

	@Test
	public void keepingNoneMeansNoBackups() {
		StorageBackups backups = backups(0);
		assertFalse(backups.isOn());
		backups.backupIfDue(10 * INTERVAL);
		assertEquals(0, writes);
	}

	// ---- backup files ----

	@Test
	public void backupsAreNamedAfterTheStorage() throws IOException {
		backups(-1).backupIfDue(INTERVAL);
		String file = backupFiles().get(0);
		assertTrue(file, file.matches("test_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}\\.csv"));
	}

	@Test
	public void compressedBackupsAreGzipped() throws IOException {
		backups(-1, true).backupIfDue(INTERVAL);
		String file = backupFiles().get(0);
		assertTrue(file, file.endsWith(".csv.gz"));
		assertEquals("x\n", read(file));
	}

	@Test
	public void folderIsCreatedIfMissing() throws IOException {
		Files.delete(folder);
		backups(-1).backupIfDue(INTERVAL);
		assertEquals(1, backupFiles().size());
	}

	@Test
	public void failedBackupLeavesNothingBehindAndIsRetriedAfterAnInterval() throws IOException {
		StorageBackups backups = backups(-1);
		failWrites = 1;
		backups.backupIfDue(INTERVAL);
		assertEquals(List.of(), files());
		assertEquals(1, reports.size());
		assertTrue(reports.get(0), reports.get(0).contains("Could not back up the database 'test'"));

		backups.backupIfDue(INTERVAL + 1); // not straight away
		assertEquals(1, writes);
		backups.backupIfDue(2 * INTERVAL); // but an interval later, without anything new being saved
		assertEquals(1, backupFiles().size());
	}

	// ---- deleting old backups ----

	@Test
	public void onlyTheNewestBackupsAreKept() throws IOException {
		StorageBackups backups = backups(2);
		makeBackups(backups, 4);
		List<String> files = backupFiles();
		assertEquals(files.toString(), 2, files.size());
		assertEquals("v3\n", read(files.get(0)));
		assertEquals("v4\n", read(files.get(1)));
	}

	@Test
	public void keepingAllDeletesNothing() throws IOException {
		create("test_2020-01-01_00-00-00-000.csv");
		makeBackups(backups(-1), 3);
		assertEquals(4, backupFiles().size());
	}

	@Test
	public void pruningLeavesOtherFilesAlone() throws IOException {
		create("notes.txt");
		create("other_2020-01-01_00-00-00-000.csv");
		create("test_notes.txt");
		makeBackups(backups(1), 3);
		assertEquals(1, backupFiles().size());
		List<String> files = files();
		assertTrue(files.toString(), files.containsAll(
			List.of("notes.txt", "other_2020-01-01_00-00-00-000.csv", "test_notes.txt")));
	}

	@Test
	public void pruningDeletesInterruptedBackups() throws IOException {
		create("test_2020-01-01_00-00-00-000.csv.tmp");
		makeBackups(backups(5), 1);
		assertFalse(files().contains("test_2020-01-01_00-00-00-000.csv.tmp"));
	}

	@Test
	public void startWithKeepingNoneDeletesOnlyThisStoragesBackups() throws IOException {
		create("test_2020-01-01_00-00-00-000.csv");
		create("test_2020-01-02_00-00-00-000.csv.gz");
		create("test_2020-01-03_00-00-00-000.csv.tmp");
		create("notes.txt");
		create("other_2020-01-01_00-00-00-000.csv");
		backups(0).start();
		assertEquals(List.of("notes.txt", "other_2020-01-01_00-00-00-000.csv"), files());
		assertTrue(reports.toString(), reports.isEmpty());
	}

	@Test
	public void startOtherwiseDeletesNothing() throws IOException {
		create("test_2020-01-01_00-00-00-000.csv");
		create("test_2020-01-02_00-00-00-000.csv");
		backups(1).start();
		backups(-1).start();
		assertEquals(2, backupFiles().size());
	}

	@Test
	public void startWithoutAFolderDoesNothing() throws IOException {
		Files.delete(folder);
		backups(0).start();
		assertFalse(Files.exists(folder));
		Files.createDirectories(folder); // for tearDown
	}

	@Test
	public void unsafeCharactersInTheNameAreReplaced() {
		assertEquals("my_database_1", StorageBackups.safeName("my database/1"));
		assertEquals("default", StorageBackups.safeName("default"));
		assertEquals("a.b-c_d", StorageBackups.safeName("a.b-c_d"));
	}

}
