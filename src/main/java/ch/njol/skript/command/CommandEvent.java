package ch.njol.skript.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class CommandEvent extends Event {

	private final CommandSender sender;
	String command;

	@Nullable
	private final String[] args;

	public CommandEvent(CommandSender sender, String command, @Nullable String[] args) {
		this.sender = sender;
		this.command = command;
		this.args = args;
	}

	public CommandSender getSender() {
		return sender;
	}

	public String getCommand() {
		return command;
	}

	@Nullable
	public String[] getArgs() {
		return args;
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
