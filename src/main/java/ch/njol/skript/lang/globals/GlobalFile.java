package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Represents a file in the globals folder.
 * <p>
 * Global files allow Skript and addons to create config-like files that can be reloaded
 * using {@code /sk reload globals}.
 * <p>
 * To create a global file, extend this class and implement the methods.
 * Registering this global file via {@link GlobalFileRegistry#registerGlobal(GlobalFile)}
 * will initialize the global file.
 * <p>
 * Each addon's global files are stored in their own data folder: {@code /plugins/YourAddon/globals/filename.sk}
 *
 * @see GlobalFileRegistry
 * @see GlobalOptions
 */
public abstract class GlobalFile {

	private final SkriptAddon addon;
	private final String name;
	protected final File file;
	private boolean isLoaded = false;

	/**
	 * Loads this global file.
	 */
	public void load() {
		onLoad();
		isLoaded = true;
	}

	/**
	 * Unloads this global file.
	 */
	public void unload() {
		onUnload();
		isLoaded = false;
	}

	/**
	 * Reloads this global file. If the global file is not loaded, the method won't do anything.
	 */
	public void reload() {
		if (!isLoaded) {
			return;
		}
		onLoad();
		onUnload();
	}

	/**
	 * @return Whether this global file is loaded.
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * Called when the global file is initialized. This method is only called when the global file is registered,
	 * not on reload.
	 * You should call {@link #load()} if you want to load the file after initialization.
	 * <p>
	 * This method can be used to create non-existing global files. Either manually or
	 * using the {@link #copyFile(String)} method to easily copy a default file from the JAR.
	 */
	protected abstract void onInit();

	/**
	 * Called when the global file is loaded. This method can be called when global files are reloaded.
	 */
	protected abstract void onLoad();

	/**
	 * Called when the global file is unloaded. This method can be called when global files are reloaded.
	 */
	protected abstract void onUnload();

	public GlobalFile(SkriptAddon addon, String name) {
		this.addon = addon;
		this.name = name;

		String filePath = "globals/" + name;
		Plugin plugin = JavaPlugin.getProvidingPlugin(addon.source());
		file = new File(plugin.getDataFolder(), filePath);
	}

	/**
	 * @return The name of this GlobalFile
	 */
	public String name() {
		return name;
	}

	/**
	 * @return The owner of this GlobalFile
	 */
	public SkriptAddon addon() {
		return addon;
	}

	/**
	 * Copies a file from the plugin's jar into target file
	 * Replaces existing files
	 * <p>
	 * Example usage to load the <code>/resources/globals/options.sk</code> file from the JAR:
	 * <pre>
	 *  protected void onInit() {
	 *    if (!file.exists())
	 *      copyFile("options.sk");
	 *    load();
	 *  }
	 * </pre>
	 */
	protected void copyFile(String sourcePath) {
		sourcePath = "globals/" + sourcePath;
		Plugin plugin = JavaPlugin.getProvidingPlugin(addon.source());

		try (InputStream stream = plugin.getResource(sourcePath)) {
			if (stream == null) {
				Skript.error("The " + sourcePath + " file doesn't exist and couldn't be read from the " + plugin.getName() + " jar file.");
				return;
			}
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// noinspection ThrowableNotThrown
			Skript.exception(e, "Error while loading the " + sourcePath + " file from the jar file.");
		}
	}

}
