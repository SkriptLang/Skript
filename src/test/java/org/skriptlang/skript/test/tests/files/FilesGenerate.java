/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.test.tests.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;

/**
 * Ensures that the default files from Skript are generated.
 */
public class FilesGenerate {

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
		assertEquals(SkriptConfig.getConfig().get("version"), Skript.getInstance().getDescription().getVersion());
	}

}
