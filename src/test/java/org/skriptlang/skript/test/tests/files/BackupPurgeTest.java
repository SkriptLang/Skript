package org.skriptlang.skript.test.tests.files;

import ch.njol.skript.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BackupPurgeTest {

	@Test
	public void testPurge() throws IOException {
		File dir = new File("plugins/Skript/backups/");
		if (!dir.exists()) {
			dir.mkdir();
		}

		File vars = new File("plugins/Skript/variables.csv");
		if (!vars.exists()) {
			vars.createNewFile();
		}

		// Create Filler Files to be used for testing
		for (int i = 0; i < 100; i++) {
			(new File(dir, ("PurgeTest_"+i))).createNewFile();
		}
		// Test 100 filler files were created
		assertEquals("Filler Files != 100", 100, (new ArrayList<File>(Arrays.asList(dir.listFiles()))).size());

		// Test 'backupPurge' method deleting to 50
		FileUtils.backupPurge(vars, 50);
		assertEquals("Backup Purge did not delete down to 50", 50, (new ArrayList<File>(Arrays.asList(dir.listFiles()))).size());

		// Test 'backupPurge' method deleting to 20
		FileUtils.backupPurge(vars, 20);
		assertEquals("Backup Purge did not delete down to 20", 20, (new ArrayList<File>(Arrays.asList(dir.listFiles()))).size());

		// Test 'backupPurge' method deleting all files
		FileUtils.backupPurge(vars, 0);
		assertEquals("Backup Purge did not delete all files", 0, (new ArrayList<File>(Arrays.asList(dir.listFiles()))).size());

		// Test calling with invalid input
		assertThrows("Backup Purge did not throw exception for invalid input", IllegalArgumentException.class, () -> FileUtils.backupPurge(vars, -1));

	}

}
