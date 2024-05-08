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

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Predicate;

public interface NullableChecker<T> extends ch.njol.util.Checker<T>, Predicate<T> {

	@Override
	boolean check(@Nullable T o);

	public static final NullableChecker<Object> nullChecker = new NullableChecker<Object>() {
		@Override
		public boolean check(final @Nullable Object o) {
			return o != null;
		}
	};

}
