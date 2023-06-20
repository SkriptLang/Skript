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

/**
 * @author Peter Güttinger
 */
@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Examples({"set the player's health to 10 - the player's health",
		"loop (argument + 2) / 5 times:",
		"\tmessage \"Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%\"",
		"message \"You have %health of player * 2% half hearts of HP!\""})
@Since("1.4.2")
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
		Skript.registerExpression(ExprArithmetic.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
	}
	
	private Expression<?> first, second;
	private Operator op;

	private Class<?> returnType;
	
	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();
	
	// A parsed chain, like a tree
	private ArithmeticGettable<?> arithmeticGettable;

	private boolean leftGrouped, rightGrouped;

	@Override
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = LiteralUtils.defendExpression(exprs[0]);
		second = LiteralUtils.defendExpression(exprs[1]);

		if (!LiteralUtils.canInitSafely(first, second))
			return false;

		Class<?> firstClass = first.getReturnType(), secondClass = second.getReturnType();

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;
		op = patternInfo.operator;
		OperationInfo<?, ?, ?> operationInfo = null;

		Expression<?> convertedLeft, convertedRight;
		boolean hasOperation = false;
		for (OperationInfo<?, ?, ?> info : Arithmetics.getOperations(op)) {
			if (!info.getLeft().isAssignableFrom(firstClass) && !info.getRight().isAssignableFrom(secondClass))
				continue;

			hasOperation = true;
			convertedLeft = first.getConvertedExpression(info.getLeft());
			convertedRight = second.getConvertedExpression(info.getRight());
			if (convertedLeft != null && convertedRight != null) {
				first = convertedLeft;
				second = convertedRight;
				operationInfo = info;
				break;
			}
		}

		if (!hasOperation && (firstClass != Object.class || secondClass != Object.class))
			return error(firstClass, secondClass);
		if (operationInfo == null && firstClass != Object.class && secondClass != Object.class)
			return error(firstClass, secondClass);

		returnType = operationInfo == null ? Object.class : operationInfo.getReturnType();

		if (Number.class.isAssignableFrom(returnType)) {
			if (op == Operator.DIVISION || op == Operator.EXPONENTIATION) {
				returnType = Double.class;
			} else {
				boolean firstIsInt = false;
				boolean secondIsInt = false;
				for (Class<?> i : INTEGER_CLASSES) {
					firstIsInt |= i.isAssignableFrom(first.getReturnType());
					secondIsInt |= i.isAssignableFrom(second.getReturnType());
				}

				returnType = firstIsInt && secondIsInt ? Long.class : Double.class;
			}
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

		arithmeticGettable = ArithmeticChain.parse(chain);
		if (arithmeticGettable == null)
			return error(firstClass, secondClass);

		return true;
	}

	@Override
	protected Object[] get(Event event) {
		Object result = arithmeticGettable.get(event);
		Object[] one = (Object[]) Array.newInstance(result == null ? returnType : result.getClass(), 1);
		one[0] = result;
		return one;
	}

	private boolean error(Class<?> firstClass, Class<?> secondClass) {
		ClassInfo<?> first = Classes.getSuperClassInfo(firstClass), second = Classes.getSuperClassInfo(secondClass);
		Skript.error(op.getName() + " can't be performed on " + first.getName().withIndefiniteArticle() + " and " + second.getName().withIndefiniteArticle());
		return false;
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
	public String toString(@Nullable Event event, boolean debug) {
		String one = first.toString(event, debug);
		String two = second.toString(event, debug);
		if (leftGrouped)
			one = '(' + one + ')';
		if (rightGrouped)
			two = '(' + two + ')';
		return one + ' ' + op + ' ' + two;
	}

	@Override
	public Expression<?> simplify() {
		if (first instanceof Literal && second instanceof Literal)
			return new SimpleLiteral<>(getArray(null), Object.class, false);
		return this;
	}

}
