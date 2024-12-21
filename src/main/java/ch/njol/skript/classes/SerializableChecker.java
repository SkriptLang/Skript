package ch.njol.skript.classes;

import java.util.function.Predicate;

/**
 * @deprecated use {@link Predicate}
 */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface SerializableChecker<T> extends ch.njol.util.Checker<T>, Predicate<T> {}
