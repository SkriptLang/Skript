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
package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.EvtClick;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Cancel Event")
@Description("Cancels the event (e.g. prevent blocks from being placed, or damage being taken).")
@Examples({"on damage:",
		"	victim is a player",
		"	victim has the permission \"skript.god\"",
		"	cancel the event"})
@Since("1.0")
public class EffCancelEvent extends Effect {
	static {
		Skript.registerEffect(EffCancelEvent.class, "cancel [the] event", "uncancel [the] event");
	}
	
	private boolean cancel;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't cancel an event anymore after it has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		cancel = matchedPattern == 0;
		final Class<? extends Event>[] es = getParser().getCurrentEvents();
		if (es == null)
			return false;
		for (final Class<? extends Event> e : es) {
			if (Cancellable.class.isAssignableFrom(e) || BlockCanBuildEvent.class.isAssignableFrom(e))
				return true; // TODO warning if some event(s) cannot be cancelled even though some can (needs a way to be suppressed)
		}
		if (getParser().isCurrentEvent(PlayerLoginEvent.class))
			Skript.error("A connect event cannot be cancelled, but the player may be kicked ('kick player by reason of \"...\"')", ErrorQuality.SEMANTIC_ERROR);
		else
			Skript.error(Utils.A(getParser().getCurrentEventName()) + " event cannot be cancelled", ErrorQuality.SEMANTIC_ERROR);
		return false;
	}
	
	@Override
	public void execute(final Event e) {
		if (e instanceof Cancellable)
			((Cancellable) e).setCancelled(cancel);
		if (e instanceof PlayerInteractEvent) {
			EvtClick.interactTracker.eventModified((Cancellable) e);
			((PlayerInteractEvent) e).setUseItemInHand(cancel ? Result.DENY : Result.DEFAULT);
			((PlayerInteractEvent) e).setUseInteractedBlock(cancel ? Result.DENY : Result.DEFAULT);
		} else if (e instanceof BlockCanBuildEvent) {
			((BlockCanBuildEvent) e).setBuildable(!cancel);
		} else if (e instanceof PlayerDropItemEvent) {
			PlayerUtils.updateInventory(((PlayerDropItemEvent) e).getPlayer());
		} else if (e instanceof InventoryInteractEvent) {
			PlayerUtils.updateInventory(((Player) ((InventoryInteractEvent) e).getWhoClicked()));
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (cancel ? "" : "un") + "cancel event";
	}
	
}
