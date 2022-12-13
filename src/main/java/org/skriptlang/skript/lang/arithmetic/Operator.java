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
import ch.njol.skript.localization.Noun;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Operator {

	ADDITION('+', "add"),
	SUBTRACTION('-', "subtract"),
	MULTIPLICATION('*', "multiply"),
	DIVISION('/', "divide"),
	EXPONENTIATION('^', "exponent");

	private final List<OperationInfo<?, ?, ?>> handlerList = Collections.synchronizedList(new ArrayList<>());
	private final char sign;
	private final Noun m_name;

	Operator(char sign, String node) {
		this.sign = sign;
		this.m_name = new Noun("operators." + node);
	}

	@Override
	public String toString() {
		return sign + "";
	}

	public String getName() {
		return m_name.toString();
	}

	public <T> void addHandler(Class<T> type, Operation<T, T, T> operation) {
		addHandler(type, type, operation);
	}

	public <L, R> void addHandler(Class<L> left, Class<R> right, Operation<L, R, L> operation) {
		addHandler(left, right, left, operation);
	}

	public <L, R> void addHandler(Class<L> left, Class<R> right, Operation<L, R, L> operation, Operation<R, L, L> commutativeOperation) {
		addHandler(left, right, left, operation, commutativeOperation);
	}

	public <L, R, T> void addHandler(Class<L> left, Class<R> right, Class<T> returnType, Operation<L, R, T> operation, Operation<R, L, T> commutativeOperation) {
		addHandler(left, right, returnType, operation);
		addHandler(right, left, returnType, commutativeOperation);
	}

	public <L, R, T> void addHandler(Class<L> left, Class<R> right, Class<T> returnType, Operation<L, R, T> operation) {
		Skript.checkAcceptRegistrations();
		if (findHandler(left, right) != null)
			throw new SkriptAPIException("An operator is already registered with the types '" + left + "' and '" + right + '\'');
		handlerList.add(new OperationInfo<>(left, right, returnType, operation));
	}

	public List<OperationInfo<?, ?, ?>> getHandlers(Class<?> type) {
		return handlerList.stream()
			.filter(handler -> handler.getLeft().isAssignableFrom(type))
			.collect(Collectors.toList());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <L, R> OperationInfo<L, R, ?> findHandler(Class<L> left, Class<R> right) {
		return (OperationInfo<L, R, ?>) handlerList.stream()
			.filter(handler -> handler.getLeft().isAssignableFrom(left) && handler.getRight().isAssignableFrom(right))
			.findFirst().orElse(null);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <L, R, T> OperationInfo<L, R, T> findHandler(Class<L> left, Class<R> right, Class<T> returnType) {
		return (OperationInfo<L, R, T>) handlerList.stream()
			.filter(handler ->
				handler.getLeft().isAssignableFrom(left)
					&& handler.getRight().isAssignableFrom(right)
					&& handler.getReturnType().isAssignableFrom(returnType))
			.findFirst().orElse(null);
	}

	@Nullable
	public Class<?> lookupClass(Class<?> to) {
		List<OperationInfo<?, ?, ?>> operationInfos = getHandlers(to);
		if (operationInfos.size() == 0)
			return null;
		OperationInfo<?, ?, ?> operation = findHandler(to, to);

		if (operation == null) {
			operation = operationInfos.get(0);
			return operation.getRight();
		} else {
			return to;
		}
	}

	public boolean acceptsClass(Class<?> type, Class<?>... classes) {
		List<OperationInfo<?, ?, ?>> infoList = getHandlers(type);
		if (infoList.size() == 0)
			return false;

		for (Class<?> aClass : classes) {
			for (OperationInfo<?, ?, ?> info : infoList) {
				if (info.getRight().isAssignableFrom(aClass))
					return true;
			}
		}
		return false;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <L, R, T> T calculate(L left, R right, Class<T> expectedReturnType) {
		Operation<L, R, T> operation = (Operation<L, R, T>) findHandler(left.getClass(), right.getClass(), expectedReturnType);
		if (operation == null)
			return null;
		return operation.calculate(left, right);
	}
}
