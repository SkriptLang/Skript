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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Operation;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * Represents a chain of arithmetic operations between two operands.
 *
 * @param <L> the type of the left operand
 * @param <R> the type of the right operand
 * @param <T> the return type of the operation
 */
public class ArithmeticChain<L, R, T> implements ArithmeticGettable<T> {

	@SuppressWarnings("unchecked")
	private static final Checker<Object>[] CHECKERS = new Checker[] {
		o -> o.equals(Operator.ADDITION) || o.equals(Operator.SUBTRACTION),
		o -> o.equals(Operator.MULTIPLICATION) || o.equals(Operator.DIVISION),
		o -> o.equals(Operator.EXPONENTIATION)
	};

	private final ArithmeticGettable<L> left;
	private final ArithmeticGettable<R> right;
	private final Operator operator;
	private final Class<? extends T> returnType;
	@Nullable
	private OperationInfo<? extends L, ? extends R, ? extends T> operationInfo;

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
		if (left == null && this.left instanceof ArithmeticChain)
			return null;

		R right = this.right.get(event);
		if (right == null && this.right instanceof ArithmeticChain)
			return null;

		Class<? extends L> leftClass = left != null ? (Class<? extends L>) left.getClass() : this.left.getReturnType();
		Class<? extends R> rightClass = right != null ? (Class<? extends R>) right.getClass() : this.right.getReturnType();

		if (leftClass == Object.class && rightClass == Object.class)
			return null;

		if (left == null && leftClass == Object.class) {
			operationInfo = lookupOperationInfo(rightClass, OperationInfo::getRight);
		} else if (right == null && rightClass == Object.class) {
			operationInfo = lookupOperationInfo(leftClass, OperationInfo::getLeft);
		} else if (operationInfo == null) {
			operationInfo = Arithmetics.lookupOperationInfo(operator, leftClass, rightClass, returnType);
		}

		if (operationInfo == null)
			return null;

		left = left != null ? left : Arithmetics.getDefaultValue(operationInfo.getLeft());
		if (left == null)
			return null;
		right = right != null ? right : Arithmetics.getDefaultValue(operationInfo.getRight());
		if (right == null)
			return null;

		return ((Operation<L, R, T>) operationInfo.getOperation()).calculate(left, right);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private OperationInfo<L, R, T> lookupOperationInfo(Class<?> anchor, Function<OperationInfo<?, ?, ?>, Class<?>> anchorFunction) {
		OperationInfo<?, ?, ?> operationInfo = Arithmetics.lookupOperationInfo(operator, anchor, anchor);
		if (operationInfo != null)
			return (OperationInfo<L, R, T>) operationInfo;

		return (OperationInfo<L, R, T>) Arithmetics.getOperations(operator).stream()
			.filter(info -> anchorFunction.apply(info).isAssignableFrom(anchor))
			.filter(info -> Converters.converterExists(info.getReturnType(), returnType))
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
				ArithmeticGettable<L> left = parse(chain.subList(0, lastIndex));

				Operator operator = (Operator) chain.get(lastIndex);

				ArithmeticGettable<R> right = parse(chain.subList(lastIndex + 1, chain.size()));

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
