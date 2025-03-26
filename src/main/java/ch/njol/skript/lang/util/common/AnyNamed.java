package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.UnknownNullability;

/**
 * A provider for anything with a (text) name.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprName}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyNamed extends AnyProvider {

	/**
	 * @return This thing's name
	 */
	@UnknownNullability String name();

	/**
	 * This is called before {@link #setName(String)}.
	 * If the result is false, setting the name will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsNameChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's name, if possible.
	 * If not possible, then {@link #supportsNameChange()} should return false and this
	 * may throw an error.
	 *
	 * @param name The name to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setName(String name) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
