package ch.njol.skript;

import ch.njol.skript.doc.Documentation;
import ch.njol.skript.test.runner.TestMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SkriptCommandTabCompleter implements TabCompleter {

	@Override
	@Nullable
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> options = new ArrayList<>();
		
		if (!command.getName().equalsIgnoreCase("skript"))
			return null;
		
		if (args[0].equalsIgnoreCase("update") && args.length == 2) {
			options.add("check");
			options.add("changes");
		} else if (args[0].matches("(?i)(reload|disable|enable|test)") && args.length >= 2) {
			boolean useTestDirectory = args[0].equalsIgnoreCase("test") && TestMode.DEV_MODE;
			File scripts = useTestDirectory ? TestMode.TEST_DIR.toFile() : Skript.getInstance().getScriptsFolder();
			String scriptsPathString = scripts.toPath().toString();
			int scriptsPathLength = scriptsPathString.length();

			String lastArg = args[args.length - 1];
			String fs = File.separator;

			boolean enable = args[0].equalsIgnoreCase("enable");

			// Live update, this will get all old and new (even not loaded) scripts
			// TODO Find a better way for caching, it isn't exactly ideal to be calling this method constantly
			if (args.length == 2 || !args[1].matches("(?i)(all|scripts|aliases|config|lastReloaded)")) {
				String[] filteredArgs = Arrays.copyOfRange(args, 1, args.length);
				List<String> separatedArgs = SkriptCommand.separateCommaArguments(true, false, filteredArgs);
				String currentScript = !separatedArgs.isEmpty() ? separatedArgs.get(0) : "";
				try (Stream<Path> files = Files.walk(scripts.toPath())) {
					files.map(Path::toFile)
						.forEach(file -> {
							if (!(enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter()).accept(file))
								return;

							// Ignore hidden files like .git/ for users that use git source control.
							if (file.isHidden())
								return;

							if (!enable && SkriptCommand.isScriptDisabled(file))
								return;

							String fileString = file.toString().substring(scriptsPathLength);
							if (fileString.isEmpty())
								return;

							if (file.isDirectory()) {
								fileString = fileString + fs; // Add file separator at the end of directories
							} else if (file.getParentFile().toPath().toString().equals(scriptsPathString)) {
								fileString = fileString.substring(1); // Remove file separator from the beginning of files or directories in root only
								if (fileString.isEmpty())
									return;
							}

							// Make sure the user's argument matches with the file's name or beginning of file path
							if (!currentScript.isEmpty() && !file.getName().startsWith(currentScript) && !fileString.startsWith(currentScript))
								return;

							// Trim off previous arguments if needed
							if (args.length > 2 && fileString.length() >= currentScript.length())
								fileString = fileString.substring(currentScript.lastIndexOf(" ") + 1);

							// Just in case
							if (fileString.isEmpty())
								return;

							// Need to remove files that are already listed or included in a directory
							if (args[0].matches("(?i)(reload|disable|test)") && args.length > 2 && !separatedArgs.isEmpty()) {
								for (String current : separatedArgs) {
									if (
										(current.endsWith("\\") && fileString.contains(current))
										|| fileString.equals(current)
										|| (!current.endsWith(".sk") && fileString.equals(current + ".sk"))
									) {
										return;
									}
								}
							}

							fileString += ",";
							options.add(fileString);
						});
					if (lastArg.isEmpty() && options.isEmpty()) {
						options.add(",");
					}
				} catch (Exception e) {
					//noinspection ThrowableNotThrown
					Skript.exception(e, "An error occurred while trying to update the list of disabled scripts!");
				}
			}
			
			// These will be added even if there are incomplete script arg
			if (args.length == 2) {
				options.add("all");
				if (args[0].equalsIgnoreCase("reload")) {
					options.add("config");
					options.add("aliases");
					options.add("scripts");
					options.add("lastReloaded");
				}
			}

		} else if (args.length == 1) {
			options.add("help");
			options.add("reload");
			options.add("enable");
			options.add("disable");
			options.add("update");
			options.add("list");
			options.add("show");
			options.add("info");
			if (Documentation.getDocsTemplateDirectory().exists())
				options.add("gen-docs");
			if (TestMode.DEV_MODE)
				options.add("test");
		}
		
		return options;
	}

}
