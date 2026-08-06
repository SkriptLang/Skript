package org.skriptlang.skript.bukkit.command.custom;

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

	private final CommandSender sender;
	private final @Nullable Entity executor;
	private final Location location;

	public ScriptCommandEvent(CommandSourceStack source) {
		this.sender = source.getSender();
		this.executor = source.getExecutor();
		this.location = source.getLocation();
	}

	/**
	 * @return The sender is the thing that initiated/triggered the execution of the command.
	 * It differs to {@link #getExecutor()} in that the executor can be changed by a command, e.g. {@code /execute}.
	 */
	public CommandSender getSender() {
		return sender;
	}

	/**
	 * @return The entity that executes the command.
	 * May not always be {@link #getSender()} as the executor of a command can be changed to a different entity than the one that triggered the command.
	 */
	public @Nullable Entity getExecutor() {
		return executor;
	}

	/**
	 * @return The location that the command is being executed at.
	 */
	public Location getLocation() {
		return location;
	}

	@Override
	@Contract("-> fail")
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
