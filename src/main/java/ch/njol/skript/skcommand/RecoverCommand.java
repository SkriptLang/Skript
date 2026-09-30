package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.njol.skript.skcommand.SkriptCommand.info;

/**
 * Writes all scripts in the memory to files, useful when the user accidentally deletes a script<p>
 * Symbolic links that point outside the scripts folder are saved in {@code dump/external/} instead<p>
 * Usage: <code>/sk recover</code>
 */
class RecoverCommand extends SubCommand {

	private static final Path DUMP_FOLDER = Skript.getInstance().getDataFolder().toPath().resolve("dump");

	public RecoverCommand() {
		super("recover", "dump");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		// Create a unique folder for each dump, don't want to overwrite old dumps in a recover command
		// The format looks like '18 July 2026 20-34-20'
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH-mm-ss");
		String targetFolderName = LocalDateTime.now().format(formatter);
		Path targetFolder = DUMP_FOLDER.resolve(targetFolderName);

		Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
			info(sender, "recover.recovering");
			recoverScripts(sender, targetFolder);
			info(sender, "recover.recovered", targetFolder);
		});
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

	/**
	 * Dumps all loaded scripts to a folder. Even if the script files are deleted.
	 */
	private static void recoverScripts(CommandSender sender, Path targetFolder) {
		try {
			Files.createDirectories(targetFolder);
		} catch (IOException e) {
			info(sender, "recover.io error", e.getMessage());
			// noinspection ThrowableNotThrown
			Skript.exception(e, "Error while recovering scripts.");
		}

		for (Script script : ScriptLoader.getLoadedScripts()) {
			Config config = script.getConfig();
			String name = config.getFileName();
			Path filePath = targetFolder.resolve(
				// Replaces all instances of ../../ with a single external/
				name.replaceAll(
					"(?:\\.\\." + Pattern.quote(File.separator) + ")+",
					Matcher.quoteReplacement("external" + File.separator)
				)
			);

			try {
				Files.createDirectories(filePath.getParent());
				Files.createFile(filePath);
				config.save(filePath.toFile());
			} catch (IOException e) {
				info(sender, "recover.io error", e.getMessage());
				// noinspection ThrowableNotThrown
				Skript.exception(e, "Error while recovering scripts.");
			}
		}
	}

}
