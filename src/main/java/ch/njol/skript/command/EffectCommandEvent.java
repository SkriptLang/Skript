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

import ch.njol.skript.lang.Effect;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * Called whenever an effect command attempt is done.
 */
public class EffectCommandEvent extends CommandEvent implements Cancellable {

	private boolean cancelled;
	@Nullable
	private final Effect effect;

	public EffectCommandEvent(CommandSender sender, String command, @Nullable Effect effect) {
		super(sender, command, new String[0]);
		this.effect = effect;
	}

	/**
	 * @see EffectCommandEvent#EffectCommandEvent(CommandSender, String, Effect)
	 */
	@Deprecated
	public EffectCommandEvent(CommandSender sender, String command) {
		this(sender, command, null);
    }

	/**
	 * @return the parsed effect, if valid
	 */
	@Nullable
	public Effect getEffect() {
		return effect;
	}

	@Deprecated
	public void setCommand(String command) {
		throw new UnsupportedOperationException();
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
