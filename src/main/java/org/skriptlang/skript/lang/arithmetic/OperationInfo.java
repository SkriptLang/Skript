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

}
