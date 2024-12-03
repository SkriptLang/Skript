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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;

@Name("Loot Context")
@Description("Creates a loot context at a location. Loot contexts help determine the loot that would be dropped from a loot table.")
@Examples("the loot context at {_location}")
@Since("INSERT VERSION")
public class ExprLootContext extends SimpleExpression<LootContext> {

	static {
		Skript.registerExpression(ExprLootContext.class, LootContext.class, ExpressionType.COMBINED,
			"[the] loot[ ]context %direction% %location%"
		);
	}

	private Expression<Location> location;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		location = (Expression<Location>) exprs[1];
		if (exprs[0] != null)
			location = Direction.combine((Expression<? extends Direction>) exprs[0], location);

		return true;
	}

	@Nullable
	@Override
	protected LootContext[] get(Event event) {
		Location loc = location.getSingle(event);
		if (loc == null)
			return new LootContext[0];
		return new LootContext[]{new LootContext.Builder(loc).build()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LootContext> getReturnType() {
		return LootContext.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loot context " + location.toString(event, debug);
	}
}
