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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.Pair;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comparators are used to provide Skript with specific instructions for comparing two objects.
 * By integrating with the {@link Converter} system, comparators can be used to compare two objects.
 *  that seemingly have no {@link Relation}.
 * @see #registerComparator(Class, Class, Comparator)
 */
public final class Comparators {

	private Comparators() {}

	/**
	 * A default comparator to compare two objects using {@link Object#equals(Object)}.
	 */
	public static final Comparator<Object, Object> EQUALS_COMPARATOR = (o1, o2) -> Relation.get(o1.equals(o2));

	/**
	 * A Java {@link java.util.Comparator} for comparing two objects using {@link #compare(Object, Object)}.
	 */
	public static final java.util.Comparator<Object> JAVA_COMPARATOR = (o1, o2) -> compare(o1, o2).getRelation();

	/**
	 * A List containing information for all registered comparators.
	 */
	private static final List<ComparatorInfo<?, ?>> COMPARATORS = new ArrayList<>();

	/**
	 * @return An unmodifiable list containing all registered {@link ComparatorInfo}s.
	 * Please note that this does not include any special Comparators resolved by Skript during runtime.
	 * This method ONLY returns Comparators explicitly registered during registration.
	 */
	public static List<ComparatorInfo<?, ?>> getComparatorInfo() {
		return Collections.unmodifiableList(COMPARATORS);
	}

	/**
	 * A map for quickly accessing comparators that have already been resolved.
	 * This is useful for skipping complex lookups that may require conversion and inversion.
	 */
	private static final Map<Pair<Class<?>, Class<?>>, Comparator<?, ?>> QUICK_ACCESS_COMPARATORS = new HashMap<>();

	/**
	 * Registers a new Comparator with Skript's collection of Comparators.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @param comparator A Comparator for comparing objects of 'firstType' and 'secondType'.
	 */
	public static <Type1, Type2> void registerComparator(
		Class<Type1> firstType,
		Class<Type2> secondType,
		Comparator<Type1, Type2> comparator
	) {
		Skript.checkAcceptRegistrations();

		if (firstType == Object.class && secondType == Object.class)
			throw new IllegalArgumentException("It is not possible to add a comparator between objects");

		COMPARATORS.add(new ComparatorInfo<>(firstType, secondType, comparator));
	}

	/**
	 * Compares two objects to see if a Relation exists between them.
	 * @param first The first object for comparison.
	 * @param second The second object for comparison.
	 * @return The Relation between the two provided objects.
	 */
	@SuppressWarnings("unchecked")
	public static <Type1, Type2> Relation compare(@Nullable Type1 first, @Nullable Type2 second) {
		if (first == null || second == null)
			return Relation.NOT_EQUAL;

		Comparator<Type1, Type2> comparator = getComparator((Class<Type1>) first.getClass(), (Class<Type2>) second.getClass());
		if (comparator == null)
			return Relation.NOT_EQUAL;

		return comparator.compare(first, second);
	}

	/**
	 * A method for obtaining a comparator that can compare two objects of 'firstType' and 'secondType'.
	 * Please note that comparators may convert objects if necessary for comparisons.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return A comparator capable of determine the {@link Relation} between two objects of 'firstType' and 'secondType'.
	 * Will be null if no comparator capable of comparing two objects of 'firstType' and 'secondType' was found.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <Type1, Type2> Comparator<Type1, Type2> getComparator(Class<Type1> firstType, Class<Type2> secondType) {
		Pair<Class<?>, Class<?>> typePair = new Pair<>(firstType, secondType);

		Comparator<Type1, Type2> comparator = (Comparator<Type1, Type2>) QUICK_ACCESS_COMPARATORS.get(typePair);

		if (comparator == null) {
			comparator = getComparator_i(firstType, secondType);
			QUICK_ACCESS_COMPARATORS.put(typePair, comparator);
		}

		return comparator;
	}

	/**
	 * The internal method for obtaining a comparator that can compare two objects of 'firstType' and 'secondType'.
	 * This method handles regular {@link Comparator}s, {@link ConvertedComparator}s, and {@link InverseComparator}s.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return A comparator capable of determine the {@link Relation} between two objects of 'firstType' and 'secondType'.
	 * Will be null if no comparator capable of comparing two objects of 'firstType' and 'secondType' was found.
	 * @param <Type1> The first type for comparison.
	 * @param <Type2> The second type for comparison.
	 * @param <CType1> The first type for any {@link ComparatorInfo}.
	 * This is also used in organizing the conversion process of arguments (ex: 'Type1' to 'CType1' converter).
	 * @param <CType2> The second type for any {@link ComparatorInfo}.
	 * This is also used in organizing the conversion process of arguments (ex: 'Type2' to 'CType2' converter).
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static <Type1, Type2, CType1, CType2> Comparator<Type1, Type2> getComparator_i(
		Class<Type1> firstType,
		Class<Type2> secondType
	) {
		
		// Look for a perfect match
		for (ComparatorInfo<?, ?> info : COMPARATORS) {
			if (info.firstType.isAssignableFrom(firstType) && info.secondType.isAssignableFrom(secondType)) {
				return (Comparator<Type1, Type2>) info.comparator;
			}
		}

		// Try to match and create an InverseComparator
		for (ComparatorInfo<?, ?> info : COMPARATORS) {
			if (info.comparator.supportsInversion() && info.firstType.isAssignableFrom(secondType) && info.secondType.isAssignableFrom(firstType)) {
				return new InverseComparator<>((Comparator<Type2, Type1>) info.comparator);
			}
		}

		// Attempt converting one parameter
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			ComparatorInfo<CType1, CType2> info = (ComparatorInfo<CType1, CType2>) unknownInfo;

			if (info.firstType.isAssignableFrom(firstType)) { // Attempt to convert the second argument to the second comparator type
				Converter<Type2, CType2> sc2 = Converters.getConverter(secondType, info.secondType);
				if (sc2 != null)
					return new ConvertedComparator<>(null, info.comparator, sc2);
			}

			if (info.secondType.isAssignableFrom(secondType)) { // Attempt to convert the first argument to the first comparator type
				Converter<Type1, CType1> fc1 = Converters.getConverter(firstType, info.firstType);
				if (fc1 != null)
					return new ConvertedComparator<>(fc1, info.comparator, null);
			}

		}

		// Attempt converting one parameter but with reversed types
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			if (!unknownInfo.comparator.supportsInversion()) // Unsupported for reversing types
				continue;

			ComparatorInfo<CType1, CType2> info = (ComparatorInfo<CType1, CType2>) unknownInfo;

			if (info.secondType.isAssignableFrom(firstType)) { // Attempt to convert the second argument to the first comparator type
				Converter<Type2, CType1> sc1 = Converters.getConverter(secondType, info.firstType);
				if (sc1 != null)
					return new InverseComparator<>(new ConvertedComparator<>(sc1, info.comparator, null));
			}

			if (info.firstType.isAssignableFrom(secondType)) { // Attempt to convert the first argument to the second comparator type
				Converter<Type1, CType2> fc2 = Converters.getConverter(firstType, info.secondType);
				if (fc2 != null)
					new InverseComparator<>(new ConvertedComparator<>(null, info.comparator, fc2));
			}

		}

		// Attempt converting both parameters
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			ComparatorInfo<CType1, CType2> info = (ComparatorInfo<CType1, CType2>) unknownInfo;

			Converter<Type1, CType1> c1 = Converters.getConverter(firstType, info.firstType);
			Converter<Type2, CType2> c2 = Converters.getConverter(secondType, info.secondType);
			if (c1 != null && c2 != null)
				return new ConvertedComparator<>(c1, info.comparator, c2);

		}

		// Attempt converting both parameters but with reversed types
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			if (!unknownInfo.comparator.supportsInversion()) // Unsupported for reversing types
				continue;

			ComparatorInfo<CType1, CType2> info = (ComparatorInfo<CType1, CType2>) unknownInfo;

			Converter<Type1, CType2> c1 = Converters.getConverter(firstType, info.secondType);
			Converter<Type2, CType1> c2 = Converters.getConverter(secondType, info.firstType);
			if (c1 != null && c2 != null)
				return new InverseComparator<>(new ConvertedComparator<>(c2, info.comparator, c1));

		}

		// Same class but no comparator
		if (firstType != Object.class && secondType == firstType) {
			return (Comparator<Type1, Type2>) EQUALS_COMPARATOR;
		}

		// Well, we tried!
		return null;
	}

}
