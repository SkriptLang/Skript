package ch.njol.skript.registrations;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains all registered converters and allows operating with them.
 * @deprecated Use {@link org.skriptlang.skript.lang.converter.Converters}
 */
@Deprecated
public abstract class Converters {
	
	private Converters() {}
	
	@SuppressWarnings("unchecked")
	public static <F, T> List<ConverterInfo<?, ?>> getConverters() {
		return org.skriptlang.skript.lang.converter.Converters.getConverterInfos().stream()
			.map(unknownInfo -> {
				org.skriptlang.skript.lang.converter.ConverterInfo<F, T> info = (org.skriptlang.skript.lang.converter.ConverterInfo<F, T>) unknownInfo;
				return new ConverterInfo<>(info.getFrom(), info.getTo(), info.getConverter()::convert, info.getFlags());
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * Registers a converter.
	 * 
	 * @param from Type that the converter converts from.
	 * @param to  Type that the converter converts to.
	 * @param converter Actual converter.
	 */
	public static <F, T> void registerConverter(Class<F> from, Class<T> to, Converter<F, T> converter) {
		registerConverter(from, to, converter, 0);
	}
	
	public static <F, T> void registerConverter(Class<F> from, Class<T> to, Converter<F, T> converter, int options) {
		org.skriptlang.skript.lang.converter.Converters.registerConverter(from, to, converter::convert, options);
	}
	
	// REMIND how to manage overriding of converters? - shouldn't actually matter
	public static void createMissingConverters() {
		org.skriptlang.skript.lang.converter.Converters.createChainedConverters();
	}
	
	/**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a
	 * converter to convert the values.
	 * 
	 * @param o
	 * @param to
	 * @return The converted value or null if no converter exists or the converter returned null for the given value.
	 */
	@Nullable
	public static <F, T> T convert(final @Nullable F o, final Class<T> to) {
		return org.skriptlang.skript.lang.converter.Converters.convert(o, to);
	}
	
	/**
	 * Converts an object into one of the given types.
	 * <p>
	 * This method does not convert the object if it is already an instance of any of the given classes.
	 * 
	 * @param o
	 * @param to
	 * @return The converted object
	 */
	@Nullable
	public static <F, T> T convert(final @Nullable F o, final Class<? extends T>[] to) {
		return org.skriptlang.skript.lang.converter.Converters.convert(o, to);
	}
	
	/**
	 * Converts all entries in the given array to the desired type, using {@link #convert(Object, Class)} to convert every single value. If you want to convert an array of values
	 * of a known type, consider using {@link #convert(Object[], Class, Converter)} for much better performance.
	 * 
	 * @param o
	 * @param to
	 * @return A T[] array without null elements
	 */
	@Nullable
	public static <T> T[] convertArray(final @Nullable Object[] o, final Class<T> to) {
		T[] converted = org.skriptlang.skript.lang.converter.Converters.convert(o, to);
		if (converted.length == 0) // no longer nullable with new converter classes
			return null;
		return converted;
	}
	
	/**
	 * Converts multiple objects into any of the given classes.
	 * 
	 * @param o
	 * @param to
	 * @param superType The component type of the returned array
	 * @return The converted array
	 */
	public static <T> T[] convertArray(final @Nullable Object[] o, final Class<? extends T>[] to, final Class<T> superType) {
		return org.skriptlang.skript.lang.converter.Converters.convert(o, to, superType);
	}

	/**
	 * Strictly converts an array to a non-null array of the specified class.
	 * Uses registered {@link ch.njol.skript.registrations.Converters} to convert.
	 *
	 * @param original The array to convert
	 * @param to       What to convert {@code original} to
	 * @return {@code original} converted to an array of {@code to}
	 * @throws ClassCastException if one of {@code original}'s
	 * elements cannot be converted to a {@code to}
	 */
	public static <T> T[] convertStrictly(Object[] original, Class<T> to) throws ClassCastException {
		return org.skriptlang.skript.lang.converter.Converters.convertStrictly(original, to);
	}

	/**
	 * Strictly converts an object to the specified class
	 *
	 * @param original The object to convert
	 * @param to What to convert {@code original} to
	 * @return {@code original} converted to a {@code to}
	 * @throws ClassCastException if {@code original} could not be converted to a {@code to}
	 */
	public static <T> T convertStrictly(Object original, Class<T> to) throws ClassCastException {
		return org.skriptlang.skript.lang.converter.Converters.convertStrictly(original, to);
	}
	
	/**
	 * Tests whether a converter between the given classes exists.
	 * 
	 * @param from
	 * @param to
	 * @return Whether a converter exists
	 */
	public static boolean converterExists(final Class<?> from, final Class<?> to) {
		return org.skriptlang.skript.lang.converter.Converters.converterExists(from, to);
	}
	
	public static boolean converterExists(final Class<?> from, final Class<?>... to) {
		return org.skriptlang.skript.lang.converter.Converters.converterExists(from, to);
	}
	
	/**
	 * Gets a converter
	 * 
	 * @param from
	 * @param to
	 * @return the converter or null if none exist
	 */
	@Nullable
	public static <F, T> Converter<? super F, ? extends T> getConverter(final Class<F> from, final Class<T> to) {
		org.skriptlang.skript.lang.converter.Converter<F, T> converter =
			org.skriptlang.skript.lang.converter.Converters.getConverter(from, to);
		if (converter == null)
			return null;
		return (Converter<F, T>) converter::convert;
	}
	
	/**
	 * Gets a converter that has been registered before.
	 * 
	 * @param from
	 * @param to
	 * @return The converter info or null if no converters were found.
	 */
	@Nullable
	public static <F, T> ConverterInfo<? super F, ? extends T> getConverterInfo(Class<F> from, Class<T> to) {
		org.skriptlang.skript.lang.converter.ConverterInfo<F, T> info =
			org.skriptlang.skript.lang.converter.Converters.getConverterInfo(from, to);
		if (info == null)
			return null;
		return new ConverterInfo<>(info.getFrom(), info.getTo(), info.getConverter()::convert, info.getFlags());
	}
	
	/**
	 * @param from
	 * @param to
	 * @param conv
	 * @return The converted array
	 * @throws ArrayStoreException if the given class is not a superclass of all objects returned by the converter
	 */
	public static <F, T> T[] convertUnsafe(final F[] from, final Class<?> to, final Converter<? super F, ? extends T> conv) {
		return org.skriptlang.skript.lang.converter.Converters.convertUnsafe(from, to, conv::convert);
	}
	
	public static <F, T> T[] convert(final F[] from, final Class<T> to, final Converter<? super F, ? extends T> conv) {
		return org.skriptlang.skript.lang.converter.Converters.convert(from, to, conv::convert);
	}
	
}
