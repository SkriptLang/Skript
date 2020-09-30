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
package ch.njol.skript.tests.runner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.tests.TestResults;

/**
 * Static utilities for Skript's 'test mode'.
 */
public class TestMode {
	
	private static final String ROOT = "skript.testing.";
	
	/**
	 * Determines if test mode is enabled. In test mode, Skript will not load
	 * normal scripts, working with {@link #TEST_DIR} instead.
	 */
	public static final boolean ENABLED = "true".equals(System.getProperty(ROOT + "enabled"));
	
	/**
	 * Root path for scripts containing tests. If {@link #DEV_MODE} is enabled,
	 * a command will be available to run them individually or collectively.
	 * Otherwise, all tests are run, results are written in JSON format to
	 * {@link #RESULTS_FILE} as in {@link TestResults}.
	 */
	@SuppressWarnings("null")
	public static final Path TEST_DIR = ENABLED ? Paths.get(System.getProperty(ROOT + "dir")) : null;
	
	/**
	 * Enable test development mode. Skript will allow individual test scripts
	 * to be loaded and ran, and prints results to chat or console.
	 */
	public static final boolean DEV_MODE = ENABLED && "true".equals(System.getProperty(ROOT + "devMode"));
	
	/**
	 * Path to file where to save results in JSON format.
	 */
	@SuppressWarnings("null")
	public static final Path RESULTS_FILE = ENABLED ? Paths.get(System.getProperty(ROOT + "results")) : null;
	
	/**
	 * In development mode, file that was last run.
	 */
	@Nullable
	public static File lastTestFile;
}
