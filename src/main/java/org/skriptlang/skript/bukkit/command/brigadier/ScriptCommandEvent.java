package org.skriptlang.skript.bukkit.command.brigadier;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ScriptCommandEvent extends Event {

	public final CommandSender sender;
	public final Map<ArgumentData<?>, Object> arguments = new HashMap<>();

	public ScriptCommandEvent(CommandSender sender) {
		this.sender = sender;
	}

	public CommandSender getSender() {
		return sender;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
