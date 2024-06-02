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
package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.NotNull;

/**
 * A provider for anything with a (number) amount/size.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprAmount}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyAmount extends AnyProvider {

	/**
     * @return This thing's amount/size
	 */
	@NotNull
	Number amount();

	/**
	 * This is called before {@link #setAmount(Number)}.
	 * If the result is false, setting the amount will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean amountSupportsChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's amount, if possible.
	 * If not possible, then {@link #amountSupportsChange()} should return false and this
	 * may throw an error.
	 *
	 * @param amount The new amount
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setAmount(Number amount) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
