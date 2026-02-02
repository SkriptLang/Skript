package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.util.Registry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores {@link GlobalFile}s
 */
public class GlobalFileRegistry implements Registry<GlobalFile> {

	private final Set<GlobalFile> globalFiles = new HashSet<>();

	/**
	 * Registers a new GlobalFile
	 */
	public void registerGlobal(GlobalFile globalFile) {
		if (globalFiles.stream().anyMatch(g -> g.getClass().equals(globalFile.getClass()))) {
			throw new IllegalArgumentException("Global file " + globalFile.getName() + " is already registered");
		}

		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(globalFile.getAddon().source());
		String name = globalFile.getName();

		File globalsFolder = new File(plugin.getDataFolder(), "/globals/");
		if (!globalsFolder.exists()) {
			globalsFolder.mkdir();
		}

		String filePath = "globals/" + name + ".sk";
		File file = new File(plugin.getDataFolder(), filePath);

		if (!file.exists()) {
			copyFile(plugin, filePath, file);
		}

		globalFiles.add(globalFile);
		globalFile.load();
	}

	/**
	 * Unregisters a GlobalFile
	 * This will also delete the file itself
	 */
	public void unregisterGlobal(GlobalFile globalFile) {
		globalFile.file.delete();
		globalFiles.remove(globalFile);
	}

	/**
	 * Reloads all GlobalFiles
	 * @see #reloadAll(SkriptAddon)
	 */
	public void reloadAll() {
		globalFiles.forEach(GlobalFile::load);
	}

	/**
	 * Reloads all GlobalFiles of a SkriptAddon
	 * @param addon The SkriptAddon
	 * @see #reloadAll()
	 */
	public void reloadAll(SkriptAddon addon) {
		globalFiles.forEach(globalFile -> {
			if (globalFile.getAddon().equals(addon)) {
				globalFile.load();
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

	/**
	 * Copies a file from the plugin's jar into target file
	 * Replaces existing files
	 */
	private static void copyFile(JavaPlugin plugin, String sourcePath, File targetFile) {
		try (InputStream stream = plugin.getResource(sourcePath)) {
			if (stream == null) {
				Skript.error("The " + sourcePath + " file doesn't exist and couldn't be read from the " + plugin.getName() + " jar file.");
				return;
			}
			Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Skript.exception(e, "Error while loading the " + sourcePath + " file from the jar file.");
		}
	}

}
