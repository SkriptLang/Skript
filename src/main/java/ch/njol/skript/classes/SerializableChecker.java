package ch.njol.skript.classes;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * @deprecated use {@link Predicate}
 */
@FunctionalInterface
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public interface SerializableChecker<T> extends ch.njol.util.Checker<T>, Predicate<T> {}
