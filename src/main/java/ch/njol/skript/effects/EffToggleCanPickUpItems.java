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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Toggle Picking Up Items")
@Description("Determines whether living entities are able to pick up items or not")
@Examples({
	"forbid player from picking up items",
	"send \"You can no longer pick up items!\" to player",
	"",
	"on drop:",
		"\tif player can't pick	up items:",
			"\t\tallow player to pick up items"
})
@Since("2.8.0")
public class EffToggleCanPickUpItems extends Effect {

	static {
		Skript.registerEffect(EffToggleCanPickUpItems.class,
				"allow %livingentities% to pick([ ]up items| items up)",
				"(forbid|disallow) %livingentities% (from|to) pick([ing | ]up items|[ing] items up)");
	}

	private Expression<LivingEntity> entities;
	private boolean allowPickUp;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		allowPickUp = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setCanPickupItems(allowPickUp);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allowPickUp) {
			return "allow " + entities.toString(event, debug) + " to pick up items";
		} else {
			return "forbid " + entities.toString(event, debug) + " from picking up items";
		}
	}

}
