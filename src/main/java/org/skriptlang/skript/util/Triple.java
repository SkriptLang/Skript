package org.skriptlang.skript.util;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a read-only pair of three values, which may be null.
 *
 * @param first The first value.
 * @param second The second value.
 * @param third The third value.
 * @param <A> The type of the first value.
 * @param <B> The type of the second value.
 * @param <C> The type of the third value.
 */
public record Triple<A, B, C>(
	@Nullable A first,
	@Nullable B second,
	@Nullable C third
) {

}
