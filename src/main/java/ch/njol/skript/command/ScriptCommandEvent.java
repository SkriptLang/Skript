/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.command;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.util.Date;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ScriptCommandEvent extends CommandEvent {

	private final ScriptCommand skriptCommand;
	private final Date executionDate = new Date();
	private boolean cooldownCancelled;

	public ScriptCommandEvent(ScriptCommand command, CommandSender sender) {
		super(sender, command.getLabel(), null);
		skriptCommand = command;
	}

	public ScriptCommand getSkriptCommand() {
		return skriptCommand;
	}

	@Override
	public String[] getArgs() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Only accurate when this event is not delayed (yet)
	 */
	public boolean isCooldownCancelled() {
		return cooldownCancelled;
	}

	public void setCooldownCancelled(boolean cooldownCancelled) {
		if (Delay.isDelayed(this)) {
			CommandSender sender = getSender();
			if (sender instanceof Player) {
				Date date = cooldownCancelled ? null : executionDate;
				skriptCommand.setLastUsage(((Player) sender).getUniqueId(), this, date);
			}
		} else {
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
