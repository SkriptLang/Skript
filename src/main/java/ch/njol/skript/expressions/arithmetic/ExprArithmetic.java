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
package ch.njol.skript.expressions.arithmetic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import ch.njol.skript.util.LiteralUtils;
import org.bukkit.event.Event;
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
import org.skriptlang.skript.lang.arithmetic.Arithmetic;

/**
 * @author Peter Güttinger
 */
@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Examples({"set the player's health to 10 - the player's health",
		"loop (argument + 2) / 5 times:",
		"\tmessage \"Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%\"",
		"message \"You have %health of player * 2% half hearts of HP!\""})
@Since("1.4.2, INSERT VERSION (custom types)")
@SuppressWarnings("null")
public class ExprArithmetic extends SimpleExpression<Object> {

	private static final Class<?>[] INTEGER_CLASSES = {Long.class, Integer.class, Short.class, Byte.class};

	private static class PatternInfo {
		public final Operator operator;
		public final boolean leftGrouped;
		public final boolean rightGrouped;

		public PatternInfo(Operator operator, boolean leftGrouped, boolean rightGrouped) {
			this.operator = operator;
			this.leftGrouped = leftGrouped;
			this.rightGrouped = rightGrouped;
		}
	}

	private final static Patterns<PatternInfo> patterns = new Patterns<>(new Object[][] {

		{"\\(%object%\\)[ ]+[ ]\\(%object%\\)", new PatternInfo(Operator.PLUS, true, true)},
		{"\\(%object%\\)[ ]+[ ]%object%", new PatternInfo(Operator.PLUS, true, false)},
		{"%object%[ ]+[ ]\\(%object%\\)", new PatternInfo(Operator.PLUS, false, true)},
		{"%object%[ ]+[ ]%object%", new PatternInfo(Operator.PLUS, false, false)},

		{"\\(%object%\\)[ ]-[ ]\\(%object%\\)", new PatternInfo(Operator.MINUS, true, true)},
		{"\\(%object%\\)[ ]-[ ]%object%", new PatternInfo(Operator.MINUS, true, false)},
		{"%object%[ ]-[ ]\\(%object%\\)", new PatternInfo(Operator.MINUS, false, true)},
		{"%object%[ ]-[ ]%object%", new PatternInfo(Operator.MINUS, false, false)},

		{"\\(%object%\\)[ ]*[ ]\\(%object%\\)", new PatternInfo(Operator.MULT, true, true)},
		{"\\(%object%\\)[ ]*[ ]%object%", new PatternInfo(Operator.MULT, true, false)},
		{"%object%[ ]*[ ]\\(%object%\\)", new PatternInfo(Operator.MULT, false, true)},
		{"%object%[ ]*[ ]%object%", new PatternInfo(Operator.MULT, false, false)},

		{"\\(%object%\\)[ ]/[ ]\\(%object%\\)", new PatternInfo(Operator.DIV, true, true)},
		{"\\(%object%\\)[ ]/[ ]%object%", new PatternInfo(Operator.DIV, true, false)},
		{"%object%[ ]/[ ]\\(%object%\\)", new PatternInfo(Operator.DIV, false, true)},
		{"%object%[ ]/[ ]%object%", new PatternInfo(Operator.DIV, false, false)},

		{"\\(%object%\\)[ ]^[ ]\\(%object%\\)", new PatternInfo(Operator.EXP, true, true)},
		{"\\(%object%\\)[ ]^[ ]%object%", new PatternInfo(Operator.EXP, true, false)},
		{"%object%[ ]^[ ]\\(%object%\\)", new PatternInfo(Operator.EXP, false, true)},
		{"%object%[ ]^[ ]%object%", new PatternInfo(Operator.EXP, false, false)},

	});

	static {
		Skript.registerExpression(ExprArithmetic.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
	}

	@SuppressWarnings("null")
	private Expression<?> first, second;
	@SuppressWarnings("null")
	private Operator op;

	@SuppressWarnings("null")
	private Class<?> returnType;

	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();

	// A parsed chain, like a tree
	@Nullable
	private ArithmeticGettable<?> arithmeticGettable;

	private boolean leftGrouped, rightGrouped;

	@Override
	@SuppressWarnings({"null"})
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		first = LiteralUtils.defendExpression(exprs[0]);
		second = LiteralUtils.defendExpression(exprs[1]);

		if (!LiteralUtils.canInitSafely(first, second))
			return false;

		returnType = first.getReturnType();
		Class<?> secondClass = second.getReturnType();

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		op = patternInfo.operator;
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;

		if (Number.class.isAssignableFrom(returnType)) {
			if (op == Operator.DIV || op == Operator.EXP) {
				returnType = Double.class;
			} else {
				Class<?> firstReturnType = first.getReturnType();
				Class<?> secondReturnType = second.getReturnType();

				boolean firstIsInt = false;
				boolean secondIsInt = false;
				for (final Class<?> i : INTEGER_CLASSES) {
					firstIsInt |= i.isAssignableFrom(firstReturnType);
					secondIsInt |= i.isAssignableFrom(secondReturnType);
				}

				returnType = firstIsInt && secondIsInt ? Long.class : Double.class;
			}
		}

		Arithmetic<?> arithmetic = null;

		if (!returnType.equals(Object.class)
				&& !secondClass.equals(Object.class)
				&& (arithmetic = getArithmetic(returnType, op, secondClass)) == null) {
			ClassInfo<?> first = Classes.getSuperClassInfo(returnType);
			ClassInfo<?> second = Classes.getSuperClassInfo(secondClass);
			Skript.error(op + " can't be performed on " + first.getName().withIndefiniteArticle() + " and " + second.getName().withIndefiniteArticle());
			return false;
		}

		// Chaining
		if (first instanceof ExprArithmetic && !leftGrouped) {
			chain.addAll(((ExprArithmetic) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(op);
		if (second instanceof ExprArithmetic && !rightGrouped) {
			chain.addAll(((ExprArithmetic) second).chain);
		} else {
			chain.add(second);
		}

		if (arithmetic != null)
			arithmeticGettable = ArithmeticChain.parse(chain, arithmetic);

		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Object[] get(final Event e) {
		if (arithmeticGettable == null) {
			Object first = this.first.getSingle(e);
			Object second = this.second.getSingle(e);

			if (first == null || second == null)
				return new Object[0];

			Arithmetic<?> arithmetic = getArithmetic(first.getClass(), op, second.getClass());
			if (arithmetic == null)
				return new Object[0];

			arithmeticGettable = ArithmeticChain.parse(chain, arithmetic);
		}

		Object result = arithmeticGettable.get(e);
		arithmeticGettable = null;

		Object[] one = (Object[]) Array.newInstance(result.getClass(), 1);
		one[0] = result;
		return one;
	}

	@Nullable
	private Arithmetic<?> getArithmetic(Class<?> firstClass, Operator operator, Class<?> secondClass) {
		Arithmetic<?> arithmetic = Arithmetics.getArithmetic(firstClass);

		if (arithmetic == null || !arithmetic.acceptsOperator(operator, secondClass)) {
			return null;
		}

		return arithmetic;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		String one = first.toString(e, debug);
		String two = second.toString(e, debug);
		if (leftGrouped)
			one = '(' + one + ')';
		if (rightGrouped)
			two = '(' + two + ')';
		return one + " " + op.getSign() + " " + two;
	}

}
