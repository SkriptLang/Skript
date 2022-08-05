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

import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.registration.Module;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for Skript addons. Use {@link Skript#registerAddon(JavaPlugin)} to create a SkriptAddon instance for your plugin.
 */
public final class SkriptAddon {
	
	public final JavaPlugin plugin;
	public final Version version;
	
	/**
	 * Package-private constructor. Use {@link Skript#registerAddon(JavaPlugin)} to get a SkriptAddon for your plugin.
	 * 
	 * @param plugin The plugin representing the SkriptAddon to be registered.
	 */
	SkriptAddon(JavaPlugin plugin) {
		this.plugin = plugin;

		Version version;
		String descriptionVersion = plugin.getDescription().getVersion();
		try {
			version = new Version(descriptionVersion);
		} catch (final IllegalArgumentException e) {
			Matcher m = Pattern.compile("(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?").matcher(descriptionVersion);
			if (!m.find())
				throw new IllegalArgumentException("The version of the plugin " + plugin.getName() + " does not contain any numbers: " + descriptionVersion);
			version = new Version(Utils.parseInt("" + m.group(1)), m.group(2) == null ? 0 : Utils.parseInt("" + m.group(2)), m.group(3) == null ? 0 : Utils.parseInt("" + m.group(3)));
			Skript.warning("The plugin " + plugin.getName() + " uses a non-standard version syntax: '" + descriptionVersion + "'. Skript will use " + version + " instead.");
		}
		this.version = version;
	}
	
	@Override
	public String toString() {
		return plugin.getName();
	}
	
	public String getName() {
		return plugin.getName();
	}

	/**
	 * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript.
	 *
	 * Please note that if you need to load the same class multiple times,
	 * you should call {@link #resetEntryCache()} each time you call this method.
	 *
	 * @param basePackage The base package to start searching in (e.g. 'ch.njol.skript').
	 * @param subPackages Specific subpackages to search in (e.g. 'conditions')
	 *                    If no subpackages are provided, all subpackages of the base package will be searched.
	 * @return This SkriptAddon.
	 */
	public SkriptAddon loadClasses(String basePackage, String... subPackages) {
		return loadClasses(basePackage, true, true, null, subPackages);
	}

	private JarEntry @Nullable [] entryCache;

	/**
	 * This method resets the cache of jar entries used in {@link #loadClasses(String, boolean, boolean, Consumer, String...)}.
	 * This method is meant for internal use, so you <i>probably</i> don't need it!
	 * However, if you need loadClasses to load the same class multiple times, you <b>should</b> use this method.
	 *
	 * Note that this cache will be cleared when Skript stops accepting registrations.
	 */
	public void resetEntryCache() {
		entryCache = null;
	}

	/**
	 * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript.
	 * <p>
	 * Please note that if you need to load the same class multiple times,
	 * you should call {@link #resetEntryCache()} each time you call this method.
	 *
	 * @param basePackage The base package to start searching in (e.g. 'ch.njol.skript').
	 * @param initialize  Whether classes found in the package search should be initialized.
	 * @param recursive   Whether to recursively search through the subpackages provided.
	 * @param withClass   A consumer that will run with each found class.
	 * @param subPackages Specific subpackages to search in (e.g. 'conditions')
	 *                    If no subpackages are provided, all subpackages of the base package will be searched.
	 * @return This SkriptAddon
	 */
	@SuppressWarnings("ThrowableNotThrown")
	public SkriptAddon loadClasses(String basePackage, boolean initialize, boolean recursive, @Nullable Consumer<Class<?>> withClass, String... subPackages) {
		for (int i = 0; i < subPackages.length; i++)
			subPackages[i] = subPackages[i].replace('.', '/') + "/";
		basePackage = basePackage.replace('.', '/') + "/";

		// Used for tracking valid classes if a non-recursive search is done
		// Depth is the measure of how "deep" from the head package of 'basePackage' a class is
		int initialDepth = !recursive ? StringUtils.count(basePackage, '/') + 1 : 0;

		File file = getFile();
		if (file == null) {
			Skript.error("Unable to retrieve file from addon '" + getName() + "'. Classes will not be loaded.");
			return this;
		}

		try (JarFile jar = new JarFile(file)) {
			if (entryCache == null)
				entryCache = jar.stream().toArray(JarEntry[]::new);
		} catch (IOException e) {
			Skript.exception(e, "Failed to load classes for addon: " + plugin.getName());
			return this;
		}

		List<String> classNames = new ArrayList<>();
		for (int i = 0; i < entryCache.length; i++) {
			JarEntry e = entryCache[i];
			if (e == null) // This entry has already been loaded before
				continue;

			String name = e.getName();
			if (name.startsWith(basePackage) && name.endsWith(".class")) {
				boolean load = subPackages.length == 0;

				if (load) { // No subpackages provided
					load = recursive || StringUtils.count(name, '/') <= initialDepth;
				} else {
					for (String subPackage : subPackages) {
						if (
							// We also need to account for subpackage depths when not doing a recursive search
							(recursive || StringUtils.count(name, '/') <= initialDepth + StringUtils.count(subPackage, '/'))
							&& name.startsWith(subPackage, basePackage.length())
						) {
							load = true;
							break;
						}
					}
				}

				if (load) {
					classNames.add(name.replace('/', '.').substring(0, name.length() - ".class".length()));
					entryCache[i] = null; // Remove this item from the entry cache as this method will only load it once
				}
			}
		}

		classNames.sort(String::compareToIgnoreCase);

		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className, initialize, plugin.getClass().getClassLoader());
				if (withClass != null)
					withClass.accept(clazz);
			} catch (ClassNotFoundException ex) {
				Skript.exception(ex, "Cannot load class " + className);
			} catch (ExceptionInInitializerError err) {
				Skript.exception(err.getCause(), this + "'s class " + className + " generated an exception while loading");
			}
		}

		return this;
	}

	/**
	 * Loads all module classes found in the package search.
	 * @param basePackage The base package to start searching in (e.g. 'ch.njol.skript').
	 * @param subPackages Specific subpackages to search in (e.g. 'conditions').
	 *                    If no subpackages are provided, all subpackages will be searched.
	 *                    Note that the search will go no further than the first layer of subpackages.
	 *                    Note that this method will also clear the entry cache of ALL checked classes,
	 *                    	even those that are not actually a Module.
	 * @return This SkriptAddon.
	 */
	@SuppressWarnings("ThrowableNotThrown")
	public SkriptAddon loadModules(String basePackage, String... subPackages) {
		return loadClasses(basePackage, false, false, c -> {
			if (Module.class.isAssignableFrom(c) && !c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
				try {
					((Module) c.getConstructor().newInstance()).register(this);
				} catch (Exception e) {
					Skript.exception(e, "Failed to load module " + c);
				}
			}
		}, subPackages);
	}
	
	@Nullable
	private String languageFileDirectory = null;
	
	/**
	 * Loads language files from the specified directory (e.g. "lang") into Skript.
	 * Localized files will be read from the plugin's jar and the plugin's data file,
	 * but the <b>default.lang</b> file is only taken from the jar and <b>must</b> exist!
	 * 
	 * @param directory The directory containing language files.
	 * @return This SkriptAddon.
	 */
	public SkriptAddon setLanguageFileDirectory(String directory) {
		if (languageFileDirectory != null)
			throw new IllegalStateException("The language file directory may only be set once.");
		directory = "" + directory.replace('\\', '/');
		if (directory.endsWith("/"))
			directory = "" + directory.substring(0, directory.length() - 1);
		languageFileDirectory = directory;
		Language.loadDefault(this);
		return this;
	}

	/**
	 * @return The language file directory set for this addon.
	 * Null if not yet set using {@link #setLanguageFileDirectory(String)}.
	 */
	@Nullable
	public String getLanguageFileDirectory() {
		return languageFileDirectory;
	}
	
	@Nullable
	private File file = null;
	
	/**
	 * @return The jar file of the plugin.
	 * 			After this method is first called, the file will be cached for future use.
	 */
	@Nullable
	public File getFile() {
		if (file != null)
			return file;
		try {
			Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
			getFile.setAccessible(true);
			file = (File) getFile.invoke(plugin);
			return file;
		} catch (NoSuchMethodException | IllegalArgumentException e) {
			Skript.outdatedError(e);
		} catch (IllegalAccessException e) {
			assert false;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		return null;
	}
	
}
