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
package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Arithmetics {

	private static final Map<Operator, List<OperationInfo<?, ?, ?>>> operations = Collections.synchronizedMap(new HashMap<>());
	private static final Map<Operator, Map<Pair<Class<?>, Class<?>>, OperationInfo<?, ?, ?>>> cachedOperations = Collections.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, DifferenceInfo<?, ?>> differences = Collections.synchronizedMap(new HashMap<>());
	private static final Map<Class<?>, DifferenceInfo<?, ?>> cachedDifferences = Collections.synchronizedMap(new HashMap<>());

	private static final Map<Class<?>, Supplier<?>> defaultValues = Collections.synchronizedMap(new HashMap<>());
	private static final Map<Class<?>, Supplier<?>> cachedDefaultValues = Collections.synchronizedMap(new HashMap<>());

	public static <T> void registerOperation(Operator operator, Class<T> type, Operation<T, T, T> operation) {
		registerOperation(operator, type, type, type, operation);
	}

	public static <L, R> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Operation<L, R, L> operation) {
		registerOperation(operator, leftClass, rightClass, leftClass, operation);
	}

	public static <L, R> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Operation<L, R, L> operation, Operation<R, L, L> commutativeOperation) {
		registerOperation(operator, leftClass, rightClass, leftClass, operation);
		registerOperation(operator, rightClass, leftClass, leftClass, commutativeOperation);
	}

	public static <L, R, T> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType, Operation<L, R, T> operation, Operation<R, L, T> commutativeOperation) {
		registerOperation(operator, leftClass, rightClass, returnType, operation);
		registerOperation(operator, rightClass, leftClass, returnType, commutativeOperation);
	}

	public static <L, R, T> void registerOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType, Operation<L, R, T> operation) {
		Skript.checkAcceptRegistrations();
		if (exactOperationExists(operator, leftClass, rightClass))
			throw new SkriptAPIException("There's already a " + operator.getName() + " operation registered for types '"
				+ leftClass + "' and '" + rightClass + "'");
		getOperations_i(operator).add(new OperationInfo<>(leftClass, rightClass, returnType, operation));
	}

	private static boolean exactOperationExists(Operator operator, Class<?> leftClass, Class<?> rightClass) {
		for (OperationInfo<?, ?, ?> info : getOperations_i(operator)) {
			if (info.getLeft() == leftClass && info.getRight() == rightClass)
				return true;
		}
		return false;
	}

	public static boolean operationExists(Operator operator, Class<?> leftClass, Class<?> rightClass) {
		return getOperationInfo(operator, leftClass, rightClass) != null;
	}

	private static List<OperationInfo<?, ?, ?>> getOperations_i(Operator operator) {
		return operations.computeIfAbsent(operator, o -> Collections.synchronizedList(new ArrayList<>()));
	}

	@UnmodifiableView
	public static List<OperationInfo<?, ?, ?>> getOperations(Operator operator) {
		assertIsOperationsDoneLoading();
		return Collections.unmodifiableList(getOperations_i(operator));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> List<OperationInfo<T, ?, ?>> getOperations(Operator operator, Class<T> type) {
		return (List) getOperations(operator).stream()
			.filter(info -> info.getLeft().isAssignableFrom(type))
			.collect(Collectors.toList());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R, T> OperationInfo<L, R, T> getOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
		OperationInfo<L, R, ?> info =  getOperationInfo(operator, leftClass, rightClass);
		if (info != null && returnType.isAssignableFrom(info.getReturnType()))
			return (OperationInfo<L, R, T>) info;
		return null;
	}

	private static Map<Pair<Class<?>, Class<?>>, OperationInfo<?, ?, ?>> getCachedOperations(Operator operator) {
		return cachedOperations.computeIfAbsent(operator, o -> Collections.synchronizedMap(new HashMap<>()));
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R> OperationInfo<L, R, ?> getOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass) {
		assertIsOperationsDoneLoading();
		return (OperationInfo<L, R, ?>) getCachedOperations(operator).computeIfAbsent(new Pair<>(leftClass, rightClass), pair ->
			getOperations(operator).stream()
				.filter(info -> info.getLeft().isAssignableFrom(leftClass) && info.getRight().isAssignableFrom(rightClass))
				.reduce((info, info2) -> {
					if (info2.getLeft() == leftClass && info2.getRight() == rightClass)
						return info2;
					return info;
				})
				.orElse(null));
	}

	@Nullable
	public static <L, R, T> Operation<L, R, T> getOperation(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
		OperationInfo<L, R, T> info = getOperationInfo(operator, leftClass, rightClass, returnType);
		return info == null ? null : info.getOperation();
	}

	@Nullable
	public static <L, R> Operation<L, R, ?> getOperation(Operator operator, Class<L> leftClass, Class<R> rightClass) {
		OperationInfo<L, R, ?> info = getOperationInfo(operator, leftClass, rightClass);
		return info == null ? null : info.getOperation();
	}

	@Nullable
	public static <L, R, T> OperationInfo<L, R, T> lookupOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass, Class<T> returnType) {
		OperationInfo<L, R, ?> info = lookupOperationInfo(operator, leftClass, rightClass);
		return info != null ? info.getConverted(leftClass, rightClass, returnType) : null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R> OperationInfo<L, R, ?> lookupOperationInfo(Operator operator, Class<L> leftClass, Class<R> rightClass) {
		OperationInfo<L, R, ?> operationInfo = getOperationInfo(operator, leftClass, rightClass);
		if (operationInfo != null)
			return operationInfo;
		return (OperationInfo<L, R, ?>) getCachedOperations(operator).computeIfAbsent(new Pair<>(leftClass, rightClass), pair -> {
			for (OperationInfo<?, ?, ?> info : getOperations(operator)) {
				if (!info.getLeft().isAssignableFrom(leftClass) && !info.getRight().isAssignableFrom(rightClass))
					continue;
				OperationInfo<L, R, ?> convertedInfo = info.getConverted(leftClass, rightClass, info.getReturnType());
				if (convertedInfo != null)
					return convertedInfo;
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	public static <L, R, T> T calculate(Operator operator, L left, R right, Class<T> returnType) {
		Operation<L, R, T> operation = (Operation<L, R, T>) getOperation(operator, left.getClass(), right.getClass(), returnType);
		return operation == null ? null : operation.calculate(left, right);
	}

	@SuppressWarnings("unchecked")
	public static <L, R, T> T calculateUnsafe(Operator operator, L left, R right) {
		Operation<L, R, T> operation = (Operation<L, R, T>) getOperation(operator, left.getClass(), right.getClass());
		return operation == null ? null : operation.calculate(left, right);
	}

	public static <T> void registerDifference(Class<T> type, Operation<T, T, T> operation) {
		registerDifference(type, type, operation);
	}

	public static <T, R> void registerDifference(Class<T> type, Class<R> returnType, Operation<T, T, R> operation) {
		Skript.checkAcceptRegistrations();
		if (exactDifferenceExists(type))
			throw new IllegalArgumentException("There's already a difference registered for type '" + type + "'");
		differences.put(type, new DifferenceInfo<>(type, returnType, operation));
	}

	private static boolean exactDifferenceExists(Class<?> type) {
		return differences.containsKey(type);
	}

	public static boolean differenceExists(Class<?> type) {
		return getDifferenceInfo(type) != null;
	}

	@SuppressWarnings("unchecked")
	public static <T, R> DifferenceInfo<T, R> getDifferenceInfo(Class<T> type, Class<R> returnType) {
		DifferenceInfo<T, ?> info = getDifferenceInfo(type);
		if (info != null && returnType.isAssignableFrom(info.getReturnType()))
			return (DifferenceInfo<T, R>) info;
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> DifferenceInfo<T, ?> getDifferenceInfo(Class<T> type) {
		if (Skript.isAcceptRegistrations())
			throw new SkriptAPIException("Differences cannot be retrieved until Skript has finished registrations.");
		return (DifferenceInfo<T, ?>) cachedDifferences.computeIfAbsent(type, c -> {
			if (differences.containsKey(type))
				return differences.get(type);
			for (Map.Entry<Class<?>, DifferenceInfo<?, ?>> entry : differences.entrySet()) {
				if (entry.getKey().isAssignableFrom(type))
					return entry.getValue();
			}
			return null;
		});
	}

	public static <T, R> Operation<T, T, R> getDifference(Class<T> type, Class<R> returnType) {
		DifferenceInfo<T, R> info = getDifferenceInfo(type, returnType);
		return info == null ? null : info.getOperation();
	}

	public static <T> Operation<T, T, ?> getDifference(Class<T> type) {
		DifferenceInfo<T, ?> info = getDifferenceInfo(type);
		return info == null ? null : info.getOperation();
	}

	@SuppressWarnings("unchecked")
	public static <T, R> R difference(T left, T right, Class<R> returnType) {
		Operation<T, T, R> operation = (Operation<T, T, R>) getDifference(left.getClass(), returnType);
		return operation == null ? null : operation.calculate(left, right);
	}

	@SuppressWarnings("unchecked")
	public static <T, R> R differenceUnsafe(T left, T right) {
		Operation<T, T, R> operation = (Operation<T, T, R>) getDifference(left.getClass());
		return operation == null ? null : operation.calculate(left, right);
	}

	public static <T> void registerDefaultValue(Class<T> type, Supplier<T> supplier) {
		Skript.checkAcceptRegistrations();
		if (defaultValues.containsKey(type))
			throw new IllegalArgumentException("There's already a default value registered for type '" + type + "'");
		defaultValues.put(type, supplier);
	}

	@SuppressWarnings("unchecked")
	public static <R, T extends R> R getDefaultValue(Class<T> type) {
		if (Skript.isAcceptRegistrations())
			throw new SkriptAPIException("Default values cannot be retrieved until Skript has finished registrations.");
		Supplier<R> supplier = (Supplier<R>) cachedDefaultValues.computeIfAbsent(type, c -> {
			if (defaultValues.containsKey(type))
				return defaultValues.get(type);
			for (Map.Entry<Class<?>, Supplier<?>> entry : defaultValues.entrySet()) {
				if (entry.getKey().isAssignableFrom(type))
					return entry.getValue();
			}
			return null;
		});
		return supplier == null ? null : supplier.get();
	}

	private static void assertIsOperationsDoneLoading() {
		if (Skript.isAcceptRegistrations())
			throw new SkriptAPIException("Operations cannot be retrieved until Skript has finished registrations.");
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
		for (OperationInfo<?, ?, ?> info : getOperations_i(operator)) {
			types.add(info.getReturnType());
		}
		return types;
	}

}
