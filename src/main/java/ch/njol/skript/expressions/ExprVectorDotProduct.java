/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Dot Product")
@Description("Gets the dot product between two vectors.")
@Examples({"set {_v} to {_v2} dot {_v3}"})
@Since("2.2-dev28")
/**
 * NOTE vector 1, 2, 3 dot vector 1, 2, 3 does NOT work!
 * it returns a new vector: 1, 2, 18. This should not happen
 * and I have no idea why it does. I have also no idea why
 * "z" takes the value 18. There must be some black magic
 * going on.
 */
public class ExprVectorDotProduct extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprVectorDotProduct.class, Number.class, ExpressionType.SIMPLE, "%vector% dot %vector%");
	}

	@SuppressWarnings("null")
	private Expression<Vector> first, second;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Number[] get(Event e) {
		Vector v1 = first.getSingle(e);
		Vector v2 = second.getSingle(e);
		if (v1 == null || v2 == null)
			return null;
		return CollectionUtils.array(v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return first.toString(e, debug) + " dot " + second.toString(e, debug);
	}

}
