package ch.njol.skript.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
public class EffectCommandEvent extends CommandEvent implements Cancellable {

	private boolean cancelled;

	public EffectCommandEvent(CommandSender sender, String command) {
		super(sender, command, new String[0]);
	}

	public void setCommand(String command) {
		this.command = command;
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
	private final static HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
