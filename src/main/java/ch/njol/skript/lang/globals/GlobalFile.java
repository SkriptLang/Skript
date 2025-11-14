package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Represents a file in the globals folder
 */
abstract class GlobalFile {

	protected final File file;

	public GlobalFile(String name) {
		String filePath = "globals/" + name + ".sk";
		file = new File(Skript.getInstance().getDataFolder(), filePath);

		if (!file.exists()) {
			copyFile(filePath, file);
		}
	}

	/**
	 * Copies a file from the Skript jar into the target file
	 */
	private static void copyFile(String sourcePath, File targetFile) {
		try (InputStream stream = Skript.getInstance().getResource(sourcePath)) {
			if (stream == null) {
				Skript.error("The " + sourcePath + " file doesn't exist and couldn't be read from the jar file.");
				return;
			}
			Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Skript.exception(e, "Error while loading the " + sourcePath + " file from the jar file.");
		}
	}

}
