package org.skriptlang.skript.commands.api;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.util.Date;
import org.bukkit.event.HandlerList;

public class ScriptCommandEvent extends CommandSenderEvent {

	private final ScriptCommand scriptCommand;
	private final String label;
	private final String[] args;

	private final Date executionDate = new Date();
	private boolean cooldownCancelled;

	public ScriptCommandEvent(ScriptCommandSender sender, ScriptCommand scriptCommand, String label, String[] args) {
		super(sender);
		this.scriptCommand = scriptCommand;
		this.label = label;
		this.args = args;
	}

	public ScriptCommand getScriptCommand() {
		return scriptCommand;
	}

	public String getLabel() {
		return label;
	}

	public String[] getArgs() {
		return args;
	}

	public boolean isCooldownCancelled() {
		return cooldownCancelled;
	}

	public void setCooldownCancelled(boolean cooldownCancelled) {
		if (Delay.isDelayed(this)) {
			// If the event is delayed, we must assume the player has already been put on cooldown
			if (getSender().getType() == ScriptCommandSender.CommandSenderType.PLAYER) {
				CommandCooldown cooldown = scriptCommand.getCooldown();
				if (cooldown == null)
					return;
				if (cooldownCancelled) {
					// If the cooldown is cancelled, we should remove the cooldown from the player
					cooldown.cancelCooldown(getSender(), this);
				} else {
					// If the cooldown is uncancelled, we should retroactively put the player on cooldown
					cooldown.applyCooldown(getSender(), this, executionDate);
				}
			}
		} else {
			// If the event is not delayed, we can just set the cooldownCancelled field,
			// so it's handled at the end of the event
			this.cooldownCancelled = cooldownCancelled;
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
