package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operation;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Priority;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a chain of arithmetic operations between two operands.
 *
 * @param <L> the type of the left operand
 * @param <R> the type of the right operand
 * @param <T> the return type of the operation
 */
public class ArithmeticChain<L, R, T> implements ArithmeticGettable<T> {

	// lazily initialized
	private static List<Set<Operator>> operatorGroups = null;

	private final ArithmeticGettable<L> left;
	private final ArithmeticGettable<R> right;
	private final Operator operator;
	private final Class<? extends T> returnType;
	private final @Nullable OperationInfo<? extends L, ? extends R, ? extends T> operationInfo;

	// the last runtime lookup, used when the operation couldn't be resolved at parse time
	private @Nullable CachedLookup lastLookup;

	private record CachedLookup(Class<?> leftClass, Class<?> rightClass, boolean leftAnchor, boolean rightAnchor,
	                            @Nullable OperationInfo<?, ?, ?> operationInfo) {

		boolean matches(Class<?> leftClass, Class<?> rightClass, boolean leftAnchor, boolean rightAnchor) {
			return this.leftClass == leftClass && this.rightClass == rightClass
				&& this.leftAnchor == leftAnchor && this.rightAnchor == rightAnchor;
		}

	}

	@SuppressWarnings("unchecked")
	public ArithmeticChain(ArithmeticGettable<L> left, Operator operator,
						   ArithmeticGettable<R> right,
						   @Nullable OperationInfo<L, R, T> operationInfo) {
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.operationInfo = operationInfo;
		this.returnType = operationInfo != null ? operationInfo.returnType() : (Class<? extends T>) Object.class;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public T get(Event event) {
		L left = this.left.get(event);
		if (left == null && this.left instanceof ArithmeticChain)
			return null;

		R right = this.right.get(event);
		if (right == null && this.right instanceof ArithmeticChain)
			return null;

		Class<? extends L> leftClass = left != null
			? (Class<? extends L>) left.getClass()
			: this.left.getReturnType();
		Class<? extends R> rightClass = right != null
			? (Class<? extends R>) right.getClass()
			: this.right.getReturnType();

		if (leftClass == Object.class && rightClass == Object.class)
			return null;

		OperationInfo<? extends L, ? extends R, ? extends T> operationInfo = this.operationInfo;
		boolean leftAnchor = left == null && leftClass == Object.class;
		boolean rightAnchor = right == null && rightClass == Object.class;
		if (operationInfo == null || leftAnchor || rightAnchor) {
			// the operand types are almost always the same between evaluations, so remember the last lookup
			CachedLookup cached = lastLookup;
			if (cached != null && cached.matches(leftClass, rightClass, leftAnchor, rightAnchor)) {
				operationInfo = (OperationInfo<? extends L, ? extends R, ? extends T>) cached.operationInfo();
			} else {
				operationInfo = resolveOperationInfo(leftClass, rightClass, leftAnchor, rightAnchor);
				lastLookup = new CachedLookup(leftClass, rightClass, leftAnchor, rightAnchor, operationInfo);
			}
		}

		if (operationInfo == null)
			return null;

		left = left != null ? left : Arithmetics.getDefaultValue(operationInfo.left());
		if (left == null)
			return null;
		right = right != null ? right : Arithmetics.getDefaultValue(operationInfo.right());
		if (right == null)
			return null;

		return ((Operation<L, R, T>) operationInfo.operation()).calculate(left, right);
	}

	private @Nullable OperationInfo<? extends L, ? extends R, ? extends T> resolveOperationInfo(
		Class<? extends L> leftClass, Class<? extends R> rightClass, boolean leftAnchor, boolean rightAnchor) {
		if (leftAnchor)
			return lookupOperationInfo(rightClass, OperationInfo::right);
		if (rightAnchor)
			return lookupOperationInfo(leftClass, OperationInfo::left);
		return Arithmetics.lookupOperationInfo(operator, leftClass, rightClass, returnType);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private OperationInfo<L, R, T> lookupOperationInfo(Class<?> anchor,
													   Function<OperationInfo<?, ?, ?>,
													   Class<?>> anchorFunction) {
		OperationInfo<?, ?, ?> operationInfo = Arithmetics.lookupOperationInfo(operator, anchor, anchor);
		if (operationInfo != null)
			return (OperationInfo<L, R, T>) operationInfo;

		return (OperationInfo<L, R, T>) Arithmetics.getOperations(operator).stream()
			.filter(info -> anchorFunction.apply(info).isAssignableFrom(anchor))
			.filter(info -> Converters.converterExists(info.returnType(), returnType))
			.reduce((info, info2) -> {
				if (anchorFunction.apply(info2) == anchor)
					return info2;
				return info;
			})
			.orElse(null);
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	/**
	 * Initializes the {@code operatorGroups}.
	 * <p>
	 * Each group is an unordered set of operators with the same priority.
	 * Together the groups are stored in a list in reverse priority order:
	 * e.g. (+, -) -> (*, /) -> (^)
	 * <p>
	 * In the same order the arithmetic chain is then evaluated in the {@link ArithmeticChain#parse(List)} method.
	 */
	private static void createOperatorGroups() {
		if (operatorGroups != null)
			throw new RuntimeException("Operator groups have already been created");
		operatorGroups = new LinkedList<>();
		List<Operator> operators = new LinkedList<>(Arithmetics.getAllOperators());
		Collections.reverse(operators);

		if (operators.isEmpty()) {
			return;
		}

		Set<Operator> currentGroup = new HashSet<>();
		Priority currentGroupPriority = operators.get(0).priority();

		for (Operator operator : operators) {
			if (operator.priority().compareTo(currentGroupPriority) != 0) {
				if (!currentGroup.isEmpty()) {
					operatorGroups.add(currentGroup);
				}
				currentGroup = new HashSet<>();
				currentGroupPriority = operator.priority();
			}
			currentGroup.add(operator);
		}

		operatorGroups.add(currentGroup);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R, T> ArithmeticGettable<T> parse(List<?> chain) {
		if (operatorGroups == null)
			createOperatorGroups();

		for (Set<?> group : operatorGroups) {
			int lastIndex = Utils.findLastIndex(chain, group::contains);
			if (lastIndex == -1)
				continue;

			ArithmeticGettable<L> left = parse(chain.subList(0, lastIndex));

			Operator operator = (Operator) chain.get(lastIndex);

			ArithmeticGettable<R> right = parse(chain.subList(lastIndex + 1, chain.size()));

			if (left == null || right == null)
				return null;

			OperationInfo<L, R, T> operationInfo = null;
			if (left.getReturnType() != Object.class && right.getReturnType() != Object.class) {
				operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(
					operator, left.getReturnType(), right.getReturnType());
				if (operationInfo == null)
					return null;
			}

			return new ArithmeticChain<>(left, operator, right, operationInfo);
		}

		if (chain.size() != 1)
			throw new IllegalStateException();

		return new ArithmeticExpressionInfo<>((Expression<T>) chain.get(0));
	}

}
