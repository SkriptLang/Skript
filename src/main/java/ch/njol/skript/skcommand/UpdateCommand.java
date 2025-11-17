package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptUpdater;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class UpdateCommand extends SubCommand {

	public UpdateCommand() {
		super("update");
		//args("check", "changes");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		SkriptUpdater updater = Skript.getInstance().getUpdater();
		if (updater == null) { // Oh. That is bad
			Skript.info(sender, "" + SkriptUpdater.m_internal_error);
			return;
		}

		updater.updateCheck(sender);

		// TODO - add a dialog showing changes?
		//if (args[1].equalsIgnoreCase("check")) {
		//	updater.updateCheck(sender);
		//} else if (args[1].equalsIgnoreCase("changes")) {
		//	updater.changesCheck(sender);
		//}
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return List.of();
	}

}
