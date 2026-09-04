package org.skriptlang.skript.bukkit.command.custom;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parent event for all {@link ScriptBrigadierCommand} events/contexts.
 */
public class ScriptCommandEvent extends Event {

	private final CommandContext<CommandSourceStack> context;

	public ScriptCommandEvent(CommandContext<CommandSourceStack> context) {
		this.context = context;
	}

	/**
	 * @return All context surrounding the command execution.
	 */
	public CommandContext<CommandSourceStack> getContext() {
		return context;
	}

	/**
	 * @return The sender is the thing that initiated/triggered the execution of the command.
	 * It differs to {@link #getExecutor()} in that the executor can be changed by a command, e.g. {@code /execute}.
	 */
	public CommandSender getSender() {
		return context.getSource().getSender();
	}

	/**
	 * @return The entity that executes the command.
	 * May not always be {@link #getSender()} as the executor of a command can be changed to a different entity than the one that triggered the command.
	 */
	public @Nullable Entity getExecutor() {
		return context.getSource().getExecutor();
	}

	/**
	 * @return The location that the command is being executed at.
	 */
	public Location getLocation() {
		return context.getSource().getLocation();
	}

	@Override
	@Contract("-> fail")
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
