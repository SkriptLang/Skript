package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.command.CommandEvent;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.SkriptCommandSender;

/**
 * Event for executing brigadier commands.
 */
public class BrigadierCommandEvent extends CommandEvent {

	private final CommandContext<SkriptCommandSender> context;

	private static @Nullable CommandSender getBukkit(SkriptCommandSender skriptCommandSender) {
		return skriptCommandSender instanceof BukkitCommandSender bcs ? bcs.wrapped() : null;
	}

	public BrigadierCommandEvent(CommandContext<SkriptCommandSender> context) {
		super(getBukkit(context.getSource()), context.getRootNode().getName(),
			context.getNodes().stream()
				.map(arg -> arg.getRange().get(context.getInput()))
				.toArray(String[]::new));
		this.context = context;
	}

	/**
	 * @return command execution context
	 */
	public CommandContext<SkriptCommandSender> getContext() {
		return context;
	}

	/**
	 * @deprecated for compatibility reasons with the old command systems
	 */
	@Override
	@Deprecated(forRemoval = true)
	public @Nullable CommandSender getSender() {
		return super.getSender();
	}

	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
