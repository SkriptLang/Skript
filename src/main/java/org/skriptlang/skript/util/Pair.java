package org.skriptlang.skript.util;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a read-only pair of two values, which may be null.
 *
 * @param first The first value.
 * @param second The second value.
 * @param <A> The type of the first value.
 * @param <B> The type of the second value.
 */
public record Pair<A, B>(
	@Nullable A first,
	@Nullable B second
) {

}
