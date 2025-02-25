package org.skriptlang.skript.test.tests.files;

import ch.njol.skript.Skript;
import org.junit.Test;
import org.skriptlang.skript.config.SkriptConfig;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Ensures that the default files from Skript are generated.
 */
public class FilesGenerateTest {

	@Test
	public void checkFiles() {
		Skript skript = Skript.getInstance();
		File dataFolder = skript.getDataFolder();
		assertTrue(skript.getScriptsFolder().exists());
		assertTrue(skript.getScriptsFolder().isDirectory());
		assertTrue(new File(dataFolder, "config.sk").exists());
		assertTrue(new File(dataFolder, "features.sk").exists());
		File lang = new File(dataFolder, "lang");
		assertTrue(lang.exists());
		assertTrue(lang.isDirectory());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void checkConfigurationVersion() {
		assertEquals(SkriptConfig.VERSION.value(), Skript.getInstance().getDescription().getVersion());
	}

}
