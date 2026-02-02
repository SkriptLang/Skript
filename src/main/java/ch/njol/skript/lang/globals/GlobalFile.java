package ch.njol.skript.lang.globals;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.File;

/**
 * Represents a file in the globals folder.
 * <p>
 * GlobalFiles allow addons to create config-like files that can be reloaded
 * using {@code /sk reload globals}.
 * <p>
 * To create a GlobalFile, extend this class and implement {@link #load()}.
 * Registering this GlobalFile via {@link GlobalFileRegistry#registerGlobal(GlobalFile)}
 * will {@link #load()} the global file.
 * <p>
 * Each addon's global files are stored in their own data folder: {@code plugins/YourAddon/globals/filename.sk}
 *
 * @see GlobalFileRegistry
 * @see GlobalOptions
 */
public abstract class GlobalFile {

	private final SkriptAddon addon;
	private final String name;
	protected final File file;

	/**
	 * Loads this global file. This method should not handle file creation.
	 */
	public abstract void load();

	public GlobalFile(SkriptAddon addon, String name) {
		this.addon = addon;
		this.name = name;

		String filePath = "globals/" + name + ".sk";
		Plugin plugin = JavaPlugin.getProvidingPlugin(addon.source());
		file = new File(plugin.getDataFolder(), filePath);
	}

	/**
	 * @return The name of this GlobalFile
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The owner of this GlobalFile
	 */
	public SkriptAddon getAddon() {
		return addon;
	}

}
