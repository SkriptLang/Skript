package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents a read-only pair of two values, which may not be null.
 * Throws a {@link NullPointerException} if the first or second value is null.
 *
 * @param first The first value.
 * @param second The second value.
 * @param <A> The type of the first value.
 * @param <B> The type of the second value.
 */
public record NotNullPair<A, B>(
	@NotNull A first,
	@NotNull B second
) {

	/**
	 * @throws NullPointerException If the first or second value is null.
	 */
	public NotNullPair {
		Preconditions.checkNotNull(first, "first passed value is null");
		Preconditions.checkNotNull(second, "second passed value is null");
	}

}
