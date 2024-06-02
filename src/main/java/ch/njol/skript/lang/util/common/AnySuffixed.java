package ch.njol.skript.lang.util.common;

/**
 * A provider for anything with a (text) suffix, e.g. a player.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprSuffix}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnySuffixed extends AnyProvider {

	/**
     * @return This thing's suffix
	 */
	String suffix();

	/**
	 * This is called before {@link #setSuffix(String)}.
	 * If the result is false, setting the suffix will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean suffixSupportsChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's suffix, if possible.
	 * If not possible, then {@link #suffixSupportsChange()} should return false and this
	 * may throw an error.
	 *
	 * @param suffix The suffix to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setSuffix(String suffix) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
