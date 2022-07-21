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

import ch.njol.skript.classes.Converter;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A ConvertedComparator is a comparator that converts its parameters so that they may be used
 * within a different comparator that requires different parameter types.
 *
 * @param <Type1> The first type for comparison.
 * @param <Type2> The second type for comparison.
 * @param <CType1> The type of the conversion result for Type1.
 * If no 'firstConverter' is provided, then this is the same as Type1.
 * @param <CType2> The type of the conversion result for Type2.
 * If no 'secondConverter' is provided, then this is the same as Type2.
 */
final class ConvertedComparator<Type1, Type2, CType1, CType2> implements Comparator<Type1, Type2> {

	private final Comparator<CType1, CType2> comparator;
	@Nullable
	private final Converter<Type1, CType1> firstConverter;
	@Nullable
	private final Converter<Type2, CType2> secondConverter;

	public ConvertedComparator(
		@Nullable Converter<Type1, CType1> firstConverter,
		Comparator<CType1, CType2> c,
		@Nullable Converter<Type2, CType2> secondConverter
	) {
		if (firstConverter == null && secondConverter == null)
			throw new IllegalArgumentException("firstConverter and secondConverter must not BOTH be null!");
		this.firstConverter = firstConverter;
		this.comparator = c;
		this.secondConverter = secondConverter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Relation compare(Type1 o1, Type2 o2) {
		// null converter means 'comparator' is actually Comparator<Type1, CType2>
		CType1 t1 = firstConverter == null ? (CType1) o1 : firstConverter.convert(o1);
		if (t1 == null)
			return Relation.NOT_EQUAL;

		// null converter means 'comparator' is actually Comparator<CType1, Type2>
		CType2 t2 = secondConverter == null ? (CType2) o2 : secondConverter.convert(o2);
		if (t2 == null)
			return Relation.NOT_EQUAL;

		return comparator.compare(t1, t2);
	}

	@Override
	public boolean supportsOrdering() {
		return comparator.supportsOrdering();
	}

	@Override
	public String toString() {
		return "ConvertedComparator(" + firstConverter + "," + comparator + "," + secondConverter + ")";
	}

}
