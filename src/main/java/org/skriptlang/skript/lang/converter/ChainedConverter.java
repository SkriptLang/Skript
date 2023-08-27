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
package org.skriptlang.skript.lang.converter;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A Chained Converter is very similar to a regular {@link Converter}.
 * They are used when it is not possible to directly convert from Type A to Type B.
 * Instead, a "middle man" is used that makes it possible to convert from Type A to Type B.
 * What this means is that Type A is converted to the "middle man" type.
 * Then, that "middle man" type is converted to Type B.
 *
 * Of course, multiple Chained Converters can be chained together to provide far more conversion possibilities.
 * In these cases, you might have, for example, another "middle man" between
 *  this Chained Converter's Type A and "middle man"
 *
 * By using conversion chains, Skript can convert an object into a different type that seemingly has no relation.
 *
 * @param <F> The type to convert from.
 * @param <M> A middle type that is needed for converting 'from' to 'to'.
 *                'from' will be converted to this type, and then this type will be converted to 'to'.
 * @param <T> The type to convert to.
 */
final class ChainedConverter<F, M, T> implements Converter<F, T> {

	private final Converter<F, M> first;
	private final Converter<M, T> second;

	ChainedConverter(Converter<F, M> first, Converter<M, T> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	@Nullable
	public T convert(F from) {
		M middle = first.convert(from);
		if (middle == null) {
			return null;
		}
		return second.convert(middle);
	}

	@Override
	public String toString() {
		return "ChainedConverter{first=" + first + ",second=" + second + "}";
	}

}
