package ch.njol.skript.registrations;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <h2>WARNING! This class has been removed in this update.</h2>
 * This class stub has been left behind to prevent loading errors from outdated addons,
 * but its functionality has been largely removed.
 *
 * @deprecated Use {@link org.skriptlang.skript.lang.converter.Converters} instead.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@SuppressWarnings("removal")
public abstract class Converters {

	static {
		Utils.loadedRemovedClassWarning(Converters.class);
	}

	@SuppressWarnings("unchecked")
	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> List<ConverterInfo<?, ?>> getConverters() {
		return org.skriptlang.skript.lang.converter.Converters.getConverterInfos().stream()
			.map(unknownInfo -> {
				org.skriptlang.skript.lang.converter.ConverterInfo<F, T> info =
					(org.skriptlang.skript.lang.converter.ConverterInfo<F, T>) unknownInfo;
				return new ConverterInfo<>(info.getFrom(), info.getTo(), info.getConverter(), info.getFlags());
			})
			.collect(Collectors.toList());
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> void registerConverter(Class<F> from, Class<T> to, Converter<F, T> converter) {
		registerConverter(from, to, converter, 0);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> void registerConverter(Class<F> from, Class<T> to, Converter<F, T> converter, int options) {
		org.skriptlang.skript.lang.converter.Converters.registerConverter(from, to, converter::convert, options);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> T convert(@Nullable F o, Class<T> to) {
		return org.skriptlang.skript.lang.converter.Converters.convert(o, to);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> T convert(@Nullable F o, Class<? extends T>[] to) {
		return org.skriptlang.skript.lang.converter.Converters.convert(o, to);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <T> T[] convertArray(@Nullable Object[] o, Class<T> to) {
		T[] converted = org.skriptlang.skript.lang.converter.Converters.convert(o, to);
		if (converted.length == 0) // no longer nullable with new converter classes
			return null;
		return converted;
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <T> T[] convertArray(@Nullable Object[] o, Class<? extends T>[] to,
									   Class<T> superType) {
		return org.skriptlang.skript.lang.converter.Converters.convert(o, to, superType);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <T> T[] convertStrictly(Object[] original, Class<T> to) throws ClassCastException {
		return org.skriptlang.skript.lang.converter.Converters.convertStrictly(original, to);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <T> T convertStrictly(Object original, Class<T> to) throws ClassCastException {
		return org.skriptlang.skript.lang.converter.Converters.convertStrictly(original, to);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static boolean converterExists(Class<?> from, Class<?> to) {
		return org.skriptlang.skript.lang.converter.Converters.converterExists(from, to);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static boolean converterExists(Class<?> from, Class<?>... to) {
		return org.skriptlang.skript.lang.converter.Converters.converterExists(from, to);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> Converter<? super F, ? extends T> getConverter(Class<F> from, Class<T> to) {
		org.skriptlang.skript.lang.converter.Converter<F, T> converter =
			org.skriptlang.skript.lang.converter.Converters.getConverter(from, to);
		if (converter == null)
			return null;
		return (Converter<F, T>) converter::convert;
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> ConverterInfo<? super F, ? extends T> getConverterInfo(Class<F> from, Class<T> to) {
		org.skriptlang.skript.lang.converter.ConverterInfo<F, T> info =
			org.skriptlang.skript.lang.converter.Converters.getConverterInfo(from, to);
		if (info == null)
			return null;
		return new ConverterInfo<>(info.getFrom(), info.getTo(), info.getConverter()::convert, info.getFlags());
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> T[] convertUnsafe(F[] from, Class<?> to,
										   Converter<? super F, ? extends T> conv) {
		return org.skriptlang.skript.lang.converter.Converters.convertUnsafe(from, to, conv::convert);
	}

	@Deprecated(since = "2.10.0", forRemoval = true)
	public static <F, T> T[] convert(F[] from, Class<T> to, Converter<? super F, ? extends T> conv) {
		return org.skriptlang.skript.lang.converter.Converters.convert(from, to, conv::convert);
	}

}
