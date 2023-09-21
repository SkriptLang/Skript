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

import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
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

@Name("Vectors - Arithmetic")
@Description("Arithmetic expressions for vectors. This expression is deprecated in INSERT VERSION and above")
@Examples({
	"set {_v} to vector 1, 2, 3 // 5",
	"set {_v} to {_v} ++ {_v}",
	"set {_v} to {_v} -- {_v}",
	"set {_v} to {_v} ** {_v}",
	"set {_v} to {_v} // {_v}"
})
@Since("2.2-dev28, INSERT VERSION (deprecation)")
@Deprecated
public class ExprVectorArithmetic extends SimpleExpression<Vector> {

	private final static Patterns<Operator> patterns = new Patterns<>(new Object[][] {
			{"%vector%[ ]++[ ]%vector%", Operator.ADDITION},
			{"%vector%[ ]--[ ]%vector%", Operator.SUBTRACTION},
			{"%vector%[ ]**[ ]%vector%", Operator.MULTIPLICATION},
			{"%vector%[ ]//[ ]%vector%", Operator.DIVISION}
	});

	static {
		Skript.registerExpression(ExprVectorArithmetic.class, Vector.class, ExpressionType.SIMPLE, patterns.getPatterns());
	}

	private Expression<Vector> first, second;
	private Operator op;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		op = patterns.getInfo(matchedPattern);
		Skript.warning("This expression was deprecated in favor of the arithmetic expression, and will be removed in the future." +
			" Please use that instead. E.g. 'vector(2, 4, 1) + vector(5, 2, 3)'");
		return true;
	}

	@Override
	protected Vector[] get(Event event) {
		Vector v1 = first.getSingle(event), v2 = second.getSingle(event);
		if (v1 == null)
			v1 = new Vector();
		if (v2 == null)
			v2 = new Vector();
		return new Vector[] {Arithmetics.calculate(op, v1, v2, Vector.class)};
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
		return first.toString(event, debug) + " " + op +  " " + second.toString(event, debug);
	}

}
