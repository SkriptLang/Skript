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
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Unlimited Lifetime")
@Description("Makes a dropped item have an unlimited lifetime.")
@Examples({
	"drop tripwire hook at player's location",
	"make last dropped item have unlimited lifetime"})
public class EffUnlimitedLifetime extends Effect {

	static {
		Skript.registerEffect(EffUnlimitedLifetime.class,
			"make %entities% have [an] (unlimited|infinite) life(time|span)");
	}

	private Expression<Entity> entityExpr;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entityExpr = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Entity[] entities = entityExpr.getArray(event);
		for (Entity entity : entities) {
			if (!(entity instanceof Item))
				continue;
			((Item) entity).setUnlimitedLifetime(true);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entityExpr.toString(event, debug) + " have an unlimited lifetime";
	}
}
