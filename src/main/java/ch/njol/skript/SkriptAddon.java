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
package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;

/**
 * Utility class for Skript addons. Use {@link Skript#registerAddon(JavaPlugin)} to create a SkriptAddon instance for your plugin.
 */
public final class SkriptAddon {

	public final JavaPlugin plugin;
	public final Version version;
	private final String name;

	/**
	 * Package-private constructor. Use {@link Skript#registerAddon(JavaPlugin)} to get a SkriptAddon for your plugin.
	 * 
	 * @param p
	 */
	SkriptAddon(final JavaPlugin p) {
		plugin = p;
		name = "" + p.getName();
		Version v;
		try {
			v = new Version("" + p.getDescription().getVersion());
		} catch (final IllegalArgumentException e) {
			final Matcher m = Pattern.compile("(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?").matcher(p.getDescription().getVersion());
			if (!m.find())
				throw new IllegalArgumentException("The version of the plugin " + p.getName() + " does not contain any numbers: " + p.getDescription().getVersion());
			v = new Version(Utils.parseInt("" + m.group(1)), m.group(2) == null ? 0 : Utils.parseInt("" + m.group(2)), m.group(3) == null ? 0 : Utils.parseInt("" + m.group(3)));
			Skript.warning("The plugin " + p.getName() + " uses a non-standard version syntax: '" + p.getDescription().getVersion() + "'. Skript will use " + v + " instead.");
		}
		version = v;
	}

	@Override
	public final String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript does it.
	 * 
	 * @param basePackage The base package to add to all sub packages, e.g. <tt>"ch.njol.skript"</tt>.
	 * @param subPackages Which subpackages of the base package should be loaded, e.g. <tt>"expressions", "conditions", "effects"</tt>. Subpackages of these packages will be loaded
	 *            as well. Use an empty array to load all subpackages of the base package.
	 * @throws IOException If some error occurred attempting to read the plugin's jar file.
	 * @return This SkriptAddon
	 */
	public SkriptAddon loadClasses(String basePackage, String... subPackages) throws IOException {
		Utils.getClasses(plugin, basePackage, subPackages);
		return this;
	}

	@Nullable
	private String languageFileDirectory = null;

	/**
	 * Makes Skript load language files from the specified directory, e.g. "lang" or "skript lang" if you have a lang folder yourself. Localised files will be read from the
	 * plugin's jar and the plugin's data folder, but the default English file is only taken from the jar and <b>must</b> exist!
	 * 
	 * @param directory Directory name
	 * @return This SkriptAddon
	 */
	public SkriptAddon setLanguageFileDirectory(String directory) {
		if (languageFileDirectory != null)
			throw new IllegalStateException();
		directory = "" + directory.replace('\\', '/');
		if (directory.endsWith("/"))
			directory = "" + directory.substring(0, directory.length() - 1);
		languageFileDirectory = directory;
		Language.loadDefault(this);
		return this;
	}

	@Nullable
	public String getLanguageFileDirectory() {
		return languageFileDirectory;
	}

	@Nullable
	private File file;

	/**
	 * The first invocation of this method uses reflection to invoke the protected method {@link JavaPlugin#getFile()} to get the plugin's jar file.
	 * The file is then cached and returned upon subsequent calls to this method to reduce usage of reflection.
	 * Only nullable if there was an exception thrown.
	 * 
	 * @return The jar file of the plugin.
	 */
	@Nullable
	public File getFile() {
		if (file == null)
			file = Utils.getFile(plugin);
		return file;
	}

}
