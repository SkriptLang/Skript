package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.util.Registry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores {@link GlobalFile}s
 */
public class GlobalFileRegistry implements Registry<GlobalFile> {

	private final Set<GlobalFile> globalFiles = new HashSet<>();

	/**
	 * Registers and initializes a new {@link GlobalFile}
	 */
	public void registerGlobal(GlobalFile globalFile) {
		if (globalFiles.stream().anyMatch(g -> g.getClass().equals(globalFile.getClass()))) {
			throw new IllegalArgumentException("Global file " + globalFile.name() + " is already registered");
		}

		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(globalFile.addon().source());

		File globalsFolder = new File(plugin.getDataFolder(), "/globals/");
		if (!globalsFolder.exists()) {
			try {
				Files.createDirectory(globalsFolder.toPath());
			} catch (IOException e) {
				Skript.exception(e, "Couldn't create globals folder for " + globalFile.addon().name());
			}
		}

		globalFiles.add(globalFile);
		globalFile.onInit();
	}

	/**
	 * Unregisters a global file
	 * This will also delete the file itself
	 */
	public void unregisterGlobal(GlobalFile globalFile) {
		try {
			Files.deleteIfExists(globalFile.file.toPath());
		} catch (IOException e) {
			Skript.error("Couldn't unregister global file " + globalFile.name() + " for " + globalFile.addon().name());
		}

		globalFiles.remove(globalFile);
	}

	/**
	 * Reloads all global files
	 * @see #reloadAll(SkriptAddon)
	 */
	public void reloadAll() {
		globalFiles.forEach(GlobalFile::reload);
	}

	/**
	 * Reloads all global files of a {@link SkriptAddon}
	 * @param addon The {@link SkriptAddon}
	 * @see #reloadAll()
	 */
	public void reloadAll(SkriptAddon addon) {
		globalFiles.forEach(globalFile -> {
			if (globalFile.addon().equals(addon)) {
				globalFile.reload();
			}
		});
	}

	/**
	 * @return All global files
	 */
	@Override
	public Collection<GlobalFile> elements() {
		return globalFiles;
	}

}
