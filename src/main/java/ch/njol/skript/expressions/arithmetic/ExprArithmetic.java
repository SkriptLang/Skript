package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprArgument;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.util.SimpleExpression;
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
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Example("set the player's health to 10 - the player's health")
@Example("""
    loop (argument + 2) / 5 times:
    	message "Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%"
    """)
@Example("message \"You have %health of player * 2% half hearts of HP!\"")
@Since("1.4.2")
@SuppressWarnings("null")
public class ExprArithmetic<L, R, T> extends SimpleExpression<T> {

	private static final Class<?>[] INTEGER_CLASSES = {Long.class, Integer.class, Short.class, Byte.class};

	private record PatternInfo(Operator operator, boolean leftGrouped, boolean rightGrouped) {
	}

	// initialized during registration
	private static Patterns<PatternInfo> patterns = null;

	public static void registerExpression() {
		Skript.checkAcceptRegistrations();
		List<Object[]> infos = new ArrayList<>();
		for (Operator operator : Arithmetics.getAllOperators()) {
			infos.add(new Object[] {"\\(%object%\\)[ ]" + operator.sign() + "[ ]\\(%object%\\)",
				new PatternInfo(operator, true, true)});
			infos.add(new Object[] {"\\(%object%\\)[ ]" + operator.sign() + "[ ]%object%",
				new PatternInfo(operator, true, false)});
			infos.add(new Object[] {"%object%[ ]" + operator.sign() + "[ ]\\(%object%\\)",
				new PatternInfo(operator, false, true)});
			infos.add(new Object[] {"%object%[ ]" + operator.sign() + "[ ]%object%",
				new PatternInfo(operator, false, false)});
		}
		Object[][] arr = new Object[infos.size()][];
		for (int i = 0; i < arr.length; i++)
			arr[i] = infos.get(i);
		patterns = new Patterns<>(arr);
		//noinspection unchecked
		Skript.registerExpression(ExprArithmetic.class, Object.class,
			ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
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

	private boolean leftGrouped, rightGrouped, isTopLevel;

	@Override
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<L>) exprs[0];
		second = (Expression<R>) exprs[1];

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;
		operator = patternInfo.operator;

		// check if this is the top-level arithmetic expression (not part of a larger expression)
		ParsingStack stack = getParser().getParsingStack();
		isTopLevel = stack.isEmpty() || stack.peek().getSyntaxElementClass() != ExprArithmetic.class;

		// print warning for arg-1 confusion scenario
		printArgWarning(first, second, operator);

		/*
		 * Step 1: UnparsedLiteral Resolving
		 *
		 * Since Arithmetic may be performed on a variety of types, it is possible that 'first' or 'second'
		 *  will represent unparsed literals. That is, the parser could not determine what their literal
		 *  contents represent.
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
	 	 * TODO When 'first' can represent multiple literals, it might be worth checking which of those can work
	 	 *  with 'operator' and 'second'
	 	 *
	 	 * Case 3. 'second' is an unparsed literal, 'first' is not
	 	 * In this case, 'second' needs to be converted into the "right" type of
		 *  any operation using 'operator' with the type of 'first' as the "left" type.
		 * If 'second' cannot be converted, init will fail.
		 * If no operations are found for converting 'second', init will fail, UNLESS the type of 'first' is object,
		 *  where operations will be searched again later with the context of the type of second.
		 * TODO When 'second' can represent multiple literals, it might be worth checking which of those can work
		 *  with 'first' and 'operator'
		 */

		if (first instanceof UnparsedLiteral) {
			if (second instanceof UnparsedLiteral) { // first and second need converting
				for (OperationInfo<?, ?, ?> operation : Arithmetics.getOperations(operator)) {
					// match left type with 'first'
					Expression<?> convertedFirst = first.getConvertedExpression(operation.left());
					if (convertedFirst == null)
						continue;
					// match right type with 'second'
					Expression<?> convertedSecond = second.getConvertedExpression(operation.right());
					if (convertedSecond == null)
						continue;
					// success, set the values
					first = (Expression<L>) convertedFirst;
					second = (Expression<R>) convertedSecond;
					returnType = (Class<? extends T>) operation.returnType();
				}
			} else { // first needs converting
				// attempt to convert <first> to types that make valid operations with <second>
				Class<?> secondClass = second.getReturnType();
				List<? extends OperationInfo<?, ?, ?>> operations = Arithmetics.lookupRightOperations(operator,
					secondClass);
				if (operations.isEmpty()) { // no known operations with second's type
					if (secondClass != Object.class) // there won't be any operations
						return error(first.getReturnType(), secondClass);
					first = (Expression<L>) first.getConvertedExpression(Object.class);
				} else {
					first = (Expression<L>) first.getConvertedExpression(operations.stream()
							.map(OperationInfo::left)
							.toArray(Class[]::new));
				}
			}
		} else if (second instanceof UnparsedLiteral) { // second needs converting
			// attempt to convert <second> to types that make valid operations with <first>
			Class<?> firstClass = first.getReturnType();
			List<? extends OperationInfo<?, ?, ?>> operations = Arithmetics.lookupLeftOperations(operator, firstClass);
			if (operations.isEmpty()) { // no known operations with first's type
				if (firstClass != Object.class) // there won't be any operations
					return error(firstClass, second.getReturnType());
				second = (Expression<R>) second.getConvertedExpression(Object.class);
			} else {
				second = (Expression<R>) second.getConvertedExpression(operations.stream()
						.map(OperationInfo::right)
						.toArray(Class[]::new));
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
		 * If the types of 'first' or 'second' are object, it is possible that multiple operations with
		 *  different return types will be found. If that is the case, the supertype of these operations
		 *  will be the return type (can be object).
		 * If the types of both are object (e.g. variables), the return type will be object
		 *  (have to wait until runtime and hope it works).
		 * Of course, if no operations are found, init will fail.
		 *
		 * After these checks, it is safe to assume returnType has a value, as init should have failed by now if not.
		 * One final check is performed specifically for numerical types.
		 * Any numerical operation involving division or exponents have a return type of Double.
		 * Other operations will also return Double, UNLESS 'first' and 'second' are of integer types,
		 *  in which case the return type will be Long.
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
					returnTypes = Arithmetics.lookupRightOperations(operator, secondClass).stream()
							.map(OperationInfo::returnType)
							.toArray(Class[]::new);
				} else { // secondClass is Object
					returnTypes = Arithmetics.lookupLeftOperations(operator, firstClass).stream()
							.map(OperationInfo::returnType)
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
			OperationInfo<L, R, T> operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(
				operator, firstClass, secondClass);
			if (operationInfo == null) // we error if we couldn't find an operation between the two types
				return error(firstClass, secondClass);
			returnType = operationInfo.returnType();
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
		 * This step builds the arithmetic chain that will be parsed into an ordered operation to be
		 *  executed at runtime.
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
		// combine chain of 'second' if we do not have parentheses
		if (second instanceof ExprArithmetic && !rightGrouped) {
			chain.addAll(((ExprArithmetic<?, ?, R>) second).chain);
		} else {
			chain.add(second);
		}

		arithmeticGettable = ArithmeticChain.parse(chain);
		return arithmeticGettable != null || error(firstClass, secondClass);
	}

	private void printArgWarning(Expression<L> first, Expression<R> second, Operator operator) {
		// if the operator is '-' and the user didn't use ()
		if (operator == Operator.SUBTRACTION && !rightGrouped && !leftGrouped
			// if the first expression is 'arg'
			&& first instanceof ExprArgument argument && argument.couldCauseArithmeticConfusion()
			// this ambiguity only occurs when the code is parsed as `arg - (1 * 2)` or a similar PEMDAS priority.
			&& second instanceof ExprArithmetic<?, ?, ?> secondArith && secondArith.first instanceof Literal<?> literal
			&& literal.canReturn(Number.class)) {
			// ensure that the second literal is a 1
			Literal<?> secondLiteral = (Literal<?>) LiteralUtils.defendExpression(literal);
			if (LiteralUtils.canInitSafely(secondLiteral)) {
				double number = ((Number) secondLiteral.getSingle()).doubleValue();
				if (number == 1)
					Skript.warning("This subtraction is ambiguous and could be interpreted as either the " +
						"'first argument' expression ('argument-1') or as subtraction from the argument " +
						"value ('(argument) - 1'). " +
					"If you meant to use 'argument-1', omit the hyphen ('arg 1') or use parentheses " +
						"to clarify your intent.");
			}
		}
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
		// errors with "object" are not very useful and often misleading
		if (first.getC() != Object.class && second.getC() != Object.class)
			Skript.error(operator.getName() + " can't be performed on " +
				first.getName().withIndefiniteArticle() + " and " +
				second.getName().withIndefiniteArticle());
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
	public Expression<T> simplify() {
		// simplify this expression IFF it's the top-level arithmetic expression
		if (isTopLevel)
			return simplifyInternal();
		return this;
	}

	/**
	 * Simplifies an arithmetic expression regardless of whether it is the top-level expression.
	 * @return the simplified expression
	 */
	private Expression<T> simplifyInternal() {
		if (first instanceof ExprArithmetic<?,?,?> firstArith) {
			//noinspection unchecked
			first = (Expression<L>) firstArith.simplifyInternal();
		} else {
			//noinspection unchecked
			first = (Expression<L>) first.simplify();
		}

		if (second instanceof ExprArithmetic<?,?,?> secondArith) {
			//noinspection unchecked
			second = (Expression<R>) secondArith.simplifyInternal();
		} else {
			//noinspection unchecked
			second = (Expression<R>) second.simplify();
		}

		if (first instanceof Literal && second instanceof Literal)
			return SimplifiedLiteral.fromExpression(this);

		return this;
	}

	/**
	 * For testing purposes only.
	 * @return the first expression
	 */
	Expression<L> getFirst() {
		return first;
	}

	/**
	 * For testing purposes only.
	 * @return the second expression
	 */
	Expression<R> getSecond() {
		return second;
	}

}
