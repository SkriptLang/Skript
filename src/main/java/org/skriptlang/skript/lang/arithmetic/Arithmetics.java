package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for managing arithmetic operations.
 */
public final class Arithmetics {

	private static final Map<Operator, List<OperationInfo<?, ?, ?>>> OPERATIONS
		= Collections.synchronizedMap(new HashMap<>());
	private static final Map<Operator, Map<OperandTypes, OperationInfo<?, ?, ?>>> CACHED_OPERATIONS
		= Collections.synchronizedMap(new HashMap<>());
	private static final Map<Operator, Map<OperandTypes, OperationInfo<?, ?, ?>>> CACHED_CONVERTED_OPERATIONS
		= Collections.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, DifferenceInfo<?, ?>> DIFFERENCES
		= Collections.synchronizedMap(new HashMap<>());
	private static final Map<Class<?>, DifferenceInfo<?, ?>> CACHED_DIFFERENCES
		= Collections.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, Supplier<?>> DEFAULT_VALUES
		= Collections.synchronizedMap(new HashMap<>());
	private static final Map<Class<?>, Supplier<?>> CACHED_DEFAULT_VALUES
		= Collections.synchronizedMap(new HashMap<>());

	/**
	 * Registers a binary operation where both left and right operands are of the same type,
	 * and the return type is also the same as the operand type.
	 *
	 * @param operator the {@link Operator} representing this operation
	 * @param type the type of both operands and the return type
	 * @param operation the {@link Operation} that performs the calculation
	 * @param <T> the type of both operands and the return type
	 */
	public static <T> void registerOperation(Operator operator, Class<T> type, Operation<T, T, T> operation) {
		registerOperation(operator, type, type, type, operation);
	}

	/**
	 * Registers a binary operation where the return type is the same as the left operand's type.
	 *
	 * @param operator the {@link Operator} representing this operation
	 * @param leftClass the type of left operands and the return type
	 * @param rightClass the type of right operand
	 * @param operation the {@link Operation} that performs the calculation
	 * @param <L> the type of left operands and the return type
	 * @param <R> the type of right operand
	 */
	public static <L, R> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass,
			Operation<L, R, L> operation) {
		registerOperation(operator, leftClass, rightClass, leftClass, operation);
	}

	/**
	 * Registers a binary operation and its commutative counterpart, where the return type
	 * is the same as the left operand's type for the primary operation.
	 *
	 * @param operator The {@link Operator} representing this operation
	 * @param leftClass the type of left operands and the return type
	 * @param rightClass the type of right operand
	 * @param operation the {@link Operation} that performs the calculation
	 * @param commutativeOperation the counterpart operation
	 * @param <L> the type of left operands and the return type
	 * @param <R> the type of right operand
	 */
	public static <L, R> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass,
			Operation<L, R, L> operation, Operation<R, L, L> commutativeOperation) {
		registerOperation(operator, leftClass, rightClass, leftClass, operation);
		registerOperation(operator, rightClass, leftClass, leftClass, commutativeOperation);
	}

	/**
	 * Registers a binary operation and its commutative counterpart.
	 *
	 * @param operator The {@link Operator} representing this operation
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @param returnType return type
	 * @param operation the {@link Operation} that performs the calculation
	 * @param commutativeOperation the counterpart operation
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	public static <L, R, T> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass,
			Class<T> returnType, Operation<L, R, T> operation, Operation<R, L, T> commutativeOperation) {
		registerOperation(operator, leftClass, rightClass, returnType, operation);
		registerOperation(operator, rightClass, leftClass, returnType, commutativeOperation);
	}


	/**
	 * Registers a binary operation.
	 *
	 * @param operator The {@link Operator} representing this operation
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @param returnType return type
	 * @param operation the {@link Operation} that performs the calculation
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	public static <L, R, T> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass,
			Class<T> returnType, Operation<L, R, T> operation) {
		Skript.checkAcceptRegistrations();
		if (exactOperationExists(operator, leftClass, rightClass))
			throw new SkriptAPIException("There's already a " + operator.getName() +
				" operation registered for types '" + leftClass.getName() + "' and '" +
				rightClass.getName() + "'");
		getRawOperations(operator).add(new OperationInfo<>(leftClass, rightClass, returnType, operation));
	}

	/**
	 * Checks if an operation with the exact specified left and right operand types
	 * is already registered.
	 *
	 * @param operator the operator of the operation
	 * @param leftClass the exact type of the left operand
	 * @param rightClass the exact type of the right operand
	 * @return {@code true} if an exact match is found, else {@code false}
	 */
	public static boolean exactOperationExists(Operator operator, Class<?> leftClass, Class<?> rightClass) {
		for (OperationInfo<?, ?, ?> info : getRawOperations(operator)) {
			if (info.left() == leftClass && info.right() == rightClass)
				return true;
		}
		return false;
	}

	/**
	 * Checks if an operation exists, considering type conversions.
	 *
	 * @param operator the operator of the operation
	 * @param leftClass the type of the left operand
	 * @param rightClass the type of the right operand
	 * @return {@code true} if suitable operation is found, else {@code false}
	 * @see #getOperationInfo(Operator, Class, Class)
	 */
	public static boolean operationExists(Operator operator, Class<?> leftClass, Class<?> rightClass) {
		return getOperationInfo(operator, leftClass, rightClass) != null;
	}

	private static List<OperationInfo<?, ?, ?>> getRawOperations(Operator operator) {
		return OPERATIONS.computeIfAbsent(operator, o -> Collections.synchronizedList(new ArrayList<>()));
	}

	/**
	 * Returns view of all operation infos instances registered for a specific {@link Operator}.
	 *
	 * @param operator the operator
	 * @return all registered operations for the given operator
	 */
	public static @UnmodifiableView List<OperationInfo<?, ?, ?>> getOperations(Operator operator) {
		return Collections.unmodifiableList(getRawOperations(operator));
	}

	/**
	 * Returns a list of operation infos for a given operator,
	 * where the left operand type is assignable from the specified {@code type}.
	 * This does not consider type conversions, but does consider type assignability.
	 *
	 * @param operator the operator
	 * @param type left operand type
	 * @return all registered operations for the given operator and type
	 * @param <T> the type to filter by for the left operand
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> @Unmodifiable List<OperationInfo<T, ?, ?>> getOperations(Operator operator, Class<T> type) {
		return (List) getOperations(operator).stream()
			.filter(info -> info.left().isAssignableFrom(type))
			.collect(Collectors.toList());
	}

	/**
	 * Returns all valid operations from {@code operator} and {@code leftClass}.
	 * Unlike {@link #getOperations(Operator, Class)}, this method considers converters.
	 *
	 * @param operator the operator
	 * @param leftClass left operand type
	 * @return list containing all valid operations from {@code operator} and {@code leftClass}.
	 * @param <L> the type to filter by for the left operand
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <L> @Unmodifiable List<OperationInfo<L, ?, ?>> lookupLeftOperations(Operator operator,
			Class<L> leftClass) {
		return (List) getOperations(operator).stream()
			.map(info -> {
				if (info.left().isAssignableFrom(leftClass)) {
					return info;
				}
				return info.getConverted(leftClass, info.right(), info.returnType());
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	/**
	 * Returns all valid operations from {@code operator} and {@code rightClass}.
	 * This method considers converters and type assignability.
	 *
	 * @param operator the operator
	 * @param rightClass right operand type
	 * @return list containing all valid operations from {@code operator} and {@code rightClass}.
	 * @param <R> the type to filter by for the right operand
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <R> @Unmodifiable List<OperationInfo<?, R, ?>> lookupRightOperations(Operator operator,
			Class<R> rightClass) {
		return (List) getOperations(operator).stream()
			.map(info -> {
				if (info.right().isAssignableFrom(rightClass)) {
					return info;
				}
				return info.getConverted(info.left(), rightClass, info.returnType());
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieves an {@link OperationInfo} for a specific operator and operand types.
	 * This method considers type assignability, but does not consider converters.
	 *
	 * @param operator the operator
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @param returnType return type
	 * @return suitable operation info
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	@SuppressWarnings("unchecked")
	public static <L, R, T> @Nullable OperationInfo<L, R, T> getOperationInfo(Operator operator,
			Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
		OperationInfo<L, R, ?> info = getOperationInfo(operator, leftClass, rightClass);
		if (info != null && returnType.isAssignableFrom(info.returnType()))
			return (OperationInfo<L, R, T>) info;
		return null;
	}

	/**
	 * Retrieves an {@link OperationInfo} for a specific operator and operand types.
	 * This method considers type assignability, but does not consider converters.
	 *
	 * @param operator the operator
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @return suitable operation info
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 */
	@SuppressWarnings("unchecked")
	public static <L, R> @Nullable OperationInfo<L, R, ?> getOperationInfo(Operator operator,
			Class<L> leftClass, Class<R> rightClass) {
		assertIsOperationsDoneLoading();
		OperandTypes operandTypes = new OperandTypes(leftClass, rightClass);
		Map<OperandTypes, OperationInfo<?, ?, ?>> operations = CACHED_OPERATIONS
			.computeIfAbsent(operator, o -> Collections.synchronizedMap(new HashMap<>()));
		OperationInfo<L, R, ?> operationInfo = (OperationInfo<L, R, ?>) operations.get(operandTypes);
		// we also cache null values for non-existing operations
		if (operations.containsKey(operandTypes))
			return operationInfo;

		operationInfo = (OperationInfo<L, R, ?>) getOperations(operator).stream()
			.filter(info ->
				info.left().isAssignableFrom(leftClass) && info.right().isAssignableFrom(rightClass))
			.reduce((info, info2) -> {
				if (info2.left() == leftClass && info2.right() == rightClass)
					return info2;
				return info;
			})
			.orElse(null);
		operations.put(operandTypes, operationInfo);
		return operationInfo;
	}

	/**
	 * Retrieves an {@link Operation} for a specific operator and operand types.
	 * This method considers type assignability, but does not consider converters.
	 *
	 * @param operator the operator
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @param returnType return type
	 * @return suitable operation
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	public static <L, R, T> @Nullable Operation<L, R, T> getOperation(Operator operator,
			Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
		OperationInfo<L, R, T> info = getOperationInfo(operator, leftClass, rightClass, returnType);
		return info == null ? null : info.operation();
	}

	/**
	 * Retrieves an {@link Operation} for a specific operator and operand types.
	 * This method considers type assignability, but does not consider converters.
	 *
	 * @param operator the operator
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @return suitable operation
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 */
	public static <L, R> @Nullable Operation<L, R, ?> getOperation(Operator operator,
			Class<L> leftClass, Class<R> rightClass) {
		OperationInfo<L, R, ?> info = getOperationInfo(operator, leftClass, rightClass);
		return info == null ? null : info.operation();
	}

	/**
	 * Retrieves an {@link OperationInfo} for a specific operator and operand types.
	 * This method considers type assignability and converters.
	 *
	 * @param operator the operator
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @param returnType return type
	 * @return suitable operation
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	public static <L, R, T> @Nullable OperationInfo<L, R, T> lookupOperationInfo(Operator operator,
			Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
		OperationInfo<L, R, ?> info = lookupOperationInfo(operator, leftClass, rightClass);
		return info != null ? info.getConverted(leftClass, rightClass, returnType) : null;
	}

	/**
	 * Retrieves an {@link OperationInfo} for a specific operator and operand types.
	 * This method considers type assignability and converters.
	 *
	 * @param operator the operator
	 * @param leftClass the type of left operand
	 * @param rightClass the type of right operand
	 * @return suitable operation
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 */
	@SuppressWarnings("unchecked")
	public static <L, R> @Nullable OperationInfo<L, R, ?> lookupOperationInfo(Operator operator,
			Class<L> leftClass, Class<R> rightClass) {
		OperationInfo<L, R, ?> operationInfo = getOperationInfo(operator, leftClass, rightClass);
		if (operationInfo != null)
			return operationInfo;

		OperandTypes operandTypes = new OperandTypes(leftClass, rightClass);
		Map<OperandTypes, OperationInfo<?, ?, ?>> operations = CACHED_CONVERTED_OPERATIONS
			.computeIfAbsent(operator, o -> Collections.synchronizedMap(new HashMap<>()));
		operationInfo = (OperationInfo<L, R, ?>) operations.get(operandTypes);
		// we also cache null values for non-existing operations
		if (operations.containsKey(operandTypes))
			return operationInfo;

		for (OperationInfo<?, ?, ?> info : getOperations(operator)) {
			OperationInfo<L, R, ?> convertedInfo = info.getConverted(
				leftClass, rightClass, info.returnType());
			if (convertedInfo == null)
				continue;
			operations.put(operandTypes, convertedInfo);
			return convertedInfo;
		}

		operations.put(operandTypes, null);
		return null;
	}

	/**
	 * Calculates the result of an operation.
	 *
	 * @param operator operator to apply
	 * @param left left operand
	 * @param right right operand
	 * @param returnType return type
	 * @return the calculated result
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	@SuppressWarnings("unchecked")
	public static <L, R, T> @Nullable T calculate(Operator operator, L left, R right, Class<T> returnType) {
		Operation<L, R, T> operation = (Operation<L, R, T>) getOperation(
			operator, left.getClass(), right.getClass(), returnType);
		return operation == null ? null : operation.calculate(left, right);
	}

	/**
	 * Calculates the result of an operation.
	 * <p>
	 * This method does not verify the operation result type.
	 *
	 * @param operator operator to apply
	 * @param left left operand
	 * @param right right operand
	 * @return the calculated result
	 * @param <L> the type of left operand
	 * @param <R> the type of right operand
	 * @param <T> return type
	 */
	@SuppressWarnings("unchecked")
	public static <L, R, T> @Nullable T calculateUnsafe(Operator operator, L left, R right) {
		Operation<L, R, T> operation = (Operation<L, R, T>) getOperation(
			operator, left.getClass(), right.getClass());
		return operation == null ? null : operation.calculate(left, right);
	}

	/**
	 * Registers a difference operation.
	 *
	 * @param type the type of the operands and result
	 * @param operation the operation for calculating the difference
	 * @param <T> the type of the operands and result
	 *
	 * @see DifferenceInfo
	 */
	public static <T> void registerDifference(Class<T> type, Operation<T, T, T> operation) {
		registerDifference(type, type, operation);
	}

	/**
	 * Registers a difference operation.
	 *
	 * @param type the type of the operands
	 * @param returnType type of the result
	 * @param operation the operation for calculating the difference
	 * @param <T> the type of the operands and result
	 *
	 * @see DifferenceInfo
	 */
	public static <T, R> void registerDifference(Class<T> type, Class<R> returnType, Operation<T, T, R> operation) {
		Skript.checkAcceptRegistrations();
		if (exactDifferenceExists(type))
			throw new SkriptAPIException("There's already a difference registered for type '" + type + "'");
		DIFFERENCES.put(type, new DifferenceInfo<>(type, returnType, operation));
	}

	/**
	 * Checks if a difference operation for exactly given type exists.
	 *
	 * @param type type to check
	 * @return if a difference operation for exactly given type exists
	 */
	public static boolean exactDifferenceExists(Class<?> type) {
		return DIFFERENCES.containsKey(type);
	}

	/**
	 * Checks if a difference operation for given type exists
	 * considering type assignability.
	 *
	 * @param type type to check
	 * @return if a difference operation for given type exists
	 */
	public static boolean differenceExists(Class<?> type) {
		return getDifferenceInfo(type) != null;
	}

	/**
	 * Returns a {@link DifferenceInfo} for a specific type and a desired return type.
	 *
	 * @param type operands type
	 * @param returnType result type
	 * @return A suitable difference info if one is found, else null
	 * @param <T> operands type
	 * @param <R> result type
	 */
	@SuppressWarnings("unchecked")
	public static <T, R> @Nullable DifferenceInfo<T, R> getDifferenceInfo(Class<T> type, Class<R> returnType) {
		DifferenceInfo<T, ?> info = getDifferenceInfo(type);
		if (info != null && returnType.isAssignableFrom(info.returnType()))
			return (DifferenceInfo<T, R>) info;
		return null;
	}

	/**
	 * Returns a {@link DifferenceInfo} for a specific type.
	 *
	 * @param type operands type
	 * @return A suitable difference info if one is found, else null
	 * @param <T> operands type
	 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable DifferenceInfo<T, ?> getDifferenceInfo(Class<T> type) {
		if (Skript.isAcceptRegistrations())
			throw new SkriptAPIException("Differences cannot be retrieved until Skript " +
				"has finished registrations.");
		// we also cache null values
		if (CACHED_DIFFERENCES.containsKey(type))
			return (DifferenceInfo<T, ?>) CACHED_DIFFERENCES.get(type);

		DifferenceInfo<T, ?> difference = null;

		if (DIFFERENCES.containsKey(type)) {
			difference = (DifferenceInfo<T, ?>) DIFFERENCES.get(type);
			CACHED_DIFFERENCES.put(type, difference);
			return difference;
		}

		for (Map.Entry<Class<?>, DifferenceInfo<?, ?>> entry : DIFFERENCES.entrySet()) {
			if (!entry.getKey().isAssignableFrom(type))
				continue;
			difference = (DifferenceInfo<T, ?>) entry.getValue();
			break;
		}

		CACHED_DIFFERENCES.put(type, difference);
		return difference;
	}

	/**
	 * Returns an {@link Operation} of a difference operation
	 * for a specific type and a desired return type.
	 *
	 * @param type operands type
	 * @param returnType result type
	 * @return A suitable operation if one is found, else null
	 * @param <T> operands type
	 * @param <R> result type
	 */
	public static <T, R> @Nullable Operation<T, T, R> getDifference(Class<T> type, Class<R> returnType) {
		DifferenceInfo<T, R> info = getDifferenceInfo(type, returnType);
		return info == null ? null : info.operation();
	}

	/**
	 * Returns an {@link Operation} of a difference operation
	 * for a specific type.
	 *
	 * @param type operands type
	 * @return A suitable operation if one is found, else null
	 * @param <T> operands type
	 */
	public static <T> @Nullable Operation<T, T, ?> getDifference(Class<T> type) {
		DifferenceInfo<T, ?> info = getDifferenceInfo(type);
		return info == null ? null : info.operation();
	}

	/**
	 * Calculates a difference between two types if difference operation
	 * for given return type exist.
	 *
	 * @param left left operand
	 * @param right right operand
	 * @param returnType return type
	 * @return difference
	 * @param <T> operands type
	 * @param <R> return type
	 */
	@SuppressWarnings("unchecked")
	public static <T, R> @Nullable R difference(T left, T right, Class<R> returnType) {
		Operation<T, T, R> operation = (Operation<T, T, R>) getDifference(left.getClass(), returnType);
		return operation == null ? null : operation.calculate(left, right);
	}

	/**
	 * Calculates a difference between two types.
	 * <p>
	 * This method does not check for the return type of the difference info.
	 *
	 * @param left left operand
	 * @param right right operand
	 * @return difference
	 * @param <T> operands type
	 * @param <R> return type
	 */
	@SuppressWarnings("unchecked")
	public static <T, R> @Nullable R differenceUnsafe(T left, T right) {
		Operation<T, T, R> operation = (Operation<T, T, R>) getDifference(left.getClass());
		return operation == null ? null : operation.calculate(left, right);
	}

	/**
	 * Registers a default value for a given type.
	 *
	 * @param type type
	 * @param supplier default value supplier
	 * @param <T> type
	 */
	public static <T> void registerDefaultValue(Class<T> type, Supplier<T> supplier) {
		Skript.checkAcceptRegistrations();
		if (DEFAULT_VALUES.containsKey(type))
			throw new SkriptAPIException("There's already a default value registered for type '" +
				type.getName() + "'");
		DEFAULT_VALUES.put(type, supplier);
	}

	/**
	 * Returns a default value of a given type.
	 *
	 * @param type type
	 * @return default value
	 * @param <R> default value
	 * @param <T> type
	 */
	@SuppressWarnings("unchecked")
	public static <R, T extends R> @Nullable R getDefaultValue(Class<T> type) {
		if (Skript.isAcceptRegistrations())
			throw new SkriptAPIException("Default values cannot be retrieved until Skript has " +
				"finished registrations.");

		Supplier<R> supplier = null;

		if (CACHED_DEFAULT_VALUES.containsKey(type)) {
			supplier = (Supplier<R>) CACHED_DEFAULT_VALUES.get(type);
			return supplier != null ? supplier.get() : null;
		}

		if (DEFAULT_VALUES.containsKey(type)) {
			supplier = (Supplier<R>) DEFAULT_VALUES.get(type);
			CACHED_DEFAULT_VALUES.put(type, supplier);
			return supplier.get();
		}

		for (Map.Entry<Class<?>, Supplier<?>> entry : DEFAULT_VALUES.entrySet()) {
			if (!entry.getKey().isAssignableFrom(type)) continue;
			supplier = (Supplier<R>) entry.getValue();
			break;
		}

		CACHED_DEFAULT_VALUES.put(type, supplier);
		return supplier != null ? supplier.get() : null;
	}

	private static void assertIsOperationsDoneLoading() {
		if (Skript.isAcceptRegistrations())
			throw new SkriptAPIException("Operations cannot be retrieved until Skript has " +
				"finished registrations.");
	}

	/**
	 * All registered types that could be returned from a calculation using this operator.
	 * This is used to fetch potential return types when unknown (variable) arguments are used in a sum.
	 *
	 * @param operator The operator to test
	 * @return Every type this could return
	 */
	public static Collection<Class<?>> getAllReturnTypes(Operator operator) {
		Set<Class<?>> types = new HashSet<>();
		for (OperationInfo<?, ?, ?> info : getRawOperations(operator)) {
			types.add(info.returnType());
		}
		return types;
	}

	/**
	 * Returns set of all operators with operations registered to them.
	 * <p>
	 * This set is sorted by the priority of the operators.
	 * <p>
	 * Modifying this set does not change the registered modifiers.
	 *
	 * @return registered operators
	 */
	public static Set<Operator> getAllOperators() {
		List<Operator> operators = new LinkedList<>(OPERATIONS.keySet());
		Collections.sort(operators);
		return new LinkedHashSet<>(operators);
	}

	private Arithmetics() {
		throw new UnsupportedOperationException();
	}

	private record OperandTypes(Class<?> left, Class<?> right) {}

}
