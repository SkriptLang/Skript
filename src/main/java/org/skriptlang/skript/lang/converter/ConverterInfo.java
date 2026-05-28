package org.skriptlang.skript.lang.converter;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

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

	public ConverterInfo(
		@NotNull Class<F> from,
		@NotNull Class<T> to,
		@NotNull Converter<F, T> converter,
		int flags
	) {
		Preconditions.checkNotNull(from, "Cannot convert from nothing to something! (from is null)");
		Preconditions.checkNotNull(to, "Cannot convert from something to nothing! (to is null)");
		Preconditions.checkNotNull(converter, "Cannot convert using a null converter!");
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
