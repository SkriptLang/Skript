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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

@Name("Vectors - Vector Projection")
@Description("An expression to get the vector projection of two vectors.")
@Examples("set {_projection} to vector projection of vector(1, 2, 3) onto vector(4, 4, 4)")
@Since("2.8.0")
public class ExprVectorProjection extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorProjection.class, Vector.class, ExpressionType.COMBINED, "[vector] projection [of] %vector% on[to] %vector%");
	}

	private Expression<Vector> left, right;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.left = (Expression<Vector>) exprs[0];
		this.right = (Expression<Vector>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected Vector[] get(Event event) {
		Vector left = this.left.getOptionalSingle(event).orElse(new Vector());
		Vector right = this.right.getOptionalSingle(event).orElse(new Vector());
		double dot = left.dot(right);
		double length = right.lengthSquared();
		double scalar = dot / length;
		return new Vector[] {right.clone().multiply(scalar)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vector projection of " + left.toString(event, debug) + " onto " + right.toString(event, debug);
	}

}
