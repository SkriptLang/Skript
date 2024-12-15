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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Keep Inventory / Experience")
@Description("Keeps the inventory or/and experiences of the dead player in a death event.")
@Examples({
	"on death of a player:",
		"\tif the victim is an op:",
			"\t\tkeep the inventory and experiences"
})
@Since("2.4")
@Events("death")
public class EffKeepInventory extends Effect {

	static {
		Skript.registerEffect(EffKeepInventory.class,
			"keep [the] (inventory|items) [(1:and [e]xp[erience][s] [point[s]])]",
			"keep [the] [e]xp[erience][s] [point[s]] [(1:and (inventory|items))]");
	}

	private boolean keepItems, keepExp;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		keepItems = matchedPattern == 0 || parseResult.mark == 1;
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
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (keepItems && !keepExp)
			return "keep the inventory";
		else
			return "keep the experience" + (keepItems ? " and inventory" : "");
	}

}
