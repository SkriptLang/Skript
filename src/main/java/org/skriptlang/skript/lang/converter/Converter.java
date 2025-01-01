package org.skriptlang.skript.lang.converter;

import org.jetbrains.annotations.Nullable;

/**
 * Used to convert an object to a different type.
 *
 * @param <F> The type to convert from.
 * @param <T> The type to convert to.
 */
@FunctionalInterface
public interface Converter<F, T> {

	/**
	 * A Converter flag declaring that this Converter can be any part of a chain.
	 */
	int ALL_CHAINING = 0;

	/**
	 * A Converter flag declaring this Converter cannot be chained to another Converter.
	 * This means that this Converter must be the beginning of a chain.
	 * <br/>
	 * <strong>Note</strong>: unchecked casts are not permitted before this converter
	 * (e.g. {@code Object} to {@param <F>}).
	 */
	int NO_LEFT_CHAINING = 1;

	/**
	 * A Converter flag declaring that another Converter cannot be chained to this Converter.
	 * This means that this Converter must be the end of a chain.
	 * <br/>
	 * <strong>Note</strong>: unchecked casts are not permitted after this converter
	 * (e.g. {@param <T>} to {@param <? extends T>}).
	 */
	int NO_RIGHT_CHAINING = 2;

	/**
	 * A Converter flag declaring that the input/output of this can use an unchecked cast,
	 * when combined with {@link #NO_LEFT_CHAINING} or {@link #NO_RIGHT_CHAINING}.
	 * <br/>
	 * An unchecked cast would be {@code Number -> Integer}. (Not all numbers are integers, some are floats!)
	 * <br/>
	 * <br/>
	 * When combined with {@link #NO_RIGHT_CHAINING} the output can be conformed with an unchecked cast,
	 * e.g. {@code String -> Number (-> cast Integer)}.
	 * <br/>
	 * When combined with {@link #NO_RIGHT_CHAINING} the output can be conformed with an unchecked cast,
	 * e.g. {@code (cast Object ->) Integer -> String}.
	 */
	int ALLOW_UNSAFE_CASTS = 4;

	/**
	 * A Converter flag declaring that this Converter cannot be a part of a chain.
	 */
	int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;

	/**
	 * Converts an object using this Converter.
	 * @param from The object to convert.
	 * @return The converted object.
	 */
	@Nullable T convert(F from);

}
