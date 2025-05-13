package ch.njol.util;

import javax.annotation.Nullable;

/**
 * @deprecated use {@link java.util.function.Predicate} instead.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
  boolean test(@Nullable T paramT);
}

