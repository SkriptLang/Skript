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
package org.skriptlang.skript.lang.comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;

/**
 * A ConvertedComparator is a comparator that converts its parameters so that they may be used
 * within a different comparator that requires different parameter types.
 *
 * @param <T1> The first type for comparison.
 * @param <T2> The second type for comparison.
 * @param <C1> The type of the conversion result for T1.
 * If no 'firstConverter' is provided, then this is the same as T1.
 * @param <C2> The type of the conversion result for T2.
 * If no 'secondConverter' is provided, then this is the same as T2.
 */
final class ConvertedComparator<T1, T2, C1, C2> implements Comparator<T1, T2> {

	private final ComparatorInfo<C1, C2> comparator;
	@Nullable
	private final ConverterInfo<T1, C1> firstConverter;
	@Nullable
	private final ConverterInfo<T2, C2> secondConverter;

	@Contract("null, _, null -> fail")
	ConvertedComparator(
		@Nullable ConverterInfo<T1, C1> firstConverter,
		ComparatorInfo<C1, C2> c,
		@Nullable ConverterInfo<T2, C2> secondConverter
	) {
		if (firstConverter == null && secondConverter == null)
			throw new IllegalArgumentException("firstConverter and secondConverter must not BOTH be null!");
		this.firstConverter = firstConverter;
		this.comparator = c;
		this.secondConverter = secondConverter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Relation compare(T1 o1, T2 o2) {
		// null converter means 'comparator' is actually Comparator<T1, C2>
		C1 t1 = firstConverter == null ? (C1) o1 : firstConverter.getConverter().convert(o1);
		if (t1 == null)
			return Relation.NOT_EQUAL;

		// null converter means 'comparator' is actually Comparator<C1, T2>
		C2 t2 = secondConverter == null ? (C2) o2 : secondConverter.getConverter().convert(o2);
		if (t2 == null)
			return Relation.NOT_EQUAL;

		return comparator.getComparator().compare(t1, t2);
	}

	@Override
	public boolean supportsOrdering() {
		return comparator.getComparator().supportsOrdering();
	}

	@Override
	public String toString() {
		return "ConvertedComparator{first=" + firstConverter + ",comparator=" + comparator + ",second=" + secondConverter + "}";
	}

}
