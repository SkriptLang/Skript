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

import org.jetbrains.annotations.UnknownNullability;

/**
 * A provider for anything with a (text) name.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprName}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyNamed extends AnyProvider {

	/**
	 * @return This thing's name
	 */
	@UnknownNullability String name();

	/**
	 * This is called before {@link #setName(String)}.
	 * If the result is false, setting the name will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean nameSupportsChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's name, if possible.
	 * If not possible, then {@link #nameSupportsChange()} should return false and this
	 * may throw an error.
	 *
	 * @param name The name to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setName(String name) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
