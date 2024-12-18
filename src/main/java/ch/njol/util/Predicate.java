package ch.njol.util;

import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
  boolean test(@Nullable T paramT);
}

