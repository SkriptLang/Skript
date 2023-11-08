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
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

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
	private static final ComparatorInfo<Object, Object> EQUALS_COMPARATOR_INFO = new ComparatorInfo<>(
		Object.class,
		Object.class,
		(o1, o2) -> Relation.get(o1.equals(o2))
	);

	/**
	 * A List containing information for all registered comparators.
	 */
	private static final List<ComparatorInfo<?, ?>> COMPARATORS = new ArrayList<>(50);

	/**
	 * @return An unmodifiable list containing all registered {@link ComparatorInfo}s.
	 * Please note that this does not include any special Comparators resolved by Skript during runtime.
	 * This method ONLY returns Comparators explicitly registered during registration.
	 * Thus, it is recommended to use {@link #getComparator(Class, Class)} if possible.
	 */
	@Unmodifiable
	public static List<ComparatorInfo<?, ?>> getComparatorInfos() {
		assertIsDoneLoading();
		return Collections.unmodifiableList(COMPARATORS);
	}

	/**
	 * A map for quickly accessing comparators that have already been resolved.
	 * Some pairs may point to a null value, indicating that no comparator exists between the two types.
	 * This is useful for skipping complex lookups that may require conversion and inversion.
	 */
	private static final Map<Pair<Class<?>, Class<?>>, ComparatorInfo<?, ?>> QUICK_ACCESS_COMPARATORS = new HashMap<>(50);

	/**
	 * Registers a new Comparator with Skript's collection of Comparators.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @param comparator A Comparator for comparing objects of <code>firstType</code> and <code>secondType</code>.
	 */
	public static <T1, T2> void registerComparator(
		Class<T1> firstType,
		Class<T2> secondType,
		Comparator<T1, T2> comparator
	) {
		Skript.checkAcceptRegistrations();

		if (firstType == Object.class && secondType == Object.class) {
			throw new IllegalArgumentException("It is not possible to add a comparator between objects");
		}

		synchronized (COMPARATORS) {
			if (exactComparatorExists_i(firstType, secondType)) {
				throw new SkriptAPIException(
					"A Comparator comparing '" + firstType + "' and '" + secondType + "' already exists!"
				);
			}
			COMPARATORS.add(new ComparatorInfo<>(firstType, secondType, comparator));
		}
	}

	/**
	 * Internal method. All calling locations are expected to manually synchronize this method if necessary.
	 * @return Whether a Comparator exists that EXACTLY matches the provided types.
	 */
	private static boolean exactComparatorExists_i(Class<?> firstType, Class<?> secondType) {
		for (ComparatorInfo<?, ?> info : COMPARATORS) {
			if (info.getFirstType() == firstType && info.getSecondType() == secondType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A method for determining whether a direct Comparator of <code>firstType</code> and <code>secondType</code> exists.
	 * Unlike other methods of this class, it is not the case that
	 *  {@link Skript#isAcceptRegistrations()} must return <code>false</code> for this method to be used.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return Whether a direct Comparator of <code>firstType</code> and <code>secondType</code> exists.
	 */
	public static boolean exactComparatorExists(Class<?> firstType, Class<?> secondType) {
		synchronized (COMPARATORS) {
			return exactComparatorExists_i(firstType, secondType);
		}
	}

	/**
	 * A method for determining whether a Comparator of <code>firstType</code> and <code>secondType</code> exists.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return Whether a Comparator of <code>firstType</code> and <code>secondType</code> exists.
	 */
	public static boolean comparatorExists(Class<?> firstType, Class<?> secondType) {
		assertIsDoneLoading();
		if (firstType != Object.class && firstType == secondType) { // Would use the default comparator
			return true;
		}
		return getComparator(firstType, secondType) != null;
	}

	/**
	 * Compares two objects to see if a Relation exists between them.
	 * @param first The first object for comparison.
	 * @param second The second object for comparison.
	 * @return The Relation between the two provided objects.
	 * Guaranteed to be {@link Relation#NOT_EQUAL} if either parameter is null.
	 */
	@SuppressWarnings("unchecked")
	public static <T1, T2> Relation compare(@Nullable T1 first, @Nullable T2 second) {
		assertIsDoneLoading(); // this would be checked later on too, but we want this guaranteed to fail

		if (first == null || second == null) {
			return Relation.NOT_EQUAL;
		}

		if (first == second) { // easiest check of them all!
			return Relation.EQUAL;
		}

		Comparator<T1, T2> comparator = getComparator((Class<T1>) first.getClass(), (Class<T2>) second.getClass());
		if (comparator == null) {
			return Relation.NOT_EQUAL;
		}

		return comparator.compare(first, second);
	}

	/**
	 * A method for obtaining a Comparator that can compare two objects of <code>firstType</code> and <code>secondType</code>.
	 * Please note that comparators may convert objects if necessary for comparisons.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return A Comparator capable of determine the {@link Relation} between two objects of <code>firstType</code> and <code>secondType</code>.
	 * Will be null if no comparator capable of comparing two objects of <code>firstType</code> and <code>secondType</code> was found.
	 */
	@Nullable
	public static <T1, T2> Comparator<T1, T2> getComparator(Class<T1> firstType, Class<T2> secondType) {
		ComparatorInfo<T1, T2> info = getComparatorInfo(firstType, secondType);
		return info != null ? info.getComparator() : null;
	}

	/**
	 * A method for obtaining the info of a Comparator that can compare two objects of <code>firstType</code> and <code>secondType</code>.
	 * Please note that comparators may convert objects if necessary for comparisons.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return The info of a Comparator capable of determine the {@link Relation} between two objects of <code>firstType</code> and <code>secondType</code>.
	 * Will be null if no info for comparing two objects of <code>firstType</code> and <code>secondType</code> was found.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T1, T2> ComparatorInfo<T1, T2> getComparatorInfo(Class<T1> firstType, Class<T2> secondType) {
		assertIsDoneLoading();

		Pair<Class<?>, Class<?>> pair = new Pair<>(firstType, secondType);
		ComparatorInfo<T1, T2> comparator;

		synchronized (QUICK_ACCESS_COMPARATORS) {
			if (QUICK_ACCESS_COMPARATORS.containsKey(pair)) {
				comparator = (ComparatorInfo<T1, T2>) QUICK_ACCESS_COMPARATORS.get(pair);
			} else { // Compute QUICK_ACCESS for provided types
				comparator = getComparatorInfo_i(firstType, secondType);
				QUICK_ACCESS_COMPARATORS.put(pair, comparator);
			}
		}

		return comparator;
	}

	/**
	 * The internal method for obtaining a comparator that can compare two objects of <code>firstType</code> and <code>secondType</code>.
	 * This method handles regular {@link Comparator}s, {@link ConvertedComparator}s, and {@link InverseComparator}s.
	 * @param firstType The first type for comparison.
	 * @param secondType The second type for comparison.
	 * @return The info of the comparator capable of determine the {@link Relation} between two objects of <code>firstType</code> and <code>secondType</code>.
	 * Will be null if no comparator capable of comparing two objects of <code>firstType</code> and <code>secondType</code> was found.
	 * @param <T1> The first type for comparison.
	 * @param <T2> The second type for comparison.
	 * @param <C1> The first type for any {@link ComparatorInfo}.
	 * This is also used in organizing the conversion process of arguments (ex: <code>T1</code> to <code>C1</code> converter).
	 * @param <C2> The second type for any {@link ComparatorInfo}.
	 * This is also used in organizing the conversion process of arguments (ex: <code>T2</code> to <code>C2</code> converter).
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static <T1, T2, C1, C2> ComparatorInfo<T1, T2> getComparatorInfo_i(
		Class<T1> firstType,
		Class<T2> secondType
	) {
		// Look for an exact match
		for (ComparatorInfo<?, ?> info : COMPARATORS) {
			if (info.getFirstType() == firstType && info.getSecondType() == secondType) {
				return (ComparatorInfo<T1, T2>) info;
			}
		}

		// Look for a basically perfect match
		for (ComparatorInfo<?, ?> info : COMPARATORS) {
			if (info.getFirstType().isAssignableFrom(firstType) && info.getSecondType().isAssignableFrom(secondType)) {
				return (ComparatorInfo<T1, T2>) info;
			}
		}

		// Try to match and create an InverseComparator
		for (ComparatorInfo<?, ?> info : COMPARATORS) {
			if (info.getComparator().supportsInversion() && info.getFirstType().isAssignableFrom(secondType) && info.getSecondType().isAssignableFrom(firstType)) {
				return new ComparatorInfo<>(
					firstType,
					secondType,
					new InverseComparator<>((ComparatorInfo<T2, T1>) info)
				);
			}
		}

		// Attempt the simplest conversion (first -> second, second -> first)
		if (Utils.getSuperType(firstType, secondType) == Object.class) { // ensure unrelated classes
			ConverterInfo<T1, T2> fs = Converters.getConverterInfo(firstType, secondType);
			if (fs != null) {
				//noinspection ConstantConditions - getComparator call will never return null
				return new ComparatorInfo<>(
					firstType,
					secondType,
					new ConvertedComparator<>(fs, Comparators.getComparatorInfo(secondType, secondType), null)
				);
			}
			ConverterInfo<T2, T1> sf = Converters.getConverterInfo(secondType, firstType);
			if (sf != null) {
				//noinspection ConstantConditions - getComparator call will never return null
				return new ComparatorInfo<>(
					firstType,
					secondType,
					new ConvertedComparator<>(null, Comparators.getComparatorInfo(firstType, firstType), sf)
				);
			}
		}

		// Attempt converting one parameter
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			ComparatorInfo<C1, C2> info = (ComparatorInfo<C1, C2>) unknownInfo;

			if (info.getFirstType().isAssignableFrom(firstType)) { // Attempt to convert the second argument to the second comparator type
				ConverterInfo<T2, C2> sc2 = Converters.getConverterInfo(secondType, info.getSecondType());
				if (sc2 != null) {
					return new ComparatorInfo<>(
						firstType,
						secondType,
						new ConvertedComparator<>(null, info, sc2)
					);
				}
			}

			if (info.getSecondType().isAssignableFrom(secondType)) { // Attempt to convert the first argument to the first comparator type
				ConverterInfo<T1, C1> fc1 = Converters.getConverterInfo(firstType, info.getFirstType());
				if (fc1 != null) {
					return new ComparatorInfo<>(
						firstType,
						secondType,
						new ConvertedComparator<>(fc1, info, null)
					);
				}
			}

		}

		// Attempt converting one parameter but with reversed types
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			if (!unknownInfo.getComparator().supportsInversion()) { // Unsupported for reversing types
				continue;
			}

			ComparatorInfo<C1, C2> info = (ComparatorInfo<C1, C2>) unknownInfo;

			if (info.getSecondType().isAssignableFrom(firstType)) { // Attempt to convert the second argument to the first comparator type
				ConverterInfo<T2, C1> sc1 = Converters.getConverterInfo(secondType, info.getFirstType());
				if (sc1 != null) {
					return new ComparatorInfo<>(
						firstType,
						secondType,
						new InverseComparator<>(new ComparatorInfo<>(secondType, firstType, new ConvertedComparator<>(sc1, info, null)))
					);
				}
			}

			if (info.getFirstType().isAssignableFrom(secondType)) { // Attempt to convert the first argument to the second comparator type
				ConverterInfo<T1, C2> fc2 = Converters.getConverterInfo(firstType, info.getSecondType());
				if (fc2 != null) {
					return new ComparatorInfo<>(
						firstType,
						secondType,
						new InverseComparator<>(new ComparatorInfo<>(secondType, firstType, new ConvertedComparator<>(null, info, fc2)))
					);
				}
			}

		}

		// Attempt converting both parameters
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			ComparatorInfo<C1, C2> info = (ComparatorInfo<C1, C2>) unknownInfo;

			ConverterInfo<T1, C1> c1 = Converters.getConverterInfo(firstType, info.getFirstType());
			ConverterInfo<T2, C2> c2 = Converters.getConverterInfo(secondType, info.getSecondType());
			if (c1 != null && c2 != null) {
				return new ComparatorInfo<>(
					firstType,
					secondType,
					new ConvertedComparator<>(c1, info, c2)
				);
			}

		}

		// Attempt converting both parameters but with reversed types
		for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
			if (!unknownInfo.getComparator().supportsInversion()) { // Unsupported for reversing types
				continue;
			}

			ComparatorInfo<C1, C2> info = (ComparatorInfo<C1, C2>) unknownInfo;

			ConverterInfo<T1, C2> c1 = Converters.getConverterInfo(firstType, info.getSecondType());
			ConverterInfo<T2, C1> c2 = Converters.getConverterInfo(secondType, info.getFirstType());
			if (c1 != null && c2 != null) {
				return new ComparatorInfo<>(
					firstType,
					secondType,
					new InverseComparator<>(new ComparatorInfo<>(secondType, firstType, new ConvertedComparator<>(c2, info, c1)))
				);
			}

		}

		// Same class but no comparator
		if (firstType != Object.class && firstType == secondType) {
			return (ComparatorInfo<T1, T2>) EQUALS_COMPARATOR_INFO;
		}

		// Well, we tried!
		return null;
	}

	private static void assertIsDoneLoading() {
		if (Skript.isAcceptRegistrations()) {
			throw new SkriptAPIException("Comparators cannot be retrieved until Skript has finished registrations.");
		}
	}

}
