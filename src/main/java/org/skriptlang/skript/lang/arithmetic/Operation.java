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

import org.jetbrains.annotations.NotNull;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Pure_function">pure</a> binary operation
 * that takes two operands of types {@code L} and {@code R}, performs a calculation,
 * and returns a result of type {@code T}.
 *
 * @param <L> The class of the left operand.
 * @param <R> The class of the right operand.
 * @param <T> The return type of the operation.
 */
@FunctionalInterface
public interface Operation<L, R, T> {

	T calculate(@NotNull L left, @NotNull R right);

}
