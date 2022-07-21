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

import ch.njol.skript.Skript;
import ch.njol.util.Pair;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converters are used to provide Skript with specific instructions for converting an object to a different type.
 * @see #registerConverter(Class, Class, Converter)
 */
public final class Converters {

	private Converters() {}

	/**
	 * A List containing information for all registered converters.
	 */
	private static final List<ConverterInfo<?, ?>> CONVERTERS = new ArrayList<>(50);

	public static List<ConverterInfo<?, ?>> getConverterInfo() {
		return Collections.unmodifiableList(CONVERTERS);
	}

	/**
	 * A map for quickly access converters that have already been resolved.
	 * This is useful for skipping complex lookups that may require chaining.
	 */
	private static final Map<Pair<Class<?>, Class<?>>, ConverterInfo<?, ?>> QUICK_ACCESS_CONVERTERS = new HashMap<>(50);

	/**
	 * Registers a new Converter with Skript's collection of Converters.
	 * @param from The type to convert from.
	 * @param to The type to convert to.
	 * @param converter A Converter for converting objects of type 'from' to type 'to'.
	 */
	public static <From, To> void registerConverter(Class<From> from, Class<To> to, Converter<From, To> converter) {
		registerConverter(from, to, converter, 0);
	}

	/**
	 * Registers a new Converter with Skript's collection of Converters.
	 * @param from The type to convert from.
	 * @param to The type to convert to.
	 * @param converter A Converter for converting objects of type 'from' to type 'to'.
	 * @param flag A flag to set for the Converter. Flags can be found under {@link Converter}.
	 */
	public static <From, To> void registerConverter(Class<From> from, Class<To> to, Converter<From, To> converter, int flag) {
		Skript.checkAcceptRegistrations();

		ConverterInfo<From, To> info = new ConverterInfo<>(from, to, converter, flag);

		for (int i = 0; i < CONVERTERS.size(); i++) {
			final ConverterInfo<?, ?> info2 = CONVERTERS.get(i);
			if (info2.from.isAssignableFrom(from) && to.isAssignableFrom(info2.to)) {
				CONVERTERS.add(i, info);
				return;
			}
		}

		CONVERTERS.add(info);
	}

	// REMIND how to manage overriding of converters? - shouldn't actually matter
	@SuppressWarnings("unchecked")
	public static <From, Middle, To> void createChainedConverters() {
		for (int i = 0; i < CONVERTERS.size(); i++) {

			ConverterInfo<?, ?> unknownInfo1 = CONVERTERS.get(i);
			for (int j = 0; j < CONVERTERS.size(); j++) { // Not from j = i+1 since new converters get added during the loops

				ConverterInfo<?, ?> unknownInfo2 = CONVERTERS.get(j);

				// chain info -> info2
				if (
					(unknownInfo1.flag & Converter.NO_RIGHT_CHAINING) == 0
					&& (unknownInfo2.flag & Converter.NO_LEFT_CHAINING) == 0
					&& unknownInfo2.from.isAssignableFrom(unknownInfo1.to)
					&& !converterExistsSlow(unknownInfo1.from, unknownInfo2.to)
				) {
					ConverterInfo<From, Middle> info1 = (ConverterInfo<From, Middle>) unknownInfo1;
					ConverterInfo<Middle, To> info2 = (ConverterInfo<Middle, To>) unknownInfo2;

					CONVERTERS.add(new ConverterInfo<>(
						info1.from,
						info2.to,
						new ChainedConverter<>(info1.converter, info2.converter),
						info1.flag | info2.flag
					));
				}

				// chain info2 -> info
				else if (
					(unknownInfo1.flag & Converter.NO_LEFT_CHAINING) == 0
					&& (unknownInfo2.flag & Converter.NO_RIGHT_CHAINING) == 0
					&& unknownInfo1.from.isAssignableFrom(unknownInfo2.to)
					&& !converterExistsSlow(unknownInfo2.from, unknownInfo1.to)
				) {
					ConverterInfo<Middle, To> info1 = (ConverterInfo<Middle, To>) unknownInfo1;
					ConverterInfo<From, Middle> info2 = (ConverterInfo<From, Middle>) unknownInfo2;

					CONVERTERS.add(new ConverterInfo<>(
						info2.from,
						info1.to,
						new ChainedConverter<>(info2.converter, info1.converter),
						info2.flag | info1.flag
					));
				}

			}

		}
	}

	private static boolean converterExistsSlow(Class<?> from, Class<?> to) {
		for (ConverterInfo<?, ?> info : CONVERTERS) {
			if ((info.from.isAssignableFrom(from) || from.isAssignableFrom(info.from)) && (info.to.isAssignableFrom(to) || to.isAssignableFrom(info.to))) {
				return true;
			}
		}
		return false;
	}

	public static boolean converterExists(Class<?> from, Class<?> to) {
		if (to.isAssignableFrom(from) || from.isAssignableFrom(to))
			return true;
		return getConverter(from, to) != null;
	}

	public static boolean converterExists(Class<?> from, Class<?>... to) {
		for (Class<?> toSingle : to) {
			if (converterExists(from, toSingle))
				return true;
		}
		return false;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <From, To> ConverterInfo<From, To> getConverterInfo(Class<From> from, Class<To> to) {
		Pair<Class<?>, Class<?>> typePair = new Pair<>(from, to);

		ConverterInfo<From, To> info = (ConverterInfo<From, To>) QUICK_ACCESS_CONVERTERS.get(typePair);

		if (info == null) { // Manual lookup
			info = getConverter_i(from, to);
			QUICK_ACCESS_CONVERTERS.put(typePair, info);
		}

		return info;
	}

	@Nullable
	public static <From, To> Converter<From, To> getConverter(Class<From> from, Class<To> to) {
		ConverterInfo<From, To> info = getConverterInfo(from, to);
		return info != null ? info.converter : null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <From, To extends ParentType, SubType extends From, ParentType> ConverterInfo<From, To> getConverter_i(Class<From> from, Class<To> to) {
		// Check for existing converters between two types first
		for (ConverterInfo<?, ?> info : CONVERTERS) {
			if (info.from.isAssignableFrom(from) && to.isAssignableFrom(info.to)) {
				return (ConverterInfo<From, To>) info;
			}
		}

		// Attempt to find converters that have either 'from' OR 'to' not exactly matching
		for (ConverterInfo<?, ?> unknownInfo : CONVERTERS) {
			if (unknownInfo.from.isAssignableFrom(from) && unknownInfo.to.isAssignableFrom(to)) {
				ConverterInfo<From, ParentType> info = (ConverterInfo<From, ParentType>) unknownInfo;

				// 'to' doesn't exactly match and needs to be filtered
				// Basically, this converter might convert 'From' into something that's shares a parent with 'To'
				return new ConverterInfo<>(from, to, fromObject -> {
					Object converted = info.converter.convert(fromObject);
					if (to.isInstance(converted))
						return (To) converted;
					return null;
				}, 0);

			} else if (from.isAssignableFrom(unknownInfo.from) && to.isAssignableFrom(unknownInfo.to)) {
				ConverterInfo<SubType, To> info = (ConverterInfo<SubType, To>) unknownInfo;

				// 'from' doesn't exactly match and needs to be filtered
				// Basically, this converter will only convert certain 'From' objects
				return new ConverterInfo<>(from, to, fromObject -> {
					if (!info.from.isInstance(from))
						return null;
					return info.converter.convert((SubType) fromObject);
				}, 0);

			}
		}

		// At this point, accept both 'from' AND 'to' not exactly matching
		for (ConverterInfo<?, ?> unknownInfo : CONVERTERS) {
			if (from.isAssignableFrom(unknownInfo.from) && unknownInfo.to.isAssignableFrom(to)) {
				ConverterInfo<SubType, ParentType> info = (ConverterInfo<SubType, ParentType>) unknownInfo;

				// 'from' and 'to' both don't exactly match and need to be filtered
				// Basically, this converter will only convert certain 'From' objects
				//   and some conversion results will only share a parent with 'To'
				return new ConverterInfo<>(from, to, fromObject -> {
					if (!info.from.isInstance(fromObject))
						return null;
					Object converted = info.converter.convert((SubType) fromObject);
					if (to.isInstance(converted))
						return (To) converted;
					return null;
				}, 0);

			}
		}

		// No converter available
		return null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <From, To> To convert(@Nullable From from, Class<To> to) {
		if (from == null)
			return null;

		if (to.isInstance(from))
			return (To) from;

		Converter<From, To> converter = getConverter((Class<From>) from.getClass(), to);
		if (converter == null)
			return null;

		return converter.convert(from);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <From, To> To convert(@Nullable From from, Class<? extends To>[] to) {
		if (from == null)
			return null;

		for (Class<? extends To> toSingle : to) {
			if (toSingle.isInstance(from))
				return (To) from;
		}

		Class<From> fromType = (Class<From>) from.getClass();
		for (Class<? extends To> toSingle : to) {
			Converter<From, ? extends To> converter = getConverter(fromType, toSingle);
			if (converter != null)
				return converter.convert(from);
		}

		return null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <To> To[] convert(@Nullable Object[] from, Class<To> to) {
		//noinspection ConstantConditions
		if (from == null)
			return null;

		if (to.isAssignableFrom(from.getClass().getComponentType()))
			return (To[]) from;

		List<To> converted = new ArrayList<>(from.length);
		for (Object fromSingle : from) {
			To convertedSingle = convert(fromSingle, to);
			if (convertedSingle != null)
				converted.add(convertedSingle);
		}

		return converted.toArray((To[]) Array.newInstance(to, converted.size()));
	}

	@SuppressWarnings("unchecked")
	public static <To> To[] convert(@Nullable Object[] from, Class<? extends To>[] to, Class<To> superType) {
		//noinspection ConstantConditions
		if (from == null)
			return (To[]) Array.newInstance(superType, 0);

		Class<?> fromType = from.getClass().getComponentType();

		for (Class<? extends To> toSingle : to) {
			if (toSingle.isAssignableFrom(fromType))
				return (To[]) from;
		}

		List<To> converted = new ArrayList<>(from.length);
		for (Object fromSingle : from) {
			To convertedSingle = convert(fromSingle, to);
			if (convertedSingle != null)
				converted.add(convertedSingle);
		}

		return converted.toArray((To[]) Array.newInstance(superType, converted.size()));
	}

	@SuppressWarnings("unchecked")
	public static <From, To> To[] convert(From[] from, Class<?> to, Converter<From, To> converter) {
		To[] converted = (To[]) Array.newInstance(to, from.length);

		int j = 0;
		for (From fromSingle : from) {
			To convertedSingle = fromSingle == null ? null : converter.convert(fromSingle);
			if (convertedSingle != null)
				converted[j++] = convertedSingle;
		}

		if (j != converted.length)
			converted = Arrays.copyOf(converted, j);

		return converted;
	}

	public static <To> To convertStrictly(Object from, Class<To> to) {
		To converted = convert(from, to);
		if (converted == null)
			throw new ClassCastException("Cannot convert '" + from + "' to an object of type '" + to + "'");
		return converted;
	}

	@SuppressWarnings("unchecked")
	public static <To> To[] convertStrictly(Object[] from, Class<To> to) {
		To[] converted = (To[]) Array.newInstance(to, from.length);

		for (int i = 0; i < from.length; i++) {
			To convertedSingle = convert(from[i], to);
			if (convertedSingle == null)
				throw new ClassCastException("Cannot convert '" + from[i] + "' to an object of type '" + to + "'");
			converted[i] = convertedSingle;
		}

		return converted;
	}

	public static <From, To> To[] convertUnsafe(From[] from, Class<?> to, Converter<From, To> converter) {
		return convert(from, to, converter);
	}

}
