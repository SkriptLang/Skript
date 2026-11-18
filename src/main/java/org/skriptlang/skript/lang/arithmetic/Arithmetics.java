package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for managing arithmetic operations.
 */
public final class Arithmetics {

	/**
	 * Cached marker for a lookup that found nothing, since {@link ConcurrentHashMap} can't hold null values.
	 */
	private static final Object NONE = new Object();

	private static final Map<Operator, OperatorEntry> OPERATORS = new ConcurrentHashMap<>();
	private static final TypeRegistry<DifferenceInfo<?, ?>> DIFFERENCES = new TypeRegistry<>();
	private static final TypeRegistry<Supplier<?>> DEFAULT_VALUES = new TypeRegistry<>();

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
		if (!entry(operator).add(new OperationInfo<>(leftClass, rightClass, returnType, operation)))
			throw new SkriptAPIException("There's already a " + operator.getName() +
				" operation registered for types '" + leftClass.getName() + "' and '" +
				rightClass.getName() + "'");
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
		return entry(operator).exactExists(leftClass, rightClass);
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

	private static OperatorEntry entry(Operator operator) {
		// plain get first, as computeIfAbsent may lock even when the key is present
		OperatorEntry entry = OPERATORS.get(operator);
		return entry != null ? entry : OPERATORS.computeIfAbsent(operator, o -> new OperatorEntry());
	}

	/**
	 * Returns view of all operation infos instances registered for a specific {@link Operator}.
	 *
	 * @param operator the operator
	 * @return all registered operations for the given operator
	 */
	public static @UnmodifiableView List<OperationInfo<?, ?, ?>> getOperations(Operator operator) {
		return entry(operator).view;
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
		return (List) entry(operator).byLeft(type);
	}

	/**
	 * Checks whether any operation is registered for a given operator
	 * where the left operand type is assignable from the specified {@code type}.
	 * This does not consider type conversions, but does consider type assignability.
	 *
	 * @param operator the operator
	 * @param type left operand type
	 * @return whether {@link #getOperations(Operator, Class)} would return a non-empty list
	 */
	public static boolean hasOperations(Operator operator, Class<?> type) {
		return !entry(operator).byLeft(type).isEmpty();
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
		return (List) entry(operator).operations.stream()
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
		return (List) entry(operator).operations.stream()
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
		return (OperationInfo<L, R, ?>) entry(operator).exact(leftClass, rightClass);
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
		assertIsOperationsDoneLoading();
		return (OperationInfo<L, R, ?>) entry(operator).lookup(leftClass, rightClass);
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
		if (!DIFFERENCES.register(type, new DifferenceInfo<>(type, returnType, operation)))
			throw new SkriptAPIException("There's already a difference registered for type '" + type + "'");
	}

	/**
	 * Checks if a difference operation for exactly given type exists.
	 *
	 * @param type type to check
	 * @return if a difference operation for exactly given type exists
	 */
	public static boolean exactDifferenceExists(Class<?> type) {
		return DIFFERENCES.containsExact(type);
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
		return (DifferenceInfo<T, ?>) DIFFERENCES.resolve(type);
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
		if (!DEFAULT_VALUES.register(type, supplier))
			throw new SkriptAPIException("There's already a default value registered for type '" +
				type.getName() + "'");
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
		Supplier<R> supplier = (Supplier<R>) DEFAULT_VALUES.resolve(type);
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
		for (OperationInfo<?, ?, ?> info : entry(operator).operations) {
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
		List<Operator> operators = new LinkedList<>(OPERATORS.keySet());
		Collections.sort(operators);
		return new LinkedHashSet<>(operators);
	}

	private Arithmetics() {
		throw new UnsupportedOperationException();
	}

	/**
	 * The operations registered for one {@link Operator}, along with the lookup caches derived from them.
	 * Registering an operation clears the caches.
	 */
	private static final class OperatorEntry {

		final List<OperationInfo<?, ?, ?>> operations = new CopyOnWriteArrayList<>();
		final List<OperationInfo<?, ?, ?>> view = Collections.unmodifiableList(operations);

		// only filled once registrations have closed
		private final ConcurrentHashMap<Class<?>, List<OperationInfo<?, ?, ?>>> byLeftCache = new ConcurrentHashMap<>();
		private final PairCache exactCache = new PairCache(this::findExact);
		private final PairCache convertedCache = new PairCache(this::findConverted);

		/**
		 * @return false if an operation with the exact same operand types is already registered
		 */
		synchronized boolean add(OperationInfo<?, ?, ?> info) {
			if (exactExists(info.left(), info.right()))
				return false;
			operations.add(info);
			// tests can register operations after registrations have closed, so drop anything cached from the old list
			byLeftCache.clear();
			exactCache.clear();
			convertedCache.clear();
			return true;
		}

		boolean exactExists(Class<?> left, Class<?> right) {
			for (OperationInfo<?, ?, ?> info : operations) {
				if (info.left() == left && info.right() == right)
					return true;
			}
			return false;
		}

		@Nullable OperationInfo<?, ?, ?> exact(Class<?> left, Class<?> right) {
			return (OperationInfo<?, ?, ?>) exactCache.get(left, right);
		}

		/**
		 * Assignable operations first, then converted ones.
		 */
		@Nullable OperationInfo<?, ?, ?> lookup(Class<?> left, Class<?> right) {
			OperationInfo<?, ?, ?> info = exact(left, right);
			return info != null ? info : (OperationInfo<?, ?, ?>) convertedCache.get(left, right);
		}

		List<OperationInfo<?, ?, ?>> byLeft(Class<?> type) {
			// can be called during parsing (Changer#getArithmeticChangeTypes), so don't cache while registering
			if (Skript.isAcceptRegistrations())
				return filterByLeft(type);
			List<OperationInfo<?, ?, ?>> cached = byLeftCache.get(type);
			return cached != null ? cached : byLeftCache.computeIfAbsent(type, this::filterByLeft);
		}

		private @Nullable Object findExact(Class<?> left, Class<?> right) {
			return operations.stream()
				.filter(info -> info.left().isAssignableFrom(left) && info.right().isAssignableFrom(right))
				// prefer an exact match over one that is only assignable
				.reduce((info, info2) -> {
					if (info2.left() == left && info2.right() == right)
						return info2;
					return info;
				})
				.orElse(null);
		}

		private @Nullable Object findConverted(Class<?> left, Class<?> right) {
			for (OperationInfo<?, ?, ?> info : operations) {
				OperationInfo<?, ?, ?> convertedInfo = info.getConverted(left, right, info.returnType());
				if (convertedInfo != null)
					return convertedInfo;
			}
			return null;
		}

		private List<OperationInfo<?, ?, ?>> filterByLeft(Class<?> type) {
			return operations.stream()
				.filter(info -> info.left().isAssignableFrom(type))
				.toList();
		}

	}

	/**
	 * A lock-free cache keyed by a pair of classes, which computes and stores (including misses) on first lookup.
	 */
	private static final class PairCache {

		private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<Class<?>, Object>> map = new ConcurrentHashMap<>();
		private final BiFunction<Class<?>, Class<?>, Object> finder;

		PairCache(BiFunction<Class<?>, Class<?>, Object> finder) {
			this.finder = finder;
		}

		@Nullable Object get(Class<?> left, Class<?> right) {
			ConcurrentHashMap<Class<?>, Object> inner = map.get(left);
			Object cached = inner != null ? inner.get(right) : null;
			if (cached == null) {
				cached = finder.apply(left, right);
				// we also cache null values for non-existing operations
				if (cached == null)
					cached = NONE;
				map.computeIfAbsent(left, k -> new ConcurrentHashMap<>()).putIfAbsent(right, cached);
			}
			return cached == NONE ? null : cached;
		}

		void clear() {
			map.clear();
		}

	}

	/**
	 * Values registered per class, resolved by exact class first and then by the first registered supertype.
	 * Resolutions, including misses, are cached until the next registration.
	 */
	private static final class TypeRegistry<V> {

		private final Map<Class<?>, V> registered = new LinkedHashMap<>();
		private final ConcurrentHashMap<Class<?>, Object> resolved = new ConcurrentHashMap<>();

		synchronized boolean containsExact(Class<?> type) {
			return registered.containsKey(type);
		}

		/**
		 * @return false if a value is already registered for exactly this type
		 */
		synchronized boolean register(Class<?> type, V value) {
			if (registered.containsKey(type))
				return false;
			registered.put(type, value);
			resolved.clear();
			return true;
		}

		@SuppressWarnings("unchecked")
		@Nullable V resolve(Class<?> type) {
			Object cached = resolved.get(type);
			if (cached == null) {
				cached = find(type);
				// we also cache null values
				if (cached == null)
					cached = NONE;
				resolved.putIfAbsent(type, cached);
			}
			return cached == NONE ? null : (V) cached;
		}

		private synchronized @Nullable V find(Class<?> type) {
			V exact = registered.get(type);
			if (exact != null)
				return exact;
			// fall back to the first registered supertype
			for (Map.Entry<Class<?>, V> entry : registered.entrySet()) {
				if (entry.getKey().isAssignableFrom(type))
					return entry.getValue();
			}
			return null;
		}

	}

}
