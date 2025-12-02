package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.VoidNode;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Writes all scripts in the memory to files, useful when the user accidentally deletes a script
 */
class RecoverCommand extends SubCommand {

	private static final Path DUMP_FOLDER = Skript.getInstance().getDataFolder().toPath().resolve("dump");

	public RecoverCommand() {
		super("recover", "dump");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		// Create a unique folder for each dump, don't want to overwrite old dumps in a recover command
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
		String targetFolderName = LocalDateTime.now().format(formatter);
		Path targetFolder = DUMP_FOLDER.resolve(targetFolderName);

		Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
			// TODO - lang entries
			sender.sendMessage("recovering...");
			recoverScripts(sender, targetFolder);
			sender.sendMessage("recovered " + targetFolder);
		});
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

	/**
	 * Dumps all loaded scripts to a folder. Even if the script files are deleted.
	 */
	public static void recoverScripts(CommandSender sender, Path targetFolder) {
		try {
			Files.createDirectories(targetFolder);
		} catch (IOException e) {
			// TODO - lang entries
			sender.sendMessage("error");
			// noinspection ThrowableNotThrown
			Skript.exception(e);
		}

		for (Script script : ScriptLoader.getLoadedScripts()) {
			Config config = script.getConfig();
			String name = config.getFileName();

			List<String> lines = new ArrayList<>(dumpNodes(config.getMainNode()));
			// FIXME - lazy fix for symbolic links, find a better way?
			Path filePath = targetFolder.resolve(name.replace(".." + File.separator, ""));

			try {
				Files.createDirectories(filePath.getParent());
				Files.write(filePath, lines, StandardOpenOption.CREATE);
			} catch (IOException e) {
				// TODO - lang entries
				sender.sendMessage("error");
				throw new RuntimeException("Error while recovering scripts.", e);
			}
		}
	}

	private static List<String> dumpNodes(Node mainNode) {
		String indentation = mainNode.getIndentation();
		String comment = mainNode.getComment() == null ? "" : mainNode.getComment();
		String key = mainNode.getKey() == null ? "" : mainNode.getKey();

		List<String> lines = new ArrayList<>();

		if (mainNode instanceof SectionNode sectionNode) {
			if (!key.isEmpty()) {
				lines.add((indentation + key + ": " + comment).stripTrailing());
			}
			for (@NotNull Iterator<Node> it = sectionNode.fullIterator(); it.hasNext(); ) {
				Node node = it.next();
				lines.addAll(dumpNodes(node));
			}
		} else if (mainNode instanceof VoidNode voidNode) {
			String line = voidNode.getIndentation() + voidNode.getComment();
			lines.add(line.stripTrailing());
		} else {
			String line = indentation + key + " " + comment;
			lines.add(line.stripTrailing());
		}

		return lines;
	}

}
