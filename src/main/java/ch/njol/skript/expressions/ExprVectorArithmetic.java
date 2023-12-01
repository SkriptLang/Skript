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

@Name("Vectors - Arithmetic")
@Description("Arithmetic expressions for vectors.")
@Examples({
	"set {_v} to vector 1, 2, 3 // vector 5, 5, 5",
	"set {_v} to {_v} ++ {_v}",
	"set {_v} to {_v} -- {_v}",
	"set {_v} to {_v} ** {_v}",
	"set {_v} to {_v} // {_v}"
})
@Since("2.2-dev28")
public class ExprVectorArithmetic extends SimpleExpression<Vector> {

	private enum Operator {
		PLUS("++") {
			@Override
			public Vector calculate(final Vector first, final Vector second) {
				return first.clone().add(second);
			}
		},
		MINUS("--") {
			@Override
			public Vector calculate(final Vector first, final Vector second) {
				return first.clone().subtract(second);
			}
		},
		MULT("**") {
			@Override
			public Vector calculate(final Vector first, final Vector second) {
				return first.clone().multiply(second);
			}
		},
		DIV("//") {
			@Override
			public Vector calculate(final Vector first, final Vector second) {
				return first.clone().divide(second);
			}
		};

		public final String sign;

		Operator(final String sign) {
			this.sign = sign;
		}

		public abstract Vector calculate(Vector first, Vector second);

		@Override
		public String toString() {
			return sign;
		}
	}

	private final static Patterns<Operator> patterns = new Patterns<>(new Object[][] {
			{"%vector%[ ]++[ ]%vector%", Operator.PLUS},
			{"%vector%[ ]--[ ]%vector%", Operator.MINUS},
			{"%vector%[ ]**[ ]%vector%", Operator.MULT},
			{"%vector%[ ]//[ ]%vector%", Operator.DIV}
	});

	static {
		Skript.registerExpression(ExprVectorArithmetic.class, Vector.class, ExpressionType.SIMPLE, patterns.getPatterns());
	}

	@SuppressWarnings("null")
	private Expression<Vector> first, second;

	@SuppressWarnings("null")
	private Operator operator;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		operator = patterns.getInfo(matchedPattern);
		return true;
	}

	@Override
	protected Vector[] get(Event event) {
		Vector first = this.first.getOptionalSingle(event).orElse(new Vector());
		Vector second = this.second.getOptionalSingle(event).orElse(new Vector());
		return CollectionUtils.array(operator.calculate(first, second));
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
		return first.toString(event, debug) + " " + operator +  " " + second.toString(event, debug);
	}

}
