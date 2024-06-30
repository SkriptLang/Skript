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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A provider for anything that contains other things.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.conditions.CondContains}
 * conditions.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyContains<Type> extends AnyProvider {

	/**
	 * If {@link #isSafeToCheck(Object)} returns false, values will not be passed to this
	 * method and will instead return false.
	 * <br/>
	 * The null-ness of the parameter depends on whether {@link #isSafeToCheck(Object)} permits null values.
	 *
	 * @param value The value to test
	 * @return Whether this contains {@param value}
	 */
	boolean contains(@UnknownNullability Type value);

	/**
	 * Objects are checked versus this before being cast for {@link #contains(Object)}.
	 * If your contains method doesn't accept all objects (e.g. for a {@link java.util.List#contains(Object)} call)
	 * then it can exclude unwanted types (or null values) here.
	 *
	 * @param value The value to check
	 * @return Whether this is safe to call {@link #contains(Object)} with
	 */
	default boolean isSafeToCheck(Object value) {
		return true;
	}

	/**
	 * The internal method used to verify an object and then check its container.
	 */
	@ApiStatus.Internal
	default boolean checkSafely(Object value) {
		//noinspection unchecked
		return this.isSafeToCheck(value) && this.contains((Type) value);
	}

}
