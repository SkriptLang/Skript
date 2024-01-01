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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Examples({"set the player's health to 10 - the player's health",
	"loop (argument + 2) / 5 times:",
	"\tmessage \"Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%\"",
	"message \"You have %health of player * 2% half hearts of HP!\""})
@Since("1.4.2")
@SuppressWarnings("null")
public class ExprArithmetic<L, R, T> extends SimpleExpression<T> {

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

	private static final Patterns<PatternInfo> patterns = new Patterns<>(new Object[][] {

		{"\\(%object%\\)[ ]+[ ]\\(%object%\\)", new PatternInfo(Operator.ADDITION, true, true)},
		{"\\(%object%\\)[ ]+[ ]%object%", new PatternInfo(Operator.ADDITION, true, false)},
		{"%object%[ ]+[ ]\\(%object%\\)", new PatternInfo(Operator.ADDITION, false, true)},
		{"%object%[ ]+[ ]%object%", new PatternInfo(Operator.ADDITION, false, false)},

		{"\\(%object%\\)[ ]-[ ]\\(%object%\\)", new PatternInfo(Operator.SUBTRACTION, true, true)},
		{"\\(%object%\\)[ ]-[ ]%object%", new PatternInfo(Operator.SUBTRACTION, true, false)},
		{"%object%[ ]-[ ]\\(%object%\\)", new PatternInfo(Operator.SUBTRACTION, false, true)},
		{"%object%[ ]-[ ]%object%", new PatternInfo(Operator.SUBTRACTION, false, false)},

		{"\\(%object%\\)[ ]*[ ]\\(%object%\\)", new PatternInfo(Operator.MULTIPLICATION, true, true)},
		{"\\(%object%\\)[ ]*[ ]%object%", new PatternInfo(Operator.MULTIPLICATION, true, false)},
		{"%object%[ ]*[ ]\\(%object%\\)", new PatternInfo(Operator.MULTIPLICATION, false, true)},
		{"%object%[ ]*[ ]%object%", new PatternInfo(Operator.MULTIPLICATION, false, false)},

		{"\\(%object%\\)[ ]/[ ]\\(%object%\\)", new PatternInfo(Operator.DIVISION, true, true)},
		{"\\(%object%\\)[ ]/[ ]%object%", new PatternInfo(Operator.DIVISION, true, false)},
		{"%object%[ ]/[ ]\\(%object%\\)", new PatternInfo(Operator.DIVISION, false, true)},
		{"%object%[ ]/[ ]%object%", new PatternInfo(Operator.DIVISION, false, false)},

		{"\\(%object%\\)[ ]^[ ]\\(%object%\\)", new PatternInfo(Operator.EXPONENTIATION, true, true)},
		{"\\(%object%\\)[ ]^[ ]%object%", new PatternInfo(Operator.EXPONENTIATION, true, false)},
		{"%object%[ ]^[ ]\\(%object%\\)", new PatternInfo(Operator.EXPONENTIATION, false, true)},
		{"%object%[ ]^[ ]%object%", new PatternInfo(Operator.EXPONENTIATION, false, false)},

	});

	static {
		//noinspection unchecked
		Skript.registerExpression(ExprArithmetic.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
	}

	private Expression<L> first;
	private Expression<R> second;
	private Operator operator;

	private Class<? extends T> returnType;

	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();

	// A parsed chain, like a tree
	private ArithmeticGettable<? extends T> arithmeticGettable;

	private boolean leftGrouped, rightGrouped;

	@Override
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = LiteralUtils.defendExpression(exprs[0]);
		second = LiteralUtils.defendExpression(exprs[1]);

		if (!LiteralUtils.canInitSafely(first, second))
			return false;

		Class<? extends L> firstClass = first.getReturnType();
		Class<? extends R> secondClass = second.getReturnType();

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;
		operator = patternInfo.operator;

		if (firstClass != Object.class && secondClass == Object.class && Arithmetics.getOperations(operator, firstClass).isEmpty()) {
			// If the first class is known but doesn't have any operations, then we fail
			return error(firstClass, secondClass);
		} else if (firstClass == Object.class && secondClass != Object.class && Arithmetics.getOperations(operator).stream()
				.noneMatch(info -> info.getRight().isAssignableFrom(secondClass))) {
			// If the second class is known but doesn't have any operations, then we fail
			return error(firstClass, secondClass);
		}

		OperationInfo<L, R, T> operationInfo;
		if (firstClass == Object.class || secondClass == Object.class) {
			// If either of the types is unknown, then we resolve the operation at runtime
			operationInfo = null;
		} else {
			operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(operator, firstClass, secondClass);
			if (operationInfo == null) // We error if we couldn't find an operation between the two types
				return error(firstClass, secondClass);
		}

		returnType = operationInfo == null ? (Class<? extends T>) Object.class : operationInfo.getReturnType();

		if (Number.class.isAssignableFrom(returnType)) {
			if (operator == Operator.DIVISION || operator == Operator.EXPONENTIATION) {
				returnType = (Class<? extends T>) Double.class;
			} else {
				boolean firstIsInt = false;
				boolean secondIsInt = false;
				for (Class<?> i : INTEGER_CLASSES) {
					firstIsInt |= i.isAssignableFrom(first.getReturnType());
					secondIsInt |= i.isAssignableFrom(second.getReturnType());
				}

				returnType = (Class<? extends T>) (firstIsInt && secondIsInt ? Long.class : Double.class);
			}
		}

		// Chaining
		if (first instanceof ExprArithmetic && !leftGrouped) {
			chain.addAll(((ExprArithmetic<?, ?, L>) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(operator);
		if (second instanceof ExprArithmetic && !rightGrouped) {
			chain.addAll(((ExprArithmetic<?, ?, R>) second).chain);
		} else {
			chain.add(second);
		}

		arithmeticGettable = ArithmeticChain.parse(chain);
		return arithmeticGettable != null || error(firstClass, secondClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T[] get(Event event) {
		T result = arithmeticGettable.get(event);
		T[] one = (T[]) Array.newInstance(result == null ? returnType : result.getClass(), 1);
		one[0] = result;
		return one;
	}

	private boolean error(Class<?> firstClass, Class<?> secondClass) {
		ClassInfo<?> first = Classes.getSuperClassInfo(firstClass), second = Classes.getSuperClassInfo(secondClass);
		Skript.error(operator.getName() + " can't be performed on " + first.getName().withIndefiniteArticle() + " and " + second.getName().withIndefiniteArticle());
		return false;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String one = first.toString(event, debug);
		String two = second.toString(event, debug);
		if (leftGrouped)
			one = '(' + one + ')';
		if (rightGrouped)
			two = '(' + two + ')';
		return one + ' ' + operator + ' ' + two;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Expression<? extends T> simplify() {
		if (first instanceof Literal && second instanceof Literal)
			return new SimpleLiteral<>(getArray(null), (Class<T>) getReturnType(), false);
		return this;
	}

}
