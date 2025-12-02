package ch.njol.skript.skcommand;

import ch.njol.skript.command.Commands;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParseCommand extends SubCommand {

	public ParseCommand() {
		super("parse");
		args("<code>");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		args[0] = ""; // delete "parse"
		Commands.handleEffectCommand(sender, String.join(" ", args));
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

}
