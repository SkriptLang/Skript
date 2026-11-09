package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Lists all enabled and disabled scripts<p>
 * Usage: <code>/sk list</code>
 */
public class ListCommand extends SubCommand {

	ListCommand() {
		super("list", "show");
	}

	@Override
	public LiteralCommandNode<CommandSourceStack> build(String literal) {
		return Commands.literal(literal)
			.executes(context -> {
				CommandSender sender = context.getSource().getSender();
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
				return SINGLE_SUCCESS;
			})
			.build();
	}

	private static List<File> getSubFiles(File folder) {
		List<File> files = new ArrayList<>();
		if (!folder.isDirectory())
			return files;

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
