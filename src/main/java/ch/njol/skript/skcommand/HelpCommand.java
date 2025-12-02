package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lists all Skript commands
 */
class HelpCommand extends SubCommand {

	public HelpCommand() {
		super("help");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		String usage = Language.get(SkriptCommand.CONFIG_NODE + ".usage");
		Skript.message(sender, usage + " <gold>/skript <red>...");

		for (SubCommand subCommand : SkriptCommand.getSubCommands()) {
			String command = String.join("/", subCommand.getAliases());
			String description = getDescription(subCommand.getAliases()[0]);
			Skript.message(sender, "   <yellow>" + command + " <dark_gray>- <white>" + description);
		}
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return SkriptCommand.getAllAliases();
	}

	private String getDescription(String command) {
		String description = Language.get_(SkriptCommand.CONFIG_NODE + ".help." + command + ".description");
		if (description == null) {
			return Language.get(SkriptCommand.CONFIG_NODE + ".help." + command);
		}
		return description;
	}

}
