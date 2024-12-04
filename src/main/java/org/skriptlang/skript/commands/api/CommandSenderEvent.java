package org.skriptlang.skript.commands.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandSenderEvent extends Event {

	private final ScriptCommandSender sender;

	public CommandSenderEvent(ScriptCommandSender sender) {
		this.sender = sender;
	}

	public ScriptCommandSender getSender() {
		return sender;
	}

	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
