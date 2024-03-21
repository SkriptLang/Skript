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

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprDrops;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Keep Inventory / Experience")
@Description({
	"Keeps the inventory or/and experiences of the dead player in a death event.",
	"Note: keeping specific items only works in Paper 1.15+"
})
@Examples({
	"on death of a player:",
		"\tif the victim is an op:",
			"\t\tkeep the inventory and experiences",
		"\telse:",
			"\t\tkeep the swords"
})
@Since("2.4, INSERT VERSION")
@RequiredPlugins("Paper (keep specific items)")
@Events("death")
public class EffKeepInventory extends Effect {

	static {
		if (Skript.methodExists(PlayerDeathEvent.class, "getItemsToKeep")) {
			Skript.registerEffect(EffKeepInventory.class,
					"keep [the] (inventory|items) [(1:and [e]xp[erience][s] [point[s]])]",
					"keep [the] [e]xp[erience][s] [point[s]] [(1:and (inventory|items))]",
					"keep [the] %itemtypes% [from ([the] drops|dropping)]");
		} else {
			Skript.registerEffect(EffKeepInventory.class,
					"keep [the] (inventory|items) [(1:and [e]xp[erience][s] [point[s]])]",
					"keep [the] [e]xp[erience][s] [point[s]] [(1:and (inventory|items))]");
		}
	}

	private boolean keepItems, keepExp;

	@Nullable
	private Expression<ItemType> itemsToKeep;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemsToKeep = matchedPattern == 2 ? (Expression<ItemType>) exprs[0] : null;
		keepItems = matchedPattern == 0 || parseResult.mark == 1 || itemsToKeep instanceof ExprDrops;
		keepExp = matchedPattern == 1 || parseResult.mark == 1;

		if (!getParser().isCurrentEvent(EntityDeathEvent.class)) {
			Skript.error("The keep inventory/experience effect can't be used outside of a death event");
			return false;
		}
		if (isDelayed.isTrue()) {
			Skript.error("Can't keep the inventory/experience anymore after the event has already passed");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof PlayerDeathEvent) {
			PlayerDeathEvent deathEvent = (PlayerDeathEvent) event;
			if (keepItems)
				deathEvent.setKeepInventory(true);
			if (keepExp)
				deathEvent.setKeepLevel(true);

			if (itemsToKeep != null && !keepItems) {
				List<ItemStack> drops = deathEvent.getDrops();
				itemsToKeep.stream(deathEvent)
						.filter(item -> item.isContainedIn(drops))
						.forEach(item -> {
							item.removeFrom(drops);
							item.addTo(deathEvent.getItemsToKeep());
						});
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (itemsToKeep != null)
			return "keep " + itemsToKeep.toString(event, debug);
		if (keepItems && !keepExp)
			return "keep the inventory";
		else
			return "keep the experience" + (keepItems ? " and inventory" : "");
	}

}
