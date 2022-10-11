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

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author bi0qaw
 */
@Name("Vectors - Arithmetic")
@Description("Arithmetic expressions for vectors.")
@Examples({
	"set {_v} to vector 1, 2, 3 // 5",
	"set {_v} to {_v} ++ {_v}",
	"set {_v} to {_v} ++ 5",
	"set {_v} to {_v} -- {_v}",
	"set {_v} to {_v} -- 5",
	"set {_v} to {_v} ** {_v}",
	"set {_v} to {_v} ** 5",
	"set {_v} to {_v} // {_v}",
	"set {_v} to {_v} // 5"
})
@Since("2.2-dev28")
public class ExprVectorArithmetic extends SimpleExpression<Vector> {

	private enum Operator {
		PLUS("++") {
			@Override
			public Vector calculate(final Vector v1, final Vector v2) {
				return v1.clone().add(v2);
			}
		},
		MINUS("--") {
			@Override
			public Vector calculate(final Vector v1, final Vector v2) {
				return v1.clone().subtract(v2);
			}
		},
		MULT("**") {
			@Override
			public Vector calculate(final Vector v1, final Vector v2) {
				return v1.clone().multiply(v2);
			}
		},
		DIV("//") {
			@Override
			public Vector calculate(final Vector v1, final Vector v2) {
				return v1.clone().divide(v2);
			}
		};

		public final String sign;

		Operator(final String sign) {
			this.sign = sign;
		}

		public abstract Vector calculate(Vector v1, Vector v2);

		@Override
		public String toString() {
			return sign;
		}
	}

	private final static Patterns<Operator> patterns = new Patterns<>(new Object[][] {
			{"%vector%[ ]++[ ]%vector/number%", Operator.PLUS},
			{"%vector%[ ]--[ ]%vector/number%", Operator.MINUS},
			{"%vector%[ ]**[ ]%vector/number%", Operator.MULT},
			{"%vector%[ ]//[ ]%vector/number%", Operator.DIV}
	});

	static {
		Skript.registerExpression(ExprVectorArithmetic.class, Vector.class, ExpressionType.SIMPLE, patterns.getPatterns());
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Vector> first;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> second;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Operator op;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = exprs[1];
		op = patterns.getInfo(matchedPattern);
		return true;
	}

	@Override
	protected Vector[] get(Event e) {
		Vector v1 = first.getSingle(e), v2;
		Object object = this.second.getSingle(e);
		if (object instanceof Number) {
			double num = ((Number) object).doubleValue();
			v2 = new Vector(num, num, num);
		} else {
			v2 = (Vector) object;
		}
		if (v1 == null)
			v1 = new Vector();
		if (v2 == null)
			v2 = new Vector();
		return CollectionUtils.array(op.calculate(v1, v2));
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
	public String toString(@Nullable Event e, boolean debug) {
		return first.toString(e, debug) + " " + op +  " " + second.toString(e, debug);
	}

}
