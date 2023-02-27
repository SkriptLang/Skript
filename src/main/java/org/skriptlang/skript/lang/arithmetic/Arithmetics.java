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
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Arithmetics {

	private Arithmetics() {}

	private static final Map<Operator, List<OperationInfo<?, ?, ?>>> registeredOperations = new ConcurrentHashMap<>();
	private static final List<DifferenceInfo<?, ?>> registeredDifferences = Collections.synchronizedList(new ArrayList<>());
	private static final Map<Class<?>, Object> defaultValues = new ConcurrentHashMap<>();

	public static <T> void registerOperation(Operator operator, Class<T> type, Operation<T, T, T> operation) {
		registerOperation(operator, type, type, operation);
	}

	public static <L, R> void registerOperation(Operator operator, Class<L> left, Class<R> right, Operation<L, R, L> operation) {
		registerOperation(operator, left, right, left, operation);
	}

	public static <L, R> void registerOperation(Operator operator, Class<L> left, Class<R> right, Operation<L, R, L> operation, Operation<R, L, L> commutativeOperation) {
		registerOperation(operator, left, right, left, operation, commutativeOperation);
	}

	public static <L, R, T> void registerOperation(Operator operator, Class<L> left, Class<R> right, Class<T> returnType, Operation<L, R, T> operation, Operation<R, L, T> commutativeOperation) {
		registerOperation(operator, left, right, returnType, operation);
		registerOperation(operator, right, left, returnType, commutativeOperation);
	}

	public static <L, R, T> void registerOperation(Operator operator, Class<L> left, Class<R> right, Class<T> returnType, Operation<L, R, T> operation) {
		Skript.checkAcceptRegistrations();
		if (findOperation(operator, left, right) != null)
			throw new SkriptAPIException("An operator is already registered with the types '" + left + "' and '" + right + '\'');
		registeredOperations.computeIfAbsent(operator, op -> Collections.synchronizedList(new ArrayList<>()))
			.add(new OperationInfo<>(left, right, returnType, operation));
	}

	public static List<OperationInfo<?, ?, ?>> getOperations(Operator operator) {
		return registeredOperations.getOrDefault(operator, Collections.emptyList());
	}

	public static List<OperationInfo<?, ?, ?>> getOperations(Operator operator, Class<?> type) {
		return getOperations(operator).stream()
			.filter(handler -> handler.getLeft().isAssignableFrom(type))
			.collect(Collectors.toList());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R> OperationInfo<L, R, ?> findOperation(Operator operator, Class<L> left, Class<R> right) {
		return (OperationInfo<L, R, ?>) getOperations(operator).stream()
			.filter(handler -> handler.getLeft().isAssignableFrom(left) && handler.getRight().isAssignableFrom(right))
			.findFirst().orElse(null);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R, T> OperationInfo<L, R, T> findOperation(Operator operator, Class<L> left, Class<R> right, Class<T> returnType) {
		return (OperationInfo<L, R, T>) getOperations(operator).stream()
			.filter(handler ->
				handler.getLeft().isAssignableFrom(left)
					&& handler.getRight().isAssignableFrom(right)
					&& handler.getReturnType().isAssignableFrom(returnType))
			.findFirst().orElse(null);
	}

	@Nullable
	public static Class<?> lookupClass(Operator operator, Class<?> to) {
		List<OperationInfo<?, ?, ?>> operationInfos = getOperations(operator, to);
		if (operationInfos.size() == 0)
			return null;
		OperationInfo<?, ?, ?> operation = findOperation(operator, to, to);

		if (operation == null) {
			operation = operationInfos.get(0);
			return operation.getRight();
		} else {
			return to;
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R, T> T calculate(Operator operator, L left, R right, Class<T> expectedReturnType) {
		OperationInfo<L, R, T> info = (OperationInfo<L, R, T>) findOperation(operator, left.getClass(), right.getClass(), expectedReturnType);
		if (info == null)
			return null;
		return info.getOperation().calculate(left, right);
	}

	public static <T> void registerDefaultValue(Class<T> type, Supplier<T> supplier) {
		Skript.checkAcceptRegistrations();
		if (defaultValues.containsKey(type))
			throw new SkriptAPIException("A default value is already registered for type '" + type + '\'');
		defaultValues.put(type, supplier);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getDefaultValue(Class<? extends T> type) {
		for (Class<?> c : defaultValues.keySet()) {
			if (c.isAssignableFrom(type))
				return ((Supplier<T>) defaultValues.get(c)).get();
		}
		return null;
	}

	public static <A> void registerDifference(Class<A> type, Operation<A, A, A> operation) {
		registerDifference(type, type, operation);
	}

	public static <A, R> void registerDifference(Class<A> type, Class<R> returnType, Operation<A, A, R> operation) {
		Skript.checkAcceptRegistrations();
		if (getDifferenceInfoExact(type) != null)
			throw new SkriptAPIException("A difference is already registered with types '" + type + "' and '" + returnType + '\'');
		registeredDifferences.add(new DifferenceInfo<>(type, returnType, operation));
	}

	@Nullable
	public static <A> Operation<? super A, ? super A, ?> getDifference(Class<A> type) {
		DifferenceInfo<? super A, ?> differenceInfo = getDifferenceInfo(type);
		if (differenceInfo == null)
			return null;
		return differenceInfo.getOperation();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A, R> Operation<? super A, ? super A, ? super R> getDifference(Class<A> type, Class<R> returnType) {
		DifferenceInfo<? super A, ?> differenceInfo = getDifferenceInfo(type);
		if (differenceInfo == null || !differenceInfo.getReturnType().isAssignableFrom(returnType))
			return null;
		return (Operation<? super A, ? super A, ? super R>) differenceInfo.getOperation();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A> DifferenceInfo<? super A, ?> getDifferenceInfo(Class<A> type) {
		return (DifferenceInfo<? super A, ?>) registeredDifferences.stream()
			.filter(info -> info.getType().isAssignableFrom(type))
			.findFirst().orElse(null);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A> DifferenceInfo<A, ?> getDifferenceInfoExact(Class<A> type) {
		return (DifferenceInfo<A, ?>) registeredDifferences.stream()
			.filter(info -> info.getType() == type)
			.findFirst().orElse(null);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <A, R> R difference(A first, A second, Class<R> expectedReturnType) {
		Operation<A, A, R> difference = (Operation<A, A, R>) getDifference(first.getClass(), expectedReturnType);
		if (difference == null)
			return null;
		return difference.calculate(first, second);
	}

}
