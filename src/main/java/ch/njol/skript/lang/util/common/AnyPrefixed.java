package ch.njol.skript.lang.util.common;

/**
 * A provider for anything with a (text) prefix, e.g. a player.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprPrefix}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyPrefixed extends AnyProvider {

	/**
     * @return This thing's prefix
	 */
	String prefix();

	/**
	 * This is called before {@link #setPrefix(String)}.
	 * If the result is false, setting the prefix will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean prefixSupportsChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's prefix, if possible.
	 * If not possible, then {@link #prefixSupportsChange()} should return false and this
	 * may throw an error.
	 *
	 * @param prefix The prefix to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setPrefix(String prefix) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
