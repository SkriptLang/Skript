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

import java.util.Iterator;
import java.util.List;

import org.bukkit.event.Event;
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

@Name("Keep Items")
@Description({
	"Keeps the given items from dropping in a 'death of player' event.",
	"Note: this effect will only keep items that have been in the drops.",
	"Use the <a href='expressions.html#ExprKeptItems'>kept items</a> expression for items outside of the drops."
})
@Examples({
	"on death of player:",
		"\tkeep the drops"
})
@Events("death")
@RequiredPlugins("Paper (keep specific items)")
@Since("INSERT VERSION")
public class EffKeepItems extends Effect {

	static {
		Skript.registerEffect(EffKeepItems.class, "keep %itemtypes% [from ([the] drops|dropping)]");
	}

	private static final boolean SPIGOT = !Skript.methodExists(PlayerDeathEvent.class, "getItemsToKeep");
	private boolean keepAll;

	private Expression<ItemType> items;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerDeathEvent.class)) {
			Skript.error("'keep items' can only be used in a 'death of player' event.");
			return false;
		}
		keepAll = exprs[0] instanceof ExprDrops;

		if (SPIGOT && !keepAll) {
			Skript.error("Can't keep specific items in a Spigot server.");
			return false;
		}

		items = (Expression<ItemType>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof PlayerDeathEvent))
			return;

		PlayerDeathEvent e = (PlayerDeathEvent) event;
		List<ItemStack> drops = e.getDrops();

		if (keepAll) {
			e.setKeepInventory(true);
			drops.clear();
			return;
		}

		List<ItemStack> keptItems = e.getItemsToKeep();
		// We make sure the items we're keeping are part of the drops this way
		List<ItemStack> itemsToKeep = items.stream(event)
			.filter(item -> item.isContainedIn(drops))
			.map(ItemType::getRandom)
			.toList();

		keptItems.addAll(itemsToKeep);
		drops.removeAll(itemsToKeep);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "keep " + items.toString(event, debug) + " from the drops";
	}

}
