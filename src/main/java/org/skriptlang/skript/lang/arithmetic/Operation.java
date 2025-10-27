package org.skriptlang.skript.lang.arithmetic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Pure_function">pure</a> binary operation
 * that takes two operands of types {@code L} and {@code R}, performs a calculation,
 * and returns a result of type {@code T}.
 *
 * @param <L> The class of the left operand.
 * @param <R> The class of the right operand.
 * @param <T> The return type of the operation.
 */
@FunctionalInterface
public interface Operation<L, R, T> extends BiFunction<L, R, T> {

	/**
	 * Performs the calculation for two given operands.
	 *
	 * @param left left operand
	 * @param right right operand
	 * @return result
	 */
	@Contract(pure = true)
	T calculate(@NotNull L left, @NotNull R right);

	@Override
	default T apply(L l, R r) {
		return calculate(l, r);
	}

}
