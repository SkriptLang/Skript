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

import ch.njol.skript.doc.Documentation;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
		} else if (args[0].matches("(?i)(reload|disable|enable)") && args.length >= 2) {
			File scripts = Skript.getInstance().getScriptsFolder();
			String scriptsPathString = scripts.toPath().toString();
			int scriptsPathLength = scriptsPathString.length();

			String scriptArg = StringUtils.join(args, " ", 1, args.length);
			String fs = File.separator;

			boolean enable = args[0].equalsIgnoreCase("enable");

			// Live update, this will get all old and new (even not loaded) scripts
			// TODO Find a better way for caching, it isn't exactly ideal to be calling this method constantly
			try (Stream<Path> files = Files.walk(scripts.toPath())) {
				files.map(Path::toFile)
					.forEach(file -> {
						if (!(enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter()).accept(file))
							return;

						// Ignore hidden files like .git/ for users that use git source control.
						if (file.isHidden())
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
						if (scriptArg.length() > 0 && !file.getName().startsWith(scriptArg) && !fileString.startsWith(scriptArg))
							return;

						// Trim off previous arguments if needed
						if (args.length > 2 && fileString.length() >= scriptArg.length())
							fileString = fileString.substring(scriptArg.lastIndexOf(" ") + 1);

						// Just in case
						if (fileString.isEmpty())
							return;

						options.add(fileString);
					});
			} catch (Exception e) {
				//noinspection ThrowableNotThrown
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
