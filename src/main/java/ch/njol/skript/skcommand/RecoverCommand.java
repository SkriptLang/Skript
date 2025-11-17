package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
		Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
			sender.sendMessage("recovering...");
			recoverScripts(sender);
			sender.sendMessage("recovered");
		});
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

	/**
	 * Dumps all loaded scripts to a folder. Even if the script files are deleted.
	 */
	public static void recoverScripts(CommandSender sender) {
		try {
			Files.createDirectories(DUMP_FOLDER);
		} catch (IOException e) {
			// TODO
			sender.sendMessage("error");
			// noinspection ThrowableNotThrown
			Skript.exception(e);
		}

		for (Script script : ScriptLoader.getLoadedScripts()) {
			Config config = script.getConfig();
			String name = config.getFileName();

			List<String> lines = new ArrayList<>();
			for (Node node : config.getMainNode()) {
				lines.addAll(loopNodes(node));
				lines.add("");
			}
			Path filePath = DUMP_FOLDER.resolve(name);
			try {
				Files.createDirectories(filePath.getParent());
				Files.write(filePath, lines, StandardOpenOption.CREATE);
			} catch (IOException e) {
				sender.sendMessage("error");
				throw new RuntimeException("Error while recovering scripts.", e);
			}
		}
	}

	private static List<String> loopNodes(Node mainNode) {
		String indentation = mainNode.getIndentation();
		String comment = mainNode.getComment();
		String key = mainNode.getKey() == null ? "" : mainNode.getKey();

		List<String> lines = new ArrayList<>();

		if (mainNode instanceof SectionNode sectionNode) {
			if (!key.isEmpty()) {
				lines.add(indentation + key + ": " + comment);
			}
			for (Node node : sectionNode) {
				lines.addAll(loopNodes(node));
			}
		} else {
			lines.add(indentation + key + comment);
		}

		return lines;
	}

}
