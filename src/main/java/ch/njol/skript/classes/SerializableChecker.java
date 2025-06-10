package ch.njol.skript.classes;

import java.util.function.Predicate;

/**
 * @deprecated use {@link Predicate} instead.
 */
@FunctionalInterface
@Deprecated(since = "2.10.0", forRemoval = true)
@SuppressWarnings("removal")
public interface SerializableChecker<T> extends ch.njol.util.Checker<T>, Predicate<T> {}
