package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a read-only pair of three values, which may not be null.
 * Throws a {@link NullPointerException} if the first, second or third value is null.
 *
 * @param first The first value.
 * @param second The second value.
 * @param third The third value.
 * @param <A> The type of the first value.
 * @param <B> The type of the second value.
 * @param <C> The type of the third value.
 */
public record NotNullTriple<A, B, C>(
	@NotNull A first,
	@NotNull B second,
	@NotNull C third
) {

	/**
	 * @throws NullPointerException If the first, second or third value is null.
	 */
	public NotNullTriple {
		Preconditions.checkNotNull(first, "first passed value is null");
		Preconditions.checkNotNull(second, "second passed value is null");
		Preconditions.checkNotNull(third, "third passed value is null");
	}

}
