package ch.njol.skript.lang;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an expression that can be simplified to a {@link Literal} at parse-time.
 *
 * @param <T> The type of the literal.
 */
@FunctionalInterface
public interface Simplifiable<T> {

    /**
     * Simplifies the expression to a {@link Literal} at parse-time.
     *
     * @return the simplified expression if it can be simplified
     */
    @NotNull Expression<? extends T> simplified();

}