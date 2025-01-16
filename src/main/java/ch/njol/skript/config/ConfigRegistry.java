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
package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A manager for registering, managing and discarding user-provided configuration files.
 * These files are in the managed config directory (i.e. {@link #directory}) and should be obtained through this
 * manager.
 * <br/><br/>
 * Scripts must register a config explicitly by name in order to use it.
 * This prompts the manager to load that config file <em>during</em> script loading, i.e. config load
 * problems shall be forwarded to the linked script.
 * <br/><br/>
 * When a script reloads, its linked configuration files will also be reloaded.
 * When a script unloads, its linked configuration files will be discarded
 * <em>if they have no other linked scripts</em> to allow for proper garbage collection
 * of resources.
 * <br/><br/>
 * A script can obtain a registered config file with {@code [the] config [named] %name%"} in the same
 * way as obtaining a script reference, but from the path of the {@code Skript/configs/} directory.
 * For safety reasons, scripts may not register (or obtain) any config file outside the
 * {@code Skript/configs/} directory.
 * <br/><br/>
 * Multiple scripts may register the same config file in order to use it.
 * If no script registers a config file it will never be loaded (or approached) by the manager.
 */
/*
 * TODO
 * 	This is designed to be (replaced by|refactored into) a proper registry when the registries rework PR
 * 	is completed. The overall skeleton is designed to remain, so that there should be no breaking changes
 * 	for anything using it. I.e. you will still be able to use Skript#userConfigs() and obtain 'this' class
 * 	although these will just become helper methods for the proper registry behaviour.
 * */
public class ConfigRegistry {

	private final File directory;
	private final Map<String, Reference<SharedConfig>> configs;

	public ConfigRegistry(File directory) {
		this.directory = directory;
		this.configs = new HashMap<>();
	}

	/**
	 * Checks whether a config is registered for a script. This can be done during init phase.
	 *
	 * @param script The current script
	 * @param configPath The path to check
	 * @return Whether the config is registered
	 */
	public boolean isRegistered(Script script, String configPath) {
		if (configPath.endsWith("/") || configPath.endsWith("\\")) // bad path
			return false;
		@Nullable ConfigStorage data = script.getData(ConfigStorage.class);
		if (data == null)
			return false; // none are registered
		configPath = this.resolvePath(configPath);
		return data.containsKey(configPath);
	}

	/**
	 * Obtains a config registered by the current script.
	 * This <em>must</em> be called after {@link #isRegistered(Script, String)}.
	 * If registration was not checked first, the behaviour may be unexpected.
	 *
	 * @param script The script to check
	 * @param configPath The config path
	 * @return The config instance
	 */
	public @NotNull Config getConfig(Script script, String configPath) {
		@Nullable ConfigStorage data = script.getData(ConfigStorage.class);
		if (data == null) // we should ALWAYS call isRegistered first
			throw new IllegalStateException();
		configPath = this.resolvePath(configPath);
		return data.get(configPath);
	}

	/**
	 * Registers a config (by path) with the given script. From then on, the script may use
	 * the config and reference it in code.
	 * This also loads the config.
	 *
	 * @param script The current script
	 * @param configPath The config path (inside the managed configs folder) to register
	 * @return Whether the config successfully registered
	 */
	public boolean register(Script script, String configPath) {
		if (configPath.endsWith("/") || configPath.endsWith("\\")) {
			Skript.error("Cannot register a directory '" + configPath + "' as a config");
			return false;
		}
		configPath = this.resolvePath(configPath);
		File file = new File(directory, configPath);
		if (!this.isFileManaged(file)) {
			Skript.error("Cannot register '" + configPath + "' as it is not a valid file inside the config folder");
			return false;
		}
		if (!file.exists()) try {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException error) {
			Skript.error("Cannot create '" + configPath + "': " + ExceptionUtils.toString(error));
			return false;
		}

		Config config;
		try {
			config = this.getConfig(configPath, file);
		} catch (IOException error) {
			Skript.error("Unable to load config '" + configPath + "': " + ExceptionUtils.toString(error));
			return false;
		}
		ConfigStorage data = script.getData(ConfigStorage.class, ConfigStorage::new);
		data.putIfAbsent(configPath, config);
		return true;
	}

	protected @NotNull SharedConfig getConfig(String path, File file) throws IOException {
		Reference<SharedConfig> reference = configs.get(path);
		SharedConfig found;
		if (reference == null || (found = reference.get()) == null) {
			Config config = this.loadConfig(file);
			found = new SharedConfig(this, config);
			this.configs.put(path, new WeakReference<>(found));
		}
		return found;
	}

	private String resolvePath(String configPath) {
		if (!StringUtils.endsWithIgnoreCase(configPath, ".sk")) {
			configPath = configPath + ".sk";
		}
		File file = new File(directory, configPath);
		Path relative = directory.toPath().relativize(file.toPath());
		return relative.toString();
	}

	protected Config loadConfig(File file) throws IOException {
		return new Config(file, false, true, ":");
	}

	/**
	 * Whether a file is in the domain of this config registry (i.e. is it somewhere inside our folder?)
	 * @param file The file to test
	 * @return Whether the file can be managed by our registry
	 */
	protected boolean isFileManaged(File file) {
		try {
			return (TestMode.ENABLED || file.getCanonicalPath().startsWith(directory.getCanonicalPath() + File.separator));
		} catch (IOException ignored) {
			return false;
		}
	}

	protected static class ConfigStorage extends HashMap<String, Config> implements ScriptData {
	}

}
