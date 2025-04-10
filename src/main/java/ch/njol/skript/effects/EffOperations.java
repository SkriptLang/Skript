package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.arithmetic.ExprArithmetic;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operation;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Name("Operations")
@Description("Perform multiplication, division, or exponentiation operations on variable objects "
	+ "(i.e. numbers, vectors, timespans, and other objects from addons). "
	+ "Literals cannot be used on the left-hand side.")
@Example("""
	set {_num} to 1
	multiply {_num} by 10
	divide {_num} by 5
	raise {_num} to the power of 2
	""")
@Example("""
	set {_nums::*} to 15, 21 and 30
	divide {_nums::*} by 3
	multiply {_nums::*} by 5
	raise {_nums::*} to the power of 3
	""")
@Example("""
	set {_vector} to vector(1,1,1)
	multiply {_vector} by vector(4,8,16)
	divide {_vector} by 2
	""")
@Example("""
	set {_timespan} to 1 hour
	multiply {_timespan} by 3
	""")
@Example("""
	# Will error due to literal
	multiply 1 by 2
	divide 10 by {_num}
	""")
@Since("INSERT VERSION")
public class EffOperations extends Effect implements SyntaxRuntimeErrorProducer {

	private static final Patterns<Operator> PATTERNS = new Patterns<>(new Object[][]{
		{"multiply %~objects% by %object%", Operator.MULTIPLICATION},
		{"divide %~objects% by %object%", Operator.DIVISION},
		{"raise %~objects% to [the] (power|exponent) [of] %object%", Operator.EXPONENTIATION}
	});

	static {
		Skript.registerEffect(EffOperations.class, PATTERNS.getPatterns());
	}

	private Operator operator;
	private Expression<?> left;
	private Class<?>[] leftAccepts;
	private Expression<?> right;
	private Node node;
	private Operation<Object, Object, Object> operation = null;
	private OperationInfo<?, ?, ?> operationInfo;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		operator = PATTERNS.getInfo(matchedPattern);
		node = getParser().getNode();
		left = exprs[0];
		right = LiteralUtils.defendExpression(exprs[1]);

		leftAccepts = left.acceptChange(ChangeMode.SET);
		// Ensure 'left' is changeable
		if (leftAccepts == null) {
			Skript.error("'" + left + "' cannot be set to anything and therefore cannot be " + getOperatorName() + ".");
			return false;
		} else if (leftAccepts.length == 0) {
			throw new IllegalStateException("An expression should never return an empty array for a ChangeMode of 'SET'");
		}
		// Ensure the accepted classes of 'left' are non-array classes
		for (int i = 0; i < leftAccepts.length; i++) {
			if (leftAccepts[i].isArray()) {
				leftAccepts[i] = leftAccepts[i].getComponentType();
			}
		}

		Class<?> leftType = left.getReturnType();
		Class<?> rightType = right.getReturnType();

		if (leftType.isArray())
			leftType = leftType.getComponentType();

		if (leftType.equals(Object.class) && rightType.equals(Object.class)) {
			// 'left' and 'right' return 'Object.class' thus making operation checks non-applicable
			// However, we can check to make sure any of the registered operations return types are applicable
			// 		for 'left's acceptedClasses
			Class<?>[] allReturnTypes = Arithmetics.getAllReturnTypes(operator).toArray(Class[]::new);
			if (!ChangerUtils.acceptsChangeTypes(leftAccepts, allReturnTypes)) {
				Skript.error(left + " cannot be " + getOperatorName() + ".");
				return false;
			}
			return LiteralUtils.canInitSafely(right);
		} else if (leftType.equals(Object.class) || rightType.equals(Object.class)) {
			Class<?>[] returnTypes;
			if (leftType.equals(Object.class)) {
				returnTypes = Arithmetics.getOperations(operator).stream()
					.filter(info -> info.getRight().isAssignableFrom(rightType))
					.map(OperationInfo::getReturnType)
					.toArray(Class[]::new);
			} else {
				returnTypes = Arithmetics.getOperations(operator, leftType).stream()
					.map(OperationInfo::getReturnType)
					.toArray(Class[]::new);
			}

			if (returnTypes.length == 0) {
				noOperationError(left, leftType, rightType);
				return false;
			}
			if (!ChangerUtils.acceptsChangeTypes(leftAccepts, returnTypes)) {
				genericParseError(left, rightType);
				return false;
			}
		} else {
			operationInfo = Arithmetics.lookupOperationInfo(operator, leftType, rightType, leftAccepts);
			if (operationInfo == null || !ChangerUtils.acceptsChangeTypes(leftAccepts, operationInfo.getReturnType())) {
				genericParseError(left, rightType);
				return false;
			}
		}
		return LiteralUtils.canInitSafely(right);
	}

	@Override
	protected void execute(Event event) {
		Object rightObject = right.getSingle(event);
		if (rightObject == null)
			return;

		Class<?> rightType = rightObject.getClass();

		Map<Class<?>, Operation<Object, Object, ?>> cachedOperations = new HashMap<>();
		Set<Class<?>> invalidTypes = new HashSet<>();

		Function<?, ?> changerFunction = (leftInput) -> {
			Class<?> leftType = leftInput.getClass();
			if (invalidTypes.contains(leftType)) {
				printArithmeticError(leftType, rightType);
				return leftInput;
			}
			Operation<Object, Object, ?> operation = cachedOperations.get(leftType);
			if (operation == null) {
				//noinspection unchecked
				OperationInfo<Object, Object, ?> operationInfo = (OperationInfo<Object, Object, ?>) Arithmetics.lookupOperationInfo(operator, leftType, rightType, leftAccepts);
				if (operationInfo == null) {
					printArithmeticError(leftType, rightType);
					invalidTypes.add(leftType);
					return leftInput;
				}
				operation = operationInfo.getOperation();
				cachedOperations.put(leftType, operation);
			}
			return operation.calculate(leftInput, rightObject);
		};
		//noinspection unchecked,rawtypes
		left.changeInPlace(event, (Function) changerFunction);
	}

	@Override
	public Node getNode() {
		return node;
	}

	private void printArithmeticError(Class<?> left, Class<?> right) {
		String error = ExprArithmetic.getArithmeticErrorMessage(operator, left, right);
		if (error != null)
			error(error);
	}

	private void genericParseError(Expression<?> leftExpr, Class<?> rightType) {
		Skript.error("'" + leftExpr + "' cannot be " + getOperatorName() + " by "
			+ Classes.getSuperClassInfo(rightType).getName().withIndefiniteArticle() + ".");
	}

	private void noOperationError(Expression<?> leftExpr, Class<?> leftType, Class<?> rightType) {
		String error = ExprArithmetic.getArithmeticErrorMessage(operator, leftType, rightType);
		if (error != null) {
			Skript.error(error);
		} else {
			genericParseError(leftExpr, rightType);
		}
	}

	private String getOperatorName() {
		return switch (operator) {
			case MULTIPLICATION -> "multiplied";
			case DIVISION -> "divided";
			case EXPONENTIATION -> "exponentiated";
			default -> "";
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		switch (operator) {
			case MULTIPLICATION -> builder.append("multiply", left, "by");
			case DIVISION -> builder.append("divide", left, "by");
			case EXPONENTIATION -> builder.append("raise", left, "to the power of");
		}
		builder.append(right);
		return builder.toString();
	}

}
