package org.skriptlang.skript.lang.arithmetic;

/**
 * Represents operation for calculating a "difference" between two values of the same type.
 * <p>
 * e.g. difference between timestamps.
 *
 * @param type the type of the operands
 * @param returnType the return type of the difference operation
 * @param operation operation that calculates the difference
 * @param <T> the type of the operands
 * @param <R> the return type of the difference operation
 */
public record DifferenceInfo<T, R>(Class<T> type, Class<R> returnType, Operation<T, T, R> operation) {
}
