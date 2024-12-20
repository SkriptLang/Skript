package org.skriptlang.skript.lang.converter;

import org.jetbrains.annotations.Nullable;

/**
 * A Chained Converter is very similar to a regular {@link Converter}.
 * They are used when it is not possible to directly convert from Type A to Type B.
 * Instead, a "middle man" is used that makes it possible to convert from Type A to Type B.
 * What this means is that Type A is converted to the "middle man" type.
 * Then, that "middle man" type is converted to Type B.
 *
 * Of course, multiple Chained Converters can be chained together to provide far more conversion possibilities.
 * In these cases, you might have, for example, another "middle man" between
 *  this Chained Converter's Type A and "middle man"
 *
 * By using conversion chains, Skript can convert an object into a different type that seemingly has no relation.
 *
 * @param <F> The type to convert from.
 * @param <M> A middle type that is needed for converting 'from' to 'to'.
 *                'from' will be converted to this type, and then this type will be converted to 'to'.
 * @param <T> The type to convert to.
 */
final class ChainedConverter<F, M, T> implements Converter<F, T> {

	private final ConverterInfo<F, M> first;
	private final ConverterInfo<M, T> second;

	ChainedConverter(ConverterInfo<F, M> first, ConverterInfo<M, T> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	@Nullable
	public T convert(F from) {
		M middle = first.getConverter().convert(from);
		if (middle == null) {
			return null;
		}
		return second.getConverter().convert(middle);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("ChainedConverter{(");
		if (first.getConverter() instanceof ChainedConverter) {
			builder.append(first.getConverter());
		} else {
			builder.append(first.getFrom()).append(" -> ").append(first.getTo());
		}

		builder.append(") -> (");

		if (second.getConverter() instanceof ChainedConverter) {
			builder.append(second.getConverter());
		} else {
			builder.append(second.getFrom()).append(" -> ").append(second.getTo());
		}
		builder.append(")}");

		return builder.toString();
	}

}
