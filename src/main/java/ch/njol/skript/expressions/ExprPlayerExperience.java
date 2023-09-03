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

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Player's experience")
@Description("Represents the player's total experience or experience of level")
@Examples({"send player's experience", "send player's total experience"})
@Since("INSERT")

public class ExprPlayerExperience extends PropertyExpression<Player, Float> {

	static {
		PropertyExpression.register(ExprPlayerExperience.class, Float.class, "[:total] (exp|experience)", "players");
	}

	private boolean isTotal;
	@Override
	protected Float[] get(Event event, Player[] source) {
		if (source.length < 1) return new Float[0];
		Player p = source[0];
		float result = isTotal ? p.getTotalExperience() : p.getExp();
		return new Float[]{result};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "experience of player " + getExpr().toString(e, debug);
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		isTotal = parseResult.hasTag("total");
		return true;
	}
}