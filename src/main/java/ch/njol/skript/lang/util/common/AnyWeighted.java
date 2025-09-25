package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.UnknownNullability;

/**
 * A provider for anything with a weight
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprWeight}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyWeighted extends AnyProvider {

    /**
     * @return This thing's weight
     */
    @UnknownNullability Number weight();

    /**
     * This is called before {@link #setWeight(Number)}.
     * If the result is false, setting the weight will never be attempted.
     *
     * @return Whether this supports being set
     */
    default boolean supportsWeightChange() {
        return false;
    }

    /**
     * The behaviour for changing this thing's weight, if possible.
     * If not possible, then {@link #supportsWeightChange()} should return false and this
     * may throw an error.
     *
     * @param weight The weight to change
     * @throws UnsupportedOperationException If this is impossible
     */
    default void setWeight(Number weight) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
