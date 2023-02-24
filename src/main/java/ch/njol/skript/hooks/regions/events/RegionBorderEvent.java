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
package ch.njol.skript.hooks.regions.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ch.njol.skript.hooks.regions.classes.Region;
import org.jetbrains.annotations.NotNull;

/**
 * @author Peter Güttinger
 */
public class RegionBorderEvent extends Event implements Cancellable {
	
	private final Region region;
	final Player player;
	private final boolean enter;
	
	public RegionBorderEvent(final Region region, final Player player, final boolean enter) {
		this.region = region;
		this.player = player;
		this.enter = enter;
	}
	
	public boolean isEntering() {
		return enter;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	private boolean cancelled = false;
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(final boolean cancel) {
		cancelled = cancel;
	}
	
	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();
	
	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
