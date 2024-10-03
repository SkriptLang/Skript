package org.skriptlang.skript.commands.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class EffectCommandEvent extends CommandSenderEvent implements Cancellable {

	private final String command;

	private boolean cancelled;

	public EffectCommandEvent(ScriptCommandSender sender, String command) {
		super(sender);
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	// Bukkit stuff
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
