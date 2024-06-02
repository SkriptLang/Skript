package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.UnknownNullability;

/**
 * A provider for anything with a (text) 'display' name.
 * This is usually different from the thing's actual name
 * (e.g. the name is used internally, the display name is shown to viewers)
 * but some may not have both properties, or may use them interchangeably.
 * <br/>
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprName}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyDisplayNamed extends AnyProvider {

	/**
	 * @return This thing's name
	 */
	@UnknownNullability String displayName();

	/**
	 * This is called before {@link #setDisplayName(String)}.
	 * If the result is false, setting the name will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean displayNameSupportsChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's name, if possible.
	 * If not possible, then {@link #displayNameSupportsChange()} should return false and this
	 * may throw an error.
	 *
	 * @param name The name to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setDisplayName(String name) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
