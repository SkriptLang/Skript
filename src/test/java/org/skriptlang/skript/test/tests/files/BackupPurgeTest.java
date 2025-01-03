package org.skriptlang.skript.test.tests.files;

import ch.njol.skript.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class BackupPurgeTest {

	private static final Path FOLDER = Path.of("plugins", "Skript", "backups");
	private static final Path VARIABLES = Path.of("plugins", "Skript", "variables.csv");

	@Before
	public void setup() throws IOException {
		if (Files.exists(FOLDER)) {
			clearFolder();
		} else {
			Files.createDirectory(FOLDER);
		}

		for (int i = 0; i < 100; i++) {
			Files.createFile(FOLDER.resolve("purge test " + i));
		}

		if (!Files.exists(VARIABLES)) {
			Files.createFile(VARIABLES);
		}
	}

	@Test
	public void testPurge() throws IOException {
		try (Stream<Path> files = Files.list(FOLDER)) {
			assertEquals(100, files.count());
		}

		testBackupPurge(50);
		testBackupPurge(20);
		testBackupPurge(0);

		assertThrows(IllegalArgumentException.class, () -> FileUtils.backupPurge(VARIABLES.toFile(), -1));
	}

	@After
	public void cleanUp() throws IOException {
		clearFolder();
	}

	private static void clearFolder() throws IOException {
		try (Stream<Path> list = Files.list(FOLDER)) {
			list.forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException ignored) {

				}
			});
		}
	}

	private static void testBackupPurge(int toKeep) throws IOException {
		FileUtils.backupPurge(VARIABLES.toFile(), toKeep);

		try (Stream<Path> files = Files.list(FOLDER)) {
			long count = files.count();
			assertNotNull(files);
			assertEquals("backup purge did not delete all files", toKeep, count);
		}
	}

}
