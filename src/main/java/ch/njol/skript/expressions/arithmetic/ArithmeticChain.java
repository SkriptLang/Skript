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
import java.util.Optional;
import java.util.function.Function;

import ch.njol.util.NonNullPair;
import ch.njol.util.Pair;
import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.converter.Converters;

public class ArithmeticChain<T> implements ArithmeticGettable<T> {

	@SuppressWarnings("unchecked")
	private static final Checker<Object>[] CHECKERS = new Checker[]{
		o -> o.equals(Operator.ADDITION) || o.equals(Operator.SUBTRACTION),
		o -> o.equals(Operator.MULTIPLICATION) || o.equals(Operator.DIVISION),
		o -> o.equals(Operator.EXPONENTIATION)
	};

	private final ArithmeticGettable<?> left;
	private final ArithmeticGettable<?> right;
	private final Operator operator;
	private final Class<? extends T> returnType;
	@Nullable
	@SuppressWarnings("rawtypes")
	private OperationInfo operationInfo;

	public ArithmeticChain(ArithmeticGettable<?> left, Operator operator, ArithmeticGettable<?> right, @Nullable OperationInfo<?, ?, T> operationInfo) {
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
		Object left = this.left.get(event);
		Object right = this.right.get(event);

		Class<?> leftClass = left == null ? this.left.getReturnType() : left.getClass();
		Class<?> rightClass = right == null ? this.right.getReturnType() : right.getClass();

		if (left == null && leftClass == Object.class && right == null && rightClass == Object.class)
			return null;

		if (right == null) {
			if (rightClass == Object.class) {
				rightClass = lookupClass(leftClass, OperationInfo::getLeft)
					.map(Pair::getSecond)
					.orElse(null);
				if (rightClass == null)
					return null;
			}

			right = Arithmetics.getDefaultValue(rightClass);
		}

		if (left == null) {
			if (leftClass == Object.class) {
				leftClass = lookupClass(rightClass, OperationInfo::getRight)
					.map(Pair::getFirst)
					.orElse(null);
				if (leftClass == null)
					return null;
			}

			left = Arithmetics.getDefaultValue(leftClass);
		}

		if (left == null && right != null && Converters.converterExists(rightClass, returnType))
			return Converters.convert(right, returnType);
		if (right == null && left != null && Converters.converterExists(leftClass, returnType))
			return Converters.convert(left, returnType);

		if (operationInfo == null) {
			operationInfo = Arithmetics.lookupOperationInfo(operator, leftClass, rightClass);
			if (operationInfo == null || !Converters.converterExists(operationInfo.getReturnType(), returnType))
				return null;
		}

		if (!operationInfo.getLeft().isAssignableFrom(leftClass))
			left = Converters.convert(left, operationInfo.getLeft());
		if (!operationInfo.getRight().isAssignableFrom(rightClass))
			right = Converters.convert(right, operationInfo.getRight());

		Object result = operationInfo.getOperation().calculate(left, right);
		return Converters.convert(result, returnType);
	}

	private <C> Optional<NonNullPair<Class<?>, Class<?>>> lookupClass(Class<C> cls, Function<OperationInfo<?, ?, ?>, Class<?>> function) {
		List<OperationInfo<?, ?, ?>> operationInfos = Arithmetics.getOperations(operator);
		if (operationInfos.isEmpty())
			return Optional.empty();

		OperationInfo<?, ?, ?> operation = Arithmetics.getOperationInfo(operator, cls, cls);
		if (operation != null)
			return Optional.of(new NonNullPair<>(cls, cls));

		return operationInfos.stream()
			.filter(info -> function.apply(info).isAssignableFrom(cls))
			.findFirst()
			.map(info -> new NonNullPair<>(info.getLeft(), info.getRight()));
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Nullable
	public static ArithmeticGettable<?> parse(List<Object> chain) {
		for (Checker<Object> checker : CHECKERS) {
			int lastIndex = Utils.findLastIndex(chain, checker);
			
			if (lastIndex != -1) {
				List<Object> leftChain = chain.subList(0, lastIndex);
				ArithmeticGettable<?> left = parse(leftChain);

				Operator operator = (Operator) chain.get(lastIndex);

				List<Object> rightChain = chain.subList(lastIndex + 1, chain.size());
				ArithmeticGettable<?> right = parse(rightChain);

				if (left == null || right == null)
					return null;

				OperationInfo<?, ?, ?> operationInfo = null;
				if (left.getReturnType() != Object.class && right.getReturnType() != Object.class) {
					operationInfo = Arithmetics.lookupOperationInfo(operator, left.getReturnType(), right.getReturnType());
					if (operationInfo == null)
						return null;
				}

				return new ArithmeticChain<>(left, operator, right, operationInfo);
			}
		}

		if (chain.size() != 1)
			throw new IllegalStateException();

		return new ArithmeticExpressionInfo<>((Expression<?>) chain.get(0));
	}

}
