package ch.njol.util;

import javax.annotation.Nullable;

/**
 * @deprecated use {@link java.util.function.Predicate}. (Removal 2.13.0)
 */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
  boolean test(@Nullable T paramT);
}

