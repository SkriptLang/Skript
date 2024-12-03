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
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;

@Name("Loot Context With Luck")
@Description("Returns the given loot context with the specified luck amount added to it.")
@Examples("the loot context at {_location} with luck 10")
@Since("INSERT VERSION")
public class ExprLootContextWithLuck extends PropertyExpression<LootContext, LootContext> {

	static {
		Skript.registerExpression(ExprLootContextWithLuck.class, LootContext.class, ExpressionType.PROPERTY,
			"%lootcontext% with luck %number%"
		);
	}

	private Expression<Number> luck;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<LootContext>) exprs[0]);
		luck = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	protected LootContext[] get(Event event, LootContext[] source) {
		LootContext context = getExpr().getSingle(event);
		if (context == null)
			return new LootContext[0];

		LootContext.Builder builder = new LootContext.Builder(context.getLocation());

		Number contextLuck = luck.getSingle(event);
		if (contextLuck != null)
			builder.luck(contextLuck.floatValue());

		if (context.getLootedEntity() != null)
			builder.lootedEntity(context.getLootedEntity());
		if (context.getKiller() != null)
			builder.killer(context.getKiller());

		return new LootContext[]{builder.build()};
	}

	@Override
	public Class<? extends LootContext> getReturnType() {
		return LootContext.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + " with luck " + luck.toString(event, debug);
	}
}
