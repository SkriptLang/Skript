package org.skriptlang.skript.lang.converter;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Array;
import java.util.*;

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

	/**
	 * @return An unmodifiable list containing all registered {@link ConverterInfo}s.
	 * Please note that this does not include any special Converters resolved by Skript during runtime.
	 * This method ONLY returns converters explicitly registered during registration.
	 * Thus, it is recommended to use {@link #getConverter(Class, Class)}.
	 */
	@Unmodifiable
	public static List<ConverterInfo<?, ?>> getConverterInfos() {
		assertIsDoneLoading();
		return Collections.unmodifiableList(CONVERTERS);
	}

	/**
	 * A map for quickly access converters that have already been resolved.
	 * Some pairs may point to a null value, indicating that no converter exists between the two types.
	 * This is useful for skipping complex lookups that may require chaining.
	 */
	private static final Map<Pair<Class<?>, Class<?>>, ConverterInfo<?, ?>> QUICK_ACCESS_CONVERTERS = new HashMap<>(50);

	/**
	 * Registers a new Converter with Skript's collection of Converters.
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @param converter A Converter for converting objects of <code>fromType</code> to <code>toType</code>.
	 */
	public static <F, T> void registerConverter(Class<F> fromType, Class<T> toType, Converter<F, T> converter) {
		registerConverter(fromType, toType, converter, Converter.ALL_CHAINING);
	}

	/**
	 * Registers a new Converter with Skript's collection of Converters.
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @param converter A Converter for converting objects of <code>fromType</code> to <code>toType</code>.
	 * @param flags Flags to set for the Converter. Flags can be found under {@link Converter}.
	 */
	public static <F, T> void registerConverter(Class<F> fromType, Class<T> toType, Converter<F, T> converter, int flags) {
		Skript.checkAcceptRegistrations();

		ConverterInfo<F, T> info = new ConverterInfo<>(fromType, toType, converter, flags);

		synchronized (CONVERTERS) {
			if (exactConverterExists_i(fromType, toType)) {
				throw new SkriptAPIException(
						"A Converter from '" + fromType + "' to '" + toType + "' already exists!"
				);
			}
			CONVERTERS.add(info);
		}
	}

	/**
	 * This method is to be called after Skript has finished registration.
	 * It allows {@link ChainedConverter}s to be created so that Skript may do more complex conversions
	 *  involving multiple converters.
	 */
	// TODO Find a better way of doing this that doesn't require a method to be called (probably requires better Registration API)
	// REMIND how to manage overriding of converters? - shouldn't actually matter
	@SuppressWarnings("unchecked")
	public static <F, M, T> void createChainedConverters() {
		Skript.checkAcceptRegistrations();
		synchronized (CONVERTERS) {
			for (int i = 0; i < CONVERTERS.size(); i++) {

				ConverterInfo<?, ?> unknownInfo1 = CONVERTERS.get(i);
				for (int j = 0; j < CONVERTERS.size(); j++) { // Not from j = i+1 since new converters get added during the loops

					ConverterInfo<?, ?> unknownInfo2 = CONVERTERS.get(j);

					// chain info -> info2
					if (
						unknownInfo2.getFrom() != Object.class // Object can only exist at the beginning of a chain
							&& unknownInfo1.getFrom() != unknownInfo2.getTo()
							&& (unknownInfo1.getFlags() & Converter.NO_RIGHT_CHAINING) == 0
							&& (unknownInfo2.getFlags() & Converter.NO_LEFT_CHAINING) == 0
							&& unknownInfo2.getFrom().isAssignableFrom(unknownInfo1.getTo())
							&& !exactConverterExists_i(unknownInfo1.getFrom(), unknownInfo2.getTo())
					) {
						ConverterInfo<F, M> info1 = (ConverterInfo<F, M>) unknownInfo1;
						ConverterInfo<M, T> info2 = (ConverterInfo<M, T>) unknownInfo2;

						CONVERTERS.add(new ConverterInfo<>(
							info1.getFrom(),
							info2.getTo(),
							new ChainedConverter<>(info1, info2),
							info1.getFlags() | info2.getFlags()
						));
					}

					// chain info2 -> info
					else if (
						unknownInfo1.getFrom() != Object.class // Object can only exist at the beginning of a chain
							&& unknownInfo2.getFrom() != unknownInfo1.getTo()
							&& (unknownInfo1.getFlags() & Converter.NO_LEFT_CHAINING) == 0
							&& (unknownInfo2.getFlags() & Converter.NO_RIGHT_CHAINING) == 0
							&& unknownInfo1.getFrom().isAssignableFrom(unknownInfo2.getTo())
							&& !exactConverterExists_i(unknownInfo2.getFrom(), unknownInfo1.getTo())
					) {
						ConverterInfo<M, T> info1 = (ConverterInfo<M, T>) unknownInfo1;
						ConverterInfo<F, M> info2 = (ConverterInfo<F, M>) unknownInfo2;

						CONVERTERS.add(new ConverterInfo<>(
							info2.getFrom(),
							info1.getTo(),
							new ChainedConverter<>(info2, info1),
							info2.getFlags() | info1.getFlags()
						));
					}

				}

			}
		}
	}

	/**
	 * Internal method. All calling locations are expected to manually synchronize this method if necessary.
	 * @return Whether a Converter exists that EXACTLY matches the provided types.
	 */
	private static boolean exactConverterExists_i(Class<?> fromType, Class<?> toType) {
		for (ConverterInfo<?, ?> info : CONVERTERS) {
			if (fromType == info.getFrom() && toType == info.getTo()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A method for determining whether a direct Converter of <code>fromType</code> to <code>toType</code> exists.
	 * Unlike other methods of this class, it is not the case that
	 *  {@link Skript#isAcceptRegistrations()} must return <code>false</code> for this method to be used.
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @return Whether a direct Converter of <code>fromType</code> to <code>toType</code> exists.
	 */
	public static boolean exactConverterExists(Class<?> fromType, Class<?> toType) {
		synchronized (CONVERTERS) {
			return exactConverterExists_i(fromType, toType);
		}
	}

	/**
	 * A method for determining whether a Converter of <code>fromType</code> to <code>toType</code> exists.
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @return Whether a Converter of <code>fromType</code> to <code>toType</code> exists.
	 */
	public static boolean converterExists(Class<?> fromType, Class<?> toType) {
		assertIsDoneLoading();
		if (toType.isAssignableFrom(fromType)) { // no converter needed
			return true;
		}
		return getConverter(fromType, toType) != null;
	}

	/**
	 * A method for determining whether a direct Converter of <code>fromType</code> to
	 *  one of the provided <code>toTypes</code> exists.
	 * @param fromType The type to convert from.
	 * @param toTypes The types to attempt converting to.
	 * @return Whether a Converter of <code>fromType</code> to one of the provided <code>toTypes</code> exists.
	 */
	public static boolean converterExists(Class<?> fromType, Class<?>... toTypes) {
		assertIsDoneLoading();
		for (Class<?> toType : toTypes) {
			if (converterExists(fromType, toType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A method for obtaining a Converter that can convert an object of <code>fromType</code> into an object of <code>toType</code>.
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @return A Converter capable of converting an object of <code>fromType</code> into an object of <code>toType</code>.
	 * Will return null if no such Converter exists.
	 */
	@Nullable
	public static <F, T> Converter<F, T> getConverter(Class<F> fromType, Class<T> toType) {
		ConverterInfo<F, T> info = getConverterInfo(fromType, toType);
		return info != null ? info.getConverter() : null;
	}

	/**
	 * A method for obtaining the ConverterInfo of a Converter that can convert
	 *  an object of <code>fromType</code> into an object of <code>toType</code>.
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @return The ConverterInfo of a Converter capable of converting an object of <code>fromType</code> into an object of <code>toType</code>.
	 * Will return null if no such Converter exists.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <F, T> ConverterInfo<F, T> getConverterInfo(Class<F> fromType, Class<T> toType) {
		assertIsDoneLoading();

		Pair<Class<?>, Class<?>> pair = new Pair<>(fromType, toType);
		ConverterInfo<F, T> converter;

		synchronized (QUICK_ACCESS_CONVERTERS) {
			if (QUICK_ACCESS_CONVERTERS.containsKey(pair)) {
				converter = (ConverterInfo<F, T>) QUICK_ACCESS_CONVERTERS.get(pair);
			} else { // Compute QUICK_ACCESS for provided types
				converter = getConverterInfo_i(fromType, toType);
				QUICK_ACCESS_CONVERTERS.put(pair, converter);
			}
		}

		return converter;
	}

	/**
	 * The internal method for obtaining the ConverterInfo of a Converter that can convert
	 *  an object of <code>fromType</code> into an object of <code>toType</code>.
	 *
	 * @param fromType The type to convert from.
	 * @param toType The type to convert to.
	 * @return The ConverterInfo of a Converter capable of converting an object of <code>fromType</code> into an object of <code>toType</code>.
	 * Will return null if no such Converter exists.
	 *
	 * @param <F> The type to convert from.
	 * @param <T> The type to convert to.
	 * @param <SubType> The <code>fromType</code> for a Converter that may only convert certain objects of <code>fromType</code>
	 * @param <ParentType> The <code>toType</code> for a Converter that may only sometimes convert objects of <code>fromType</code>
	 * into objects of <code>toType</code> (e.g. the converted object may only share a parent with <code>toType</code>)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static <F, T extends ParentType, SubType extends F, ParentType> ConverterInfo<F, T> getConverterInfo_i(
		Class<F> fromType,
		Class<T> toType
	) {
		// Check for an exact match
		for (ConverterInfo<?, ?> info : CONVERTERS) {
			if (fromType == info.getFrom() && toType == info.getTo()) {
				return (ConverterInfo<F, T>) info;
			}
		}

		// Check for an almost perfect match
		for (ConverterInfo<?, ?> info : CONVERTERS) {
			if (info.getFrom().isAssignableFrom(fromType) && toType.isAssignableFrom(info.getTo())) {
				return (ConverterInfo<F, T>) info;
			}
		}

		// We don't want to create "maybe" converters for 'Object -> X' conversions
		// Instead, we should just try and convert during runtime when we have a better idea of the fromType
		if (fromType == Object.class) {
			return new ConverterInfo<>(
				fromType, toType, fromObject -> Converters.convert(fromObject, toType), Converter.NO_LEFT_CHAINING
			);
		}

		// Attempt to find converters that have either 'from' OR 'to' not exactly matching
		for (ConverterInfo<?, ?> unknownInfo : CONVERTERS) {
			int flags = unknownInfo.getFlags();
			if (unknownInfo.getFrom().isAssignableFrom(fromType) && unknownInfo.getTo().isAssignableFrom(toType)) {
				ConverterInfo<F, ParentType> info = (ConverterInfo<F, ParentType>) unknownInfo;
				if ((flags & Converter.ALLOW_UNSAFE_CASTS) == 0) {
					if ((flags & Converter.NO_RIGHT_CHAINING) == Converter.NO_RIGHT_CHAINING)
						continue;
				}

				// 'to' doesn't exactly match and needs to be filtered
				// Basically, this converter might convert 'F' into something that's shares a parent with 'T'
				return new ConverterInfo<>(fromType, toType, fromObject -> {
					Object converted = info.getConverter().convert(fromObject);
					if (toType.isInstance(converted)) {
						return (T) converted;
					}
					return null;
				}, Converter.ALL_CHAINING);

			} else if (fromType.isAssignableFrom(unknownInfo.getFrom()) && toType.isAssignableFrom(unknownInfo.getTo())) {
				ConverterInfo<SubType, T> info = (ConverterInfo<SubType, T>) unknownInfo;
				if ((flags & Converter.ALLOW_UNSAFE_CASTS) == 0) {
					if ((flags & Converter.NO_LEFT_CHAINING) == Converter.NO_LEFT_CHAINING)
						continue;
				}

				// 'from' doesn't exactly match and needs to be filtered
				// Basically, this converter will only convert certain 'F' objects
				return new ConverterInfo<>(fromType, toType, fromObject -> {
					if (!info.getFrom().isInstance(fromObject)) {
						return null;
					}
					return info.getConverter().convert((SubType) fromObject);
				}, Converter.ALL_CHAINING);

			}
		}

		// At this point, accept both 'from' AND 'to' not exactly matching
		for (ConverterInfo<?, ?> unknownInfo : CONVERTERS) {
			if (fromType.isAssignableFrom(unknownInfo.getFrom()) && unknownInfo.getTo().isAssignableFrom(toType)) {
				ConverterInfo<SubType, ParentType> info = (ConverterInfo<SubType, ParentType>) unknownInfo;
				int flags = unknownInfo.getFlags();
				if ((flags & Converter.ALLOW_UNSAFE_CASTS) == 0) {
					if ((flags & Converter.NO_LEFT_CHAINING) == Converter.NO_LEFT_CHAINING)
						continue;
					if ((flags & Converter.NO_RIGHT_CHAINING) == Converter.NO_RIGHT_CHAINING)
						continue;
				}

				// 'from' and 'to' both don't exactly match and need to be filtered
				// Basically, this converter will only convert certain 'F' objects
				//   and some conversion results will only share a parent with 'T'
				return new ConverterInfo<>(fromType, toType, fromObject -> {
					if (!info.getFrom().isInstance(fromObject)) {
						return null;
					}
					Object converted = info.getConverter().convert((SubType) fromObject);
					if (toType.isInstance(converted)) {
						return (T) converted;
					}
					return null;
				}, Converter.ALL_CHAINING);

			}
		}


		// No converter available
		return null;
	}

	/**
	 * Standard method for converting an object into a different type.
	 * @param from The object to convert.
	 * @param toType The type that <code>from</code> should be converted into.
	 * @return An object of <code>toType</code>, or null if <code>from</code> couldn't be successfully converted.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <From, To> To convert(@Nullable From from, Class<To> toType) {
		assertIsDoneLoading();
		if (from == null) {
			return null;
		}

		if (toType.isInstance(from)) {
			return (To) from;
		}

		Converter<From, To> converter = getConverter((Class<From>) from.getClass(), toType);
		if (converter == null) {
			return null;
		}

		return converter.convert(from);
	}

	/**
	 * A method for converting an object into one of several provided types.
	 * @param from The object to convert.
	 * @param toTypes A list of types that should be tried for converting <code>from</code>.
	 * @return An object of one of the provided <code>toTypes</code>, or null if <code>from</code> couldn't successfully be converted.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <From, To> To convert(@Nullable From from, Class<? extends To>[] toTypes) {
		assertIsDoneLoading();
		if (from == null) {
			return null;
		}

		for (Class<? extends To> toType : toTypes) {
			if (toType.isInstance(from)) {
				return (To) from;
			}
		}

		Class<From> fromType = (Class<From>) from.getClass();
		for (Class<? extends To> toType : toTypes) {
			Converter<From, ? extends To> converter = getConverter(fromType, toType);
			if (converter != null) {
				return converter.convert(from);
			}
		}

		return null;
	}

	/**
	 * Standard method for bulk-conversion of objects into a different type.
	 * @param from The objects to convert.
	 * @param toType The type that <code>from</code> should be converted into.
	 * @return Objects of <code>toType</code>. Will return null if <code>from</code> is null.
	 * Please note that the returned array may not be the same size as <code>from</code>.
	 * This can happen if an object contained within <code>from</code> is not successfully converted.
	 */
	@SuppressWarnings("unchecked")
	public static <To> To[] convert(Object @Nullable [] from, Class<To> toType) {
		assertIsDoneLoading();
		if (from == null) {
			return (To[]) Array.newInstance(toType, 0);
		}

		if (toType.isAssignableFrom(from.getClass().getComponentType())) {
			return (To[]) from;
		}

		List<To> converted = new ArrayList<>(from.length);
		for (Object fromSingle : from) {
			To convertedSingle = convert(fromSingle, toType);
			if (convertedSingle != null) {
				converted.add(convertedSingle);
			}
		}

		return converted.toArray((To[]) Array.newInstance(toType, converted.size()));
	}

	/**
	 * A method for bulk-conversion of objects into one of several provided types.
	 * @param from The objects to convert.
	 * @param toTypes A list of types that should be tried for converting each object.
	 * @param superType A parent type of all provided <code>toTypes</code>.
	 * @return Objects of <code>superType</code>. Will return any empty array if <code>from</code> is null.
	 * Please note that the returned array may not be the same size as <code>from</code>.
	 * This can happen if an object contained within <code>from</code> is not successfully converted.
	 * And, of course, the returned array may contain objects of a different type.
	 */
	@SuppressWarnings("unchecked")
	public static <To> To[] convert(Object @Nullable [] from, Class<? extends To>[] toTypes, Class<To> superType) {
		assertIsDoneLoading();
		if (from == null) {
			return (To[]) Array.newInstance(superType, 0);
		}

		Class<?> fromType = from.getClass().getComponentType();

		for (Class<? extends To> toType : toTypes) {
			if (toType.isAssignableFrom(fromType)) {
				return (To[]) from;
			}
		}

		List<To> converted = new ArrayList<>(from.length);
		for (Object fromSingle : from) {
			To convertedSingle = convert(fromSingle, toTypes);
			if (convertedSingle != null) {
				converted.add(convertedSingle);
			}
		}

		return converted.toArray((To[]) Array.newInstance(superType, converted.size()));
	}

	/**
	 * A method for bulk-converting objects of a specific type using a specific Converter.
	 * @param from The objects to convert.
	 * @param toType The type to convert into.
	 * @param converter The converter to use for conversion.
	 * @return Objects of <code>toType</code>.
	 * Please note that the returned array may not be the same size as <code>from</code>.
	 * This can happen if an object contained within <code>from</code> is not successfully converted.
	 */
	@SuppressWarnings("unchecked")
	public static <From, To> To[] convert(From[] from, Class<To> toType, Converter<? super From, ? extends To> converter) {
		assertIsDoneLoading();
		To[] converted = (To[]) Array.newInstance(toType, from.length);

		int j = 0;
		for (From fromSingle : from) {
			To convertedSingle = fromSingle == null ? null : converter.convert(fromSingle);
			if (convertedSingle != null) {
				converted[j++] = convertedSingle;
			}
		}

		if (j != converted.length) {
			converted = Arrays.copyOf(converted, j);
		}

		return converted;
	}

	/**
	 * A method that guarantees an object of <code>toType</code> is returned.
	 * @param from The object to convert.
	 * @param toType The type to convert into.
	 * @return An object of <code>toType</code>.
	 * @throws ClassCastException If <code>from</code> cannot be converted.
	 */
	public static <To> To convertStrictly(Object from, Class<To> toType) {
		To converted = convert(from, toType);
		if (converted == null) {
			throw new ClassCastException("Cannot convert '" + from + "' to an object of type '" + toType + "'");
		}
		return converted;
	}

	/**
	 * A method for bulk-conversion that guarantees objects of <code>toType</code> are returned.
	 * @param from The object to convert.
	 * @param toType The type to convert into.
	 * @return Objects of <code>toType</code>. The returned array will be the same size as <code>from</code>.
	 * @throws ClassCastException If any of the provided objects cannot be converted.
	 */
	@SuppressWarnings("unchecked")
	public static <To> To[] convertStrictly(Object[] from, Class<To> toType) {
		assertIsDoneLoading();
		To[] converted = (To[]) Array.newInstance(toType, from.length);

		for (int i = 0; i < from.length; i++) {
			To convertedSingle = convert(from[i], toType);
			if (convertedSingle == null) {
				throw new ClassCastException("Cannot convert '" + from[i] + "' to an object of type '" + toType + "'");
			}
			converted[i] = convertedSingle;
		}

		return converted;
	}

	/**
	 * A method for bulk-converting objects of a specific type using a specific Converter.
	 * @param from The objects to convert.
	 * @param toType A superclass for all objects to be converted.
	 * @param converter The converter to use for conversion.
	 * @return Objects of <code>toType</code>.
	 * Please note that the returned array may not be the same size as <code>from</code>.
	 * This can happen if an object contained within <code>from</code> is not successfully converted.
	 * @throws ArrayStoreException If <code>toType</code> is not a superclass of all objects returned by the converter.
	 * @throws ClassCastException If <code>toType</code> is not of <code>To</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <From, To> To[] convertUnsafe(From[] from, Class<?> toType, Converter<? super From, ? extends To> converter) {
		return convert(from, (Class<To>) toType, converter);
	}

	private static void assertIsDoneLoading() {
		if (Skript.isAcceptRegistrations()) {
			throw new SkriptAPIException("Converters cannot be retrieved until Skript has finished registrations.");
		}
	}

}
