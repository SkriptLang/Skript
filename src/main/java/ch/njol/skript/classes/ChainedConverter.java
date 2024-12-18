package ch.njol.skript.classes;

import org.jetbrains.annotations.Nullable;

import org.skriptlang.skript.lang.converter.Converters;

/**
 * Used to chain convertes to build a single converter. This is automatically created when a new converter is added.
 * 
 * @author Peter Güttinger
 * @param <F> same as Converter's <F> (from)
 * @param <M> the middle type, i.e. the type the first converter converts to and the second converter comverts from.
 * @param <T> same as Converter's <T> (to)
 * @see Converters#registerConverter(Class, Class, Converter)
 * @see Converter
 * @deprecated Use {@link org.skriptlang.skript.lang.converter.Converter}
 */
@Deprecated
public final class ChainedConverter<F, M, T> implements Converter<F, T> {
	
	private final Converter<? super F, ? extends M> first;
	private final Converter<? super M, ? extends T> second;
	
	public ChainedConverter(final Converter<? super F, ? extends M> first, final Converter<? super M, ? extends T> second) {
		assert first != null;
		assert second != null;
		this.first = first;
		this.second = second;
	}
	
	@SuppressWarnings("unchecked")
	public static <F, M, T> ChainedConverter<F, M, T> newInstance(final Converter<? super F, ?> first, final Converter<?, ? extends T> second) {
		return new ChainedConverter<>((Converter<? super F, ? extends M>) first, (Converter<? super M, ? extends T>) second);
	}
	
	@Override
	@Nullable
	public T convert(final F f) {
		final M m = first.convert(f);
		if (m == null)
			return null;
		return second.convert(m);
	}
	
	@Override
	public String toString() {
		return "ChainedConverter{first=" + first + ",second=" + second + "}";
	}
}
