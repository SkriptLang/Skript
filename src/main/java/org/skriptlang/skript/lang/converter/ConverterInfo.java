package org.skriptlang.skript.lang.converter;

/**
 * Holds information about a {@link Converter}.
 *
 * @param <F> The type to convert from.
 * @param <T> The type to convert to.
 */
public final class ConverterInfo<F, T> {

	private final Class<F> from;
	private final Class<T> to;
	private final Converter<F, T> converter;
	private final int flags;

	public ConverterInfo(Class<F> from, Class<T> to, Converter<F, T> converter, int flags) {
		this.from = from;
		this.to = to;
		this.converter = converter;
		this.flags = flags;
	}

	public Class<F> getFrom() {
		return from;
	}

	public Class<T> getTo() {
		return to;
	}

	public Converter<F, T> getConverter() {
		return converter;
	}

	public int getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		return "ConverterInfo{from=" + from + ",to=" + to + ",converter=" + converter + ",flags=" + flags + "}";
	}

}
