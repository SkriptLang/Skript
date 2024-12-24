package ch.njol.util;

import javax.annotation.Nullable;

/**
 * @author Peter G�ttinger
 *
 */
public abstract interface Predicate<T> {
  public abstract boolean test(@Nullable T paramT);
}

