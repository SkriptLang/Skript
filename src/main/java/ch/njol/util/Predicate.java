package ch.njol.util;

import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * @deprecated use {@link java.util.function.Predicate}
 */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
  boolean test(@Nullable T paramT);
}

