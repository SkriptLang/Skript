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

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * @param <L> The class of left operand
 * @param <R> The class of the right operand
 * @param <T> The return type of the operation
 */
public class OperationInfo<L, R, T> {

	private final Class<L> left;
	private final Class<R> right;
	private final Class<T> returnType;
	private final Operation<L, R, T> operation;

	public OperationInfo(Class<L> left, Class<R> right, Class<T> returnType, Operation<L, R, T> operation) {
		this.left = left;
		this.right = right;
		this.returnType = returnType;
		this.operation = operation;
	}

	public Class<L> getLeft() {
		return left;
	}

	public Class<R> getRight() {
		return right;
	}

	public Class<T> getReturnType() {
		return returnType;
	}

	public Operation<L, R, T> getOperation() {
		return operation;
	}

	public <L2, R2> @Nullable OperationInfo<L2, R2, T> getConverted(Class<L2> fromLeft, Class<R2> fromRight) {
		return getConverted(fromLeft, fromRight, returnType);
	}

	public <L2, R2, T2> @Nullable OperationInfo<L2, R2, T2> getConverted(Class<L2> fromLeft, Class<R2> fromRight, Class<T2> toReturnType) {
		if (fromLeft == Object.class || fromRight == Object.class)
			return null;
		if (!Converters.converterExists(fromLeft, left) || !Converters.converterExists(fromRight, right) || !Converters.converterExists(returnType, toReturnType))
			return null;
		return new OperationInfo<>(fromLeft, fromRight, toReturnType, (left, right) -> {
			L convertedLeft = Converters.convert(left, this.left);
			R convertedRight = Converters.convert(right, this.right);
		if (convertedLeft == null || convertedRight == null)
			return null;
		T result = operation.calculate(convertedLeft, convertedRight);
			return Converters.convert(result, toReturnType);
		});
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("left", left)
			.add("right", right)
			.add("returnType", returnType)
			.toString();
	}

}
