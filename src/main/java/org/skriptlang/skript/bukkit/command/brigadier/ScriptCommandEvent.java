package org.skriptlang.skript.bukkit.command.brigadier;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Event for executing {@link org.skriptlang.skript.bukkit.command.elements.structures.StructCommand} commands with.
 */
public class ScriptCommandEvent extends Event {

	private final CommandSender sender;
	private final SkriptCommandExecutor executor;

	final Map<String, Object> arguments = new HashMap<>();

	public ScriptCommandEvent(CommandSender sender, SkriptCommandExecutor executor) {
		this.sender = sender;
		this.executor = executor;
	}

	public CommandSender getSender() {
		return sender;
	}

	public SkriptCommandExecutor getExecutor() {
		return executor;
	}

	public Object getArgument(String name) {
		return arguments.get(name);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
