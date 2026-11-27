package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.command.DialogTerminal;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class TerminalCommand extends SubCommand {

	private static final boolean HAS_DIALOGS = Skript.classExists("io.papermc.paper.dialog.Dialog");

	public TerminalCommand() {
		super("terminal");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		if (!HAS_DIALOGS) {
			SkriptCommand.error(sender, "terminal.not supported");
			return;
		}
		if (!(sender instanceof Player player)) {
			SkriptCommand.error(sender, "terminal.not allowed");
			return;
		}
		DialogTerminal.attemptTerminal(player);
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

}
