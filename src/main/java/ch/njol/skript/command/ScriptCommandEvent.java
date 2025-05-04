package ch.njol.skript.command;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.util.Date;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ScriptCommandEvent extends CommandEvent {
	
	private final ScriptCommand scriptCommand;
	private final String commandLabel;
	private final String rest;
	private final Date executionDate = new Date();
	private boolean cooldownCancelled;

	/**
	 * @param scriptCommand The script command executed.
	 * @param sender The executor of this script command.
	 * @param commandLabel The command name (may be the used alias)
	 * @param rest The rest of the command string (the arguments)
	 */
	public ScriptCommandEvent(ScriptCommand scriptCommand, CommandSender sender, String commandLabel, String rest) {
		super(sender, scriptCommand.getLabel(), rest.split(" "));
		this.scriptCommand = scriptCommand;
		this.commandLabel = commandLabel;
		this.rest = rest;
	}

	/**
	 * @return The script command executed.
	 */
	public ScriptCommand getScriptCommand() {
		return scriptCommand;
	}

	/**
	 * @return The used command label. This may be a command alias.
	 */
	public String getCommandLabel() {
		return commandLabel;
	}

	/**
	 * @return The arguments combined into one string.
	 * @see CommandEvent#getArgs()
	 */
	public String getArgsString() {
		return rest;
	}

	/**
	 * Only accurate when this event is not delayed (yet)
	 */
	public boolean isCooldownCancelled() {
		return cooldownCancelled;
	}

	/**
	 * @deprecated Use {@link #setCooldownCancelled(boolean, boolean)} instead.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public void setCooldownCancelled(boolean cooldownCancelled) {
		//noinspection removal
		setCooldownCancelled(cooldownCancelled, Delay.isDelayed(this));
	}

	/**
	 * Sets whether the cooldown should be cancelled.
	 *
	 * @param cooldownCancelled Whether the cooldown should be cancelled.
	 * @param delayed Whether this event is delayed. If true, the sender's cooldown will be retroactively set.
	 */
	public void setCooldownCancelled(boolean cooldownCancelled, boolean delayed) {
		this.cooldownCancelled = cooldownCancelled;
		if (delayed && getSender() instanceof Player player) {
			Date date = cooldownCancelled ? null : executionDate;
			scriptCommand.setLastUsage(player.getUniqueId(), this, date);
		}
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
