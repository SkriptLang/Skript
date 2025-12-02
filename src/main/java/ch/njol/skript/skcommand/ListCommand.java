package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lists all scripts
 */
class ListCommand extends SubCommand {

	public ListCommand() {
		super("list", "show");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		SkriptCommand.info(sender, "list.enabled.header");
		ScriptLoader.getLoadedScripts().stream()
			.map(script -> script.getConfig().getFileName())
			.forEach(name -> SkriptCommand.info(sender, "list.enabled.element", name));

		SkriptCommand.info(sender, "list.disabled.header");
		ScriptLoader.getDisabledScripts().stream()
			.flatMap(file -> {
				if (file.isDirectory()) {
					return getSubFiles(file).stream();
				}
				return Arrays.stream(new File[]{file});
			})
			.map(File::getPath)
			.map(path -> path.substring(Skript.getInstance().getScriptsFolder().getPath().length() + 1))
			.forEach(path -> SkriptCommand.info(sender, "list.disabled.element", path));
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

	private static List<File> getSubFiles(File folder) {
		List<File> files = new ArrayList<>();
		if (!folder.isDirectory()) {
			return files;
		}

		// noinspection ConstantConditions - we know 'folder' is a folder
		for (File listFile : folder.listFiles(f -> !f.isHidden())) {
			if (listFile.isDirectory()) {
				files.addAll(getSubFiles(listFile));
			} else if (listFile.getName().endsWith(".sk")) {
				files.add(listFile);
			}
		}
		return files;
	}

}
