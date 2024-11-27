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
package ch.njol.util;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class Validate {
	
	public static void notNull(final Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] == null)
				throw new IllegalArgumentException("the " + StringUtils.fancyOrderNumber(i + 1) + " parameter must not be null");
		}
	}
	
	public static void notNull(final @Nullable Object object, final String name) {
		if (object == null)
			throw new IllegalArgumentException(name + " must not be null");
	}
	
	public static void isTrue(final boolean b, final String error) {
		if (!b)
			throw new IllegalArgumentException(error);
	}
	
	public static void isFalse(final boolean b, final String error) {
		if (b)
			throw new IllegalArgumentException(error);
	}
	
	public static void notNullOrEmpty(final @Nullable String s, final String name) {
		if (s == null || s.isEmpty())
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}
	
	public static void notNullOrEmpty(final @Nullable Object[] array, final String name) {
		if (array == null || array.length == 0)
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}
	
	public static void notNullOrEmpty(final @Nullable Collection<?> collection, final String name) {
		if (collection == null || collection.isEmpty())
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}
	
	public static void notEmpty(final @Nullable String s, final String name) {
		if (s != null && s.isEmpty())
			throw new IllegalArgumentException(name + " must not be empty");
	}
	
	public static void notEmpty(final Object[] array, final String name) {
		if (array.length == 0)
			throw new IllegalArgumentException(name + " must not be empty");
	}
	
	public static void notEmpty(final int[] nums, final String name) {
		if (nums.length == 0)
			throw new IllegalArgumentException(name + " must not be empty");
	}
	
}
