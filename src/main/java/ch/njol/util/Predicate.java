package ch.njol.util;

import javax.annotation.Nullable;

/**
 * @deprecated use {@link java.util.function.Predicate}. 
 */
@Deprecated(since = "2.11.0", forRemoval = true)
@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
  boolean test(@Nullable T paramT);
}

