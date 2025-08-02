package ch.njol.skript.skcommand;

import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HelpCommand extends SubCommand {

	public HelpCommand() {
		super("help");
	}

	// TODO
	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		sender.sendMessage("help");
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of("help");
	}

}
