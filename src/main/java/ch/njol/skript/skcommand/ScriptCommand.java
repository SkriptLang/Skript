package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.StringUtils;
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
 * Utilities for {@link ch.njol.skript.skcommand.SkriptCommand.SubCommand}s that involve scripts.
 */
class ScriptCommand {

	private static final ArgsMessage INVALID_SCRIPT_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".invalid script");
	private static final ArgsMessage INVALID_FOLDER_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".invalid folder");

	public static @Nullable File getScriptFromArgs(CommandSender sender, String[] args) {
		return getScriptFromArgs(sender, args, Skript.getInstance().getScriptsFolder());
	}

	public static @Nullable File getScriptFromArgs(CommandSender sender, String[] args, File directoryFile) {
		String script = StringUtils.join(args, " ", 1, args.length);
		File f = ScriptLoader.getScriptFromName(script, directoryFile);
		if (f == null) {
			// Always allow '/' and '\' regardless of OS
			boolean isDirectory = script.endsWith("/") || script.endsWith("\\") || script.endsWith(File.separator);
			Skript.error(sender, (isDirectory ? INVALID_FOLDER_MESSAGE : INVALID_SCRIPT_MESSAGE).toString(script));
			return null;
		}
		return f;
	}

	/**
	 * Gets tab completions for script commands such as /sk enable|reload|disable
	 *
	 * @param args Args from tab complete event
	 * @return A list of directories and Skript files.
	 */
	public static List<String> getScriptCommandTabCompletions(String[] args) {
		List<String> options = new ArrayList<>();

		boolean useTestDirectory = args[0].equalsIgnoreCase("test") && TestMode.DEV_MODE;
		File scripts = useTestDirectory ? TestMode.TEST_DIR.toFile() : Skript.getInstance().getScriptsFolder();
		String scriptsPathString = scripts.toPath().toString();
		int scriptsPathLength = scriptsPathString.length();

		// support for scripts with spaces in them
		String scriptArg = StringUtils.join(args, " ", 1, args.length);
		String fs = File.separator;

		boolean enable = args[0].equalsIgnoreCase("enable");

		// Live update, this will get all old and new (even not loaded) scripts
		// TODO Find a better way for caching, it isn't exactly ideal to be calling this method constantly
		// TODO Make a cache based on last modified date of the 'scripts' folder?
		try (Stream<Path> files = Files.walk(scripts.toPath(), FileVisitOption.FOLLOW_LINKS)) {
			files.map(Path::toFile)
				.forEach(file -> {
					FileFilter fileFilter = enable
						? ScriptLoader.getDisabledScriptsFilter()
						: ScriptLoader.getLoadedScriptsFilter();

					if (!fileFilter.accept(file)) {
						return;
					}

					// Ignore hidden files like .git/ for users that use git source control.
					if (file.isHidden())
						return;

					String fileString = file.toString().substring(scriptsPathLength);
					if (fileString.isEmpty())
						return;

					if (file.isDirectory()) {
						fileString = fileString + fs; // Add file separator at the end of directories
					} else if (file.getParentFile().toPath().toString().equals(scriptsPathString)) {
						// Remove file separator from the beginning of files or directories in root only
						fileString = fileString.substring(1);
						if (fileString.isEmpty()) {
							return;
						}
					}

					// Make sure the user's argument matches with the file's name or beginning of file path
					if (!scriptArg.isEmpty() && !file.getName().startsWith(scriptArg) && !fileString.startsWith(scriptArg)) {
						return;
					}

					// Trim off previous arguments if needed
					if (args.length > 2 && fileString.length() >= scriptArg.length()) {
						fileString = fileString.substring(scriptArg.lastIndexOf(" ") + 1);
					}

					// Just in case
					if (fileString.isEmpty()) {
						return;
					}

					options.add(fileString);

					if (fileString.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX)) {
						options.add(fileString.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH));
					}
				});
		} catch (Exception e) {
			// noinspection ThrowableNotThrown
			Skript.exception(e, "An error occurred while trying to update the list of disabled scripts!");
		}

		// These will be added even if there are incomplete script arg
		if (args.length == 2) {
			options.add("all");
			if (args[0].equalsIgnoreCase("reload")) {
				options.add("config");
				options.add("aliases");
				options.add("scripts");
			}
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
