package org.skriptlang.skript.bukkit.command.elements;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.PaperCommandHandler;
import org.skriptlang.skript.lang.command.CommandHandler;
import org.skriptlang.skript.lang.command.SkriptCommandSender;
import org.skriptlang.skript.lang.command.StructGeneralCommand;

/**
 * Brigadier command structure implementation.
 */
public class StructBrigadierCommand extends StructGeneralCommand {

	private static final PaperCommandHandler HANDLER = new PaperCommandHandler();

	static {
		StructGeneralCommand.registerCommandStructure(StructBrigadierCommand.class, "brigadier");
		Bukkit.getPluginManager().registerEvents(HANDLER, Skript.getInstance());
	}

	@Override
	@SuppressWarnings("unchecked")
	public CommandHandler<SkriptCommandSender> getHandler() {
		return (CommandHandler<SkriptCommandSender>) (CommandHandler<?>) HANDLER;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "brigadier command /" + commandNode.getNamespace() + ":" + commandNode.getLiteral();
	}

}
