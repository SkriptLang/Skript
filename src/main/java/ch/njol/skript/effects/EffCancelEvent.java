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
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Cancel Event")
@Description("Cancels the event (e.g. prevent blocks from being placed, or damage being taken).")
@Examples({
	"on damage:",
		"\tvictim is a player",
		"\tvictim has the permission \"skript.god\"",
		"\tcancel the event"
})
@Since("1.0")
public class EffCancelEvent extends Effect {

	static {
		Skript.registerEffect(EffCancelEvent.class, "cancel [the] event", "uncancel [the] event");
	}

	private boolean cancel;

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (getParser().isCurrentEvent(PlayerLoginEvent.class)) {
			Skript.error("A connect event cannot be cancelled, but the player may be kicked ('kick player by reason of \"...\"')");
			return false;
		}
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't cancel an event anymore after it has already passed");
			return false;
		}
		cancel = matchedPattern == 0;
		Class<? extends Event>[] currentEvents = getParser().getCurrentEvents();
		if (currentEvents == null)
			return false;

		int cancellable = 0;
		for (Class<? extends Event> event : currentEvents) {
			if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event))
				cancellable++;
		}
		// All events are cancellable.
		if (cancellable == currentEvents.length)
			return true;
		// Some events are cancellable.
		if (cancellable > 0) {
			if (!getParser().getCurrentScript().suppressesWarning(ScriptWarning.EVENT_CANNOT_BE_CANCELLED))
				Skript.warning(Utils.A(getParser().getCurrentEventName()) + " can be called by multiple events, and some cannot be cancelled.");
			return true;
		}
		// No events are cancellable.
		Skript.error(Utils.A(getParser().getCurrentEventName()) + " event cannot be cancelled");
		return false;
	}

	@Override
	public void execute(Event event) {
		if (event instanceof Cancellable)
			((Cancellable) event).setCancelled(cancel);
		if (event instanceof PlayerInteractEvent) {
			EvtClick.interactTracker.eventModified((Cancellable) event);
			((PlayerInteractEvent) event).setUseItemInHand(cancel ? Result.DENY : Result.DEFAULT);
			((PlayerInteractEvent) event).setUseInteractedBlock(cancel ? Result.DENY : Result.DEFAULT);
		} else if (event instanceof BlockCanBuildEvent) {
			((BlockCanBuildEvent) event).setBuildable(!cancel);
		} else if (event instanceof PlayerDropItemEvent) {
			PlayerUtils.updateInventory(((PlayerDropItemEvent) event).getPlayer());
		} else if (event instanceof InventoryInteractEvent) {
			PlayerUtils.updateInventory(((Player) ((InventoryInteractEvent) event).getWhoClicked()));
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (cancel ? "" : "un") + "cancel event";
	}

}
