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

import java.util.List;
import java.util.function.Function;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.converter.Converters;

public class ArithmeticChain<L, R, T> implements ArithmeticGettable<T> {

	@SuppressWarnings("unchecked")
	private static final Checker<Object>[] CHECKERS = new Checker[]{
		o -> o.equals(Operator.ADDITION) || o.equals(Operator.SUBTRACTION),
		o -> o.equals(Operator.MULTIPLICATION) || o.equals(Operator.DIVISION),
		o -> o.equals(Operator.EXPONENTIATION)
	};

	private final ArithmeticGettable<L> left;
	private final ArithmeticGettable<R> right;
	private final Operator operator;
	private final Class<? extends T> returnType;
	@Nullable
	private OperationInfo<L, R, T> operationInfo;

	public ArithmeticChain(ArithmeticGettable<L> left, Operator operator, ArithmeticGettable<R> right, @Nullable OperationInfo<L, R, T> operationInfo) {
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.operationInfo = operationInfo;
		this.returnType = operationInfo != null ? operationInfo.getReturnType() : (Class<? extends T>) Object.class;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public T get(Event event) {
		L left = this.left.get(event);
		R right = this.right.get(event);

		Class<? extends L> leftClass = left == null ? this.left.getReturnType() : (Class<? extends L>) left.getClass();
		Class<? extends R> rightClass = right == null ? this.right.getReturnType() : (Class<? extends R>) right.getClass();

		if (left == null && leftClass == Object.class && right == null && rightClass == Object.class)
			return null;

		if (operationInfo == null && leftClass != Object.class && rightClass != Object.class)
			operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(operator, leftClass, rightClass);

		if (operationInfo != null)
			calculate(leftClass, left, rightClass, right);

		if (right == null) {
			 operationInfo = (OperationInfo<L, R, T>) lookupOperationInfo(leftClass, OperationInfo::getLeft, rightClass);
		} else if (left == null) {
			 operationInfo = (OperationInfo<L, R, T>) lookupOperationInfo(rightClass, OperationInfo::getRight, leftClass);
		}

		if (operationInfo == null || !Converters.converterExists(operationInfo.getReturnType(), returnType))
			return null;

		leftClass = operationInfo.getLeft();
		rightClass = operationInfo.getRight();
		if (left == null) {
			left = Arithmetics.getDefaultValue(leftClass);
		} else {
			right = Arithmetics.getDefaultValue(rightClass);
		}

		return calculate(leftClass, left, rightClass, right);
	}

	@Nullable
	private T calculate(Class<? extends L> leftClass, @Nullable L left, Class<? extends R> rightClass, @Nullable R right) {
		if (operationInfo == null)
			return null;
		left = getOrDefault(leftClass, left);
		right = getOrDefault(rightClass, right);
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return Converters.convert(left, returnType);
		} else if (left == null) {
			return Converters.convert(right, returnType);
		}
		return Converters.convert(operationInfo.getOperation().calculate(left, right), returnType);
	}

	private <D> D getOrDefault(Class<? extends D> type, @Nullable D value) {
		return value != null ? value : Arithmetics.getDefaultValue(type);
	}

	@Nullable
	private OperationInfo<?, ?, ?> lookupOperationInfo(
		Class<?> anchor,
		Function<OperationInfo<?, ?, ?>, Class<?>> anchorFunction,
		Class<?> targetClass
	) {
		if (targetClass != Object.class)
			return Arithmetics.lookupOperationInfo(operator, anchor, targetClass);

		OperationInfo<?, ?, ?> operationInfo = Arithmetics.getOperationInfo(operator, anchor, anchor);
        if (operationInfo != null)
            return operationInfo;

		List<OperationInfo<?, ?, ?>> operationInfos = Arithmetics.getOperations(operator);
		return operationInfos.stream()
			.filter(info -> anchorFunction.apply(info).isAssignableFrom(anchor))
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

	@Nullable
	@SuppressWarnings("unchecked")
	public static <L, R, T> ArithmeticGettable<T> parse(List<Object> chain) {
		for (Checker<Object> checker : CHECKERS) {
			int lastIndex = Utils.findLastIndex(chain, checker);

			if (lastIndex != -1) {
				List<Object> leftChain = chain.subList(0, lastIndex);
				ArithmeticGettable<L> left = parse(leftChain);

				Operator operator = (Operator) chain.get(lastIndex);

				List<Object> rightChain = chain.subList(lastIndex + 1, chain.size());
				ArithmeticGettable<R> right = parse(rightChain);

				if (left == null || right == null)
					return null;

				OperationInfo<L, R, T> operationInfo = null;
				if (left.getReturnType() != Object.class && right.getReturnType() != Object.class) {
					operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(operator, left.getReturnType(), right.getReturnType());
					if (operationInfo == null)
						return null;
				}

				return new ArithmeticChain<>(left, operator, right, operationInfo);
			}
		}

		if (chain.size() != 1)
			throw new IllegalStateException();

		return new ArithmeticExpressionInfo<>((Expression<T>) chain.get(0));
	}

}
