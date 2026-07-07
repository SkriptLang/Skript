package org.skriptlang.skript.bukkit.command.custom;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Event for executing {@link org.skriptlang.skript.bukkit.command.elements.structures.StructCommand} commands with.
 */
public class ScriptCommandEvent extends Event {

	private final String label;
	private final String rawInput;
	private final ScriptCommandExecutor commandExecutor;
	private final CommandSender sender;
	private final @Nullable Entity executor;
	private final Location location;

	final Map<String, Object> arguments = new HashMap<>();

	public ScriptCommandEvent(String label, String rawInput, ScriptCommandExecutor commandExecutor, CommandSourceStack source) {
		this.label = label;
		this.rawInput = rawInput;
		this.commandExecutor = commandExecutor;
		this.sender = source.getSender();
		this.executor = source.getExecutor();
		this.location = source.getLocation();
	}

	/**
	 * @return The label of the executed command.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return The full raw input being executed.
	 */
	public String getRawInput() {
		return rawInput;
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

	/**
	 * @return The command executor being used to execute this command (perform logic).
	 */
	public ScriptCommandExecutor getCommandExecutor() {
		return commandExecutor;
	}

	/**
	 * @return A map of all available arguments and their values.
	 */
	public Map<String, Object> getArguments() {
		return Collections.unmodifiableMap(arguments);
	}

	/**
	 * Obtains an argument by its name.
	 * @param name The name of the argument.
	 * @return The value of the argument.
	 */
	public Object getArgument(String name) {
		return arguments.get(name);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
