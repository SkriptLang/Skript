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

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

public class ArithmeticChain implements ArithmeticGettable<Object> {

	@SuppressWarnings("unchecked")
	private static final Checker<Object>[] CHECKERS = new Checker[]{
		o -> o.equals(Operator.ADDITION) || o.equals(Operator.SUBTRACTION),
		o -> o.equals(Operator.MULTIPLICATION) || o.equals(Operator.DIVISION),
		o -> o.equals(Operator.EXPONENTIATION)
	};

	private final ArithmeticGettable<?> left;
	private final ArithmeticGettable<?> right;
	private final Operator operator;
	@Nullable
	@SuppressWarnings("rawtypes")
	private OperationInfo operationInfo;

	public ArithmeticChain(ArithmeticGettable<?> left, Operator operator, ArithmeticGettable<?> right, @Nullable OperationInfo<?, ?, ?> operationInfo) {
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.operationInfo = operationInfo;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public Object get(Event event) {
		Object left = this.left.get(event);
		Object right = this.right.get(event);

		Class<?> leftClass = left == null ? this.left.getReturnType() : left.getClass();
		Class<?> rightClass = right == null ? this.right.getReturnType() : right.getClass();

		if (left == null && leftClass == Object.class && right == null && rightClass == Object.class)
			return null;

		if (right == null) {
			if (rightClass == Object.class) {
				rightClass = operator.lookupClass(leftClass);
				if (rightClass == null)
					return null;
			}

			right = Arithmetics.getDefaultValue(rightClass);
		}

		if (left == null) {
			if (leftClass == Object.class) {
				leftClass = operator.lookupClass(rightClass);
				if (leftClass == null)
					return null;
			}

			left = Arithmetics.getDefaultValue(leftClass);
		}

		if (left == null && operator.getHandlers(rightClass).size() > 0)
			return right;
		if (right == null && operator.getHandlers(leftClass).size() > 0)
			return left;

		if (operationInfo == null) {
			operationInfo = operator.findHandler(leftClass, rightClass);
			if (operationInfo == null)
				return null;
		}

		return operationInfo.getOperation().calculate(left, right);
	}

	@Override
	public Class<?> getReturnType() {
		return operationInfo == null ? Object.class : operationInfo.getReturnType();
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
					operationInfo = operator.findHandler(left.getReturnType(), right.getReturnType());
					if (operationInfo == null)
						return null;
				}

				return new ArithmeticChain(left, operator, right, operationInfo);
			}
		}

		if (chain.size() != 1)
			throw new IllegalStateException();

		return new ArithmeticExpressionInfo<>((Expression<?>) chain.get(0));
	}

}
