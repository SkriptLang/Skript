package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.util.FileUtils;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utilities for the {@code /skript} subcommands that involve scripts.
 */
class ScriptCommandUtils {

	private static final ArgsMessage INVALID_SCRIPT_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".invalid script");
	private static final ArgsMessage INVALID_FOLDER_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".invalid folder");

	public static @Nullable File getScriptFileFromName(CommandSender sender, String name) {
		return getScriptFileFromName(sender, name, Skript.getInstance().getScriptsFolder());
	}

	public static @Nullable File getScriptFileFromName(CommandSender sender, String name, File directoryFile) {
		File f = ScriptLoader.getScriptFileFromName(name, directoryFile);
		if (f == null) {
			// Always allow '/' and '\' regardless of OS
			boolean isDirectory = name.endsWith("/") || name.endsWith("\\") || name.endsWith(File.separator);
			Skript.error(sender, (isDirectory ? INVALID_FOLDER_MESSAGE : INVALID_SCRIPT_MESSAGE).toString(name));
			return null;
		}
		return f;
	}

	/**
	 * Creates a Brigadier suggestion provider for scripts and folders.
	 *
	 * @param testDirectory Whether to suggest scripts from the test directory.
	 * @param disabledScripts Whether to suggest disabled scripts instead of loaded ones.
	 * @return The suggestion provider.
	 */
	public static SuggestionProvider<CommandSourceStack> suggestions(boolean testDirectory, boolean disabledScripts) {
		return (context, builder) -> {
			for (String option : getScriptSuggestions(builder.getRemaining().trim(), testDirectory, disabledScripts)) {
				builder.suggest(option);
			}
			return builder.buildFuture();
		};
	}

	/**
	 * Gets tab completions for script commands such as {@code /sk enable|reload|disable}.
	 *
	 * @param scriptArg The argument currently being typed.
	 * @param testDirectory Whether to suggest scripts from the test directory.
	 * @param disabledScripts Whether to suggest disabled scripts instead of loaded ones.
	 * @return A list of directories and Skript files.
	 */
	private static List<String> getScriptSuggestions(String scriptArg, boolean testDirectory, boolean disabledScripts) {
		List<String> options = new ArrayList<>();

		File scripts = testDirectory ? TestMode.TEST_DIR.toFile() : Skript.getInstance().getScriptsFolder();
		String scriptsPathString = scripts.toPath().toString();
		int scriptsPathLength = scriptsPathString.length();
		String fs = File.separator;
		// Accept both '/' and '\' regardless of the OS
		String normalizedArg = scriptArg.replace('/', File.separatorChar).replace('\\', File.separatorChar);

		FileFilter fileFilter = disabledScripts
			? ScriptLoader.getDisabledScriptsFilter()
			: ScriptLoader.getLoadedScriptsFilter();

		// Live update, this will get all old and new (even not loaded) scripts
		// TODO Find a better way for caching, it isn't exactly ideal to be calling this method constantly
		// TODO Make a cache based on last modified date of the 'scripts' folder?
		try (Stream<Path> files = Files.walk(scripts.toPath(), FileVisitOption.FOLLOW_LINKS)) {
			files.map(Path::toFile)
				.forEach(file -> {
					if (!fileFilter.accept(file)) {
						// Folders are not prefixed with '-' when their scripts are disabled,
						// but they still need to be suggested so a whole folder can be enabled.
						if (!disabledScripts || !file.isDirectory())
							return;
					}

					// Ignore hidden files like .git/ for users that use git source control.
					if (file.isHidden())
						return;

					String fileString = file.toString().substring(scriptsPathLength);
					// Remove the leading file separator
					if (fileString.startsWith(fs))
						fileString = fileString.substring(1);

					if (fileString.isEmpty())
						return;

					if (file.isDirectory())
						fileString += fs; // Add file separator at the end of directories

					// Make sure the user's argument matches with the file's name or beginning of file path
					if (!normalizedArg.isEmpty() && !fileString.startsWith(normalizedArg) && !file.getName().startsWith(normalizedArg))
						return;

					options.add(fileString);

					// Also suggest the name without the disabled prefix
					if (file.isFile()) {
						String name = file.getName();
						if (name.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX)) {
							int nameStart = fileString.length() - name.length();
							options.add(fileString.substring(0, nameStart) + name.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH));
						}
					}
				});
		} catch (Exception e) {
			// noinspection ThrowableNotThrown
			Skript.exception(e, "An error occurred while trying to update the list of disabled scripts!");
		}

		return options;
	}

	public static File toggleFile(File file, boolean enable) throws IOException {
		if (enable) {
			return FileUtils.move(
				file,
				new File(file.getParentFile(), file.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)),
				false
			);
		}
		return FileUtils.move(
			file,
			new File(file.getParentFile(), ScriptLoader.DISABLED_SCRIPT_PREFIX + file.getName()),
			false
		);
	}

	public static Set<File> toggleFiles(File folder, boolean enable) throws IOException {
		assert folder.isDirectory();
		FileFilter filter = enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter();

		Set<File> changed = new HashSet<>();
		//noinspection ConstantConditions - we know 'folder' is a directory
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				changed.addAll(toggleFiles(file, enable));
			} else {
				if (filter.accept(file)) {
					String fileName = file.getName();
					String newName = enable
						? fileName.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)
						: ScriptLoader.DISABLED_SCRIPT_PREFIX + fileName;

					File dest = new File(file.getParentFile(), newName);
					changed.add(FileUtils.move(file, dest, false));
				}
			}
		}

		return changed;
	}

}
