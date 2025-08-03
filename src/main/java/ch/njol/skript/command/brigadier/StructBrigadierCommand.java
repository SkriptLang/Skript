package ch.njol.skript.command.brigadier;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.CommandHandler;
import org.skriptlang.skript.lang.command.StructGeneralCommand;

/**
 * Brigadier command structure implementation.
 */
public class StructBrigadierCommand extends StructGeneralCommand {

	private static final BrigadierCommandHandler HANDLER = new BrigadierCommandHandler();

	static {
		StructGeneralCommand.registerCommandStructure(StructBrigadierCommand.class, "brigadier");
		Bukkit.getPluginManager().registerEvents(HANDLER, Skript.getInstance());
	}

	@Override
	public CommandHandler<CommandSender> getHandler() {
		return HANDLER;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "brigadier command /" + commandNode.getNamespace() + ":" + commandNode.getLiteral();
	}

}
