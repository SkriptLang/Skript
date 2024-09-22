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
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableSet;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

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
	private Collection<Class<?>> knownReturnTypes;

	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();

	// A parsed chain, like a tree
	private ArithmeticGettable<? extends T> arithmeticGettable;

	private boolean leftGrouped, rightGrouped;

	@Override
	@SuppressWarnings({"ConstantConditions", "rawtypes", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<L>) exprs[0];
		second = (Expression<R>) exprs[1];

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;
		operator = patternInfo.operator;

		/*
		 * Step 1: UnparsedLiteral Resolving
		 *
		 * Since Arithmetic may be performed on a variety of types, it is possible that 'first' or 'second'
		 *  will represent unparsed literals. That is, the parser could not determine what their literal contents represent.
		 * Thus, it is now up to this expression to determine what they mean.
		 *
		 * If there are no unparsed literals, nothing happens at this step.
		 * If there are unparsed literals, one of three possible execution flows will occur:
		 *
	 	 * Case 1. 'first' and 'second' are unparsed literals
	 	 * In this case, there is not a lot of information to work with.
	 	 * 'first' and 'second' are attempted to be converted to fit one of all operations using 'operator'.
	 	 * If they cannot be matched with the types of a known operation, init will fail.
	 	 *
	 	 * Case 2. 'first' is an unparsed literal, 'second' is not
	 	 * In this case, 'first' needs to be converted into the "left" type of
	 	 *  any operation using 'operator' with the type of 'second' as the right type.
	 	 * If 'first' cannot be converted, init will fail.
	 	 * If no operations are found for converting 'first', init will fail, UNLESS the type of 'second' is object,
	 	 *  where operations will be searched again later with the context of the type of first.
	 	 * TODO When 'first' can represent multiple literals, it might be worth checking which of those can work with 'operator' and 'second'
	 	 *
	 	 * Case 3. 'second' is an unparsed literal, 'first' is not
	 	 * In this case, 'second' needs to be converted into the "right" type of
		 *  any operation using 'operator' with the type of 'first' as the "left" type.
		 * If 'second' cannot be converted, init will fail.
		 * If no operations are found for converting 'second', init will fail, UNLESS the type of 'first' is object,
		 *  where operations will be searched again later with the context of the type of second.
		 * TODO When 'second' can represent multiple literals, it might be worth checking which of those can work with 'first' and 'operator'
		 */

		if (first instanceof UnparsedLiteral) {
			if (second instanceof UnparsedLiteral) { // first and second need converting
				for (OperationInfo<?, ?, ?> operation : Arithmetics.getOperations(operator)) {
					// match left type with 'first'
					Expression<?> convertedFirst = first.getConvertedExpression(operation.getLeft());
					if (convertedFirst == null)
						continue;
					// match right type with 'second'
					Expression<?> convertedSecond = second.getConvertedExpression(operation.getRight());
					if (convertedSecond == null)
						continue;
					// success, set the values
					first = (Expression<L>) convertedFirst;
					second = (Expression<R>) convertedSecond;
					returnType = (Class<? extends T>) operation.getReturnType();
				}
			} else { // first needs converting
				// attempt to convert <first> to types that make valid operations with <second>
				Class<?> secondClass = second.getReturnType();
				Class[] leftTypes = Arithmetics.getOperations(operator).stream()
					.filter(info -> info.getRight().isAssignableFrom(secondClass))
					.map(OperationInfo::getLeft)
					.toArray(Class[]::new);
				if (leftTypes.length == 0) { // no known operations with second's type
					if (secondClass != Object.class) // there won't be any operations
						return error(first.getReturnType(), secondClass);
					first = (Expression<L>) first.getConvertedExpression(Object.class);
				} else {
					first = (Expression<L>) first.getConvertedExpression(leftTypes);
				}
			}
		} else if (second instanceof UnparsedLiteral) { // second needs converting
			// attempt to convert <second> to types that make valid operations with <first>
			Class<?> firstClass = first.getReturnType();
			List<? extends OperationInfo<?, ?, ?>> operations = Arithmetics.getOperations(operator, firstClass);
			if (operations.isEmpty()) { // no known operations with first's type
				if (firstClass != Object.class) // there won't be any operations
					return error(firstClass, second.getReturnType());
				second = (Expression<R>) second.getConvertedExpression(Object.class);
			} else {
				second = (Expression<R>) second.getConvertedExpression(operations.stream()
						.map(OperationInfo::getRight)
						.toArray(Class[]::new)
				);
			}
		}

		if (!LiteralUtils.canInitSafely(first, second)) // checks if there are still unparsed literals present
			return false;

		/*
		 * Step 2: Return Type Calculation
		 *
		 * After the first step, everything that can be known about 'first' and 'second' during parsing is known.
		 * As a result, it is time to determine the return type of the operation.
		 *
		 * If the types of 'first' or 'second' are object, it is possible that multiple operations with different return types
		 *  will be found. If that is the case, the supertype of these operations will be the return type (can be object).
		 * If the types of both are object (e.g. variables), the return type will be object (have to wait until runtime and hope it works).
		 * Of course, if no operations are found, init will fail.
		 *
		 * After these checks, it is safe to assume returnType has a value, as init should have failed by now if not.
		 * One final check is performed specifically for numerical types.
		 * Any numerical operation involving division or exponents have a return type of Double.
		 * Other operations will also return Double, UNLESS 'first' and 'second' are of integer types, in which case the return type will be Long.
		 *
		 * If the types of both are something meaningful, the search for a registered operation commences.
		 * If no operation can be found, init will fail.
		 */

		Class<? extends L> firstClass = first.getReturnType();
		Class<? extends R> secondClass = second.getReturnType();

		if (firstClass == Object.class || secondClass == Object.class) {
			// if either of the types is unknown, then we resolve the operation at runtime
			Class<?>[] returnTypes = null;
			if (!(firstClass == Object.class && secondClass == Object.class)) { // both aren't object
				if (firstClass == Object.class) {
					returnTypes = Arithmetics.getOperations(operator).stream()
							.filter(info -> info.getRight().isAssignableFrom(secondClass))
							.map(OperationInfo::getReturnType)
							.toArray(Class[]::new);
				} else { // secondClass is Object
					returnTypes = Arithmetics.getOperations(operator, firstClass).stream()
						.map(OperationInfo::getReturnType)
						.toArray(Class[]::new);
				}
			}
			if (returnTypes == null) { // both are object; can't determine anything
				returnType = (Class<? extends T>) Object.class;
				knownReturnTypes = Arithmetics.getAllReturnTypes(operator);
			} else if (returnTypes.length == 0) { // one of the classes is known but doesn't have any operations
				return error(firstClass, secondClass);
			} else {
				returnType = (Class<? extends T>) Classes.getSuperClassInfo(returnTypes).getC();
				knownReturnTypes = ImmutableSet.copyOf(returnTypes);
			}
		} else if (returnType == null) { // lookup
			OperationInfo<L, R, T> operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(operator, firstClass, secondClass);
			if (operationInfo == null) // we error if we couldn't find an operation between the two types
				return error(firstClass, secondClass);
			returnType = operationInfo.getReturnType();
		}

		// ensure proper return types for numerical operations
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

		/*
		 * Step 3: Chaining and Parsing
		 *
		 * This step builds the arithmetic chain that will be parsed into an ordered operation to be executed at runtime.
		 * With larger operations, it is possible that 'first' or 'second' will be instances of ExprArithmetic.
		 * As a result, their chains need to be incorporated into this instance's chain.
		 * This is to ensure that, during parsing, a "gettable" that follows the order of operations is built.
		 * However, in the case of parentheses, the chains will not be combined as the
		 *  order of operations dictates that the result of that chain be determined first.
		 *
		 * The chain (a list of values and operators) will then be parsed into a "gettable" that
		 *  can be evaluated during runtime for a final result.
		 */

		if (first instanceof ExprArithmetic && !leftGrouped) { // combine chain of 'first' if we do not have parentheses
			chain.addAll(((ExprArithmetic<?, ?, L>) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(operator);
		if (second instanceof ExprArithmetic && !rightGrouped) { // combine chain of 'second' if we do not have parentheses
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
		if (first.getC() != Object.class && second.getC() != Object.class) // errors with "object" are not very useful and often misleading
			Skript.error(operator.getName() + " can't be performed on " + first.getName().withIndefiniteArticle() + " and " + second.getName().withIndefiniteArticle());
		return false;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		if (returnType == Object.class)
			//noinspection unchecked
			return knownReturnTypes.toArray(new Class[0]);
		return super.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		if (this.returnType == Object.class && knownReturnTypes.contains(returnType))
			return true;
		return super.canReturn(returnType);
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
