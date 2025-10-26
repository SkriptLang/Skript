package org.skriptlang.skript.lang.arithmetic;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * Represents a binary operation including information about the types of its
 * operands and its return type.
 *
 * @param left type of the left operand
 * @param right type of the right operand
 * @param returnType return type of the operation
 * @param operation {@link Operation} that performs the actual calculation
 * @param <L> type of the left operand
 * @param <R> type of the right operand
 * @param <T> return type of the operation
 */
public record OperationInfo<L, R, T>(Class<L> left, Class<R> right, Class<T> returnType, Operation<L, R, T> operation) {

	public OperationInfo(
		@NotNull Class<L> left,
		@NotNull Class<R> right,
		@NotNull Class<T> returnType,
		@NotNull Operation<L, R, T> operation
	) {
		Preconditions.checkNotNull(left, "Cannot do arithmetic with nothing and something! (left is null)");
		Preconditions.checkNotNull(right, "Cannot do arithmetic with something and nothing! (right is null)");
		Preconditions.checkNotNull(returnType, "Cannot have nothing as the result of arithmetic! (returnType is null)");
		Preconditions.checkNotNull(operation, "Cannot do arithmetic with a null operation!");
		this.left = left;
		this.right = right;
		this.returnType = returnType;
		this.operation = operation;
	}

	@Deprecated(since = "2.13", forRemoval = true)
	public Class<L> getLeft() {
		return left;
	}

	@Deprecated(since = "2.13", forRemoval = true)
	public Class<R> getRight() {
		return right;
	}

	@Deprecated(since = "2.13", forRemoval = true)
	public Class<T> getReturnType() {
		return returnType;
	}

	@Deprecated(since = "2.13", forRemoval = true)
	public Operation<L, R, T> getOperation() {
		return operation;
	}

	/**
	 * Attempts to create a new {@link OperationInfo} that accepts
	 * operands of types {@code fromLeft} and {@code fromRight},
	 * while maintaining the original return type and calculation
	 * of this operation info.
	 *
	 * @param fromLeft type for the new left operand
	 * @param fromRight type for the new right operand
	 * @return new {@link OperationInfo} capable of handling the specified input types if
	 *         converters for them exist, else null
	 * @param <L2> type of the new left operand
	 * @param <R2> type of the new right operand
	 */
	public <L2, R2> @Nullable OperationInfo<L2, R2, T> getConverted(Class<L2> fromLeft, Class<R2> fromRight) {
		return getConverted(fromLeft, fromRight, returnType);
	}

	/**
	 * Attempts to create a new {@link OperationInfo} that accepts
	 * operands of types {@code fromLeft} and {@code fromRight},
	 * and returns {@code toReturnType}, while maintaining the
	 * calculation of this operation info.
	 *
	 * @param fromLeft type for the new left operand
	 * @param fromRight type for the new right operand
	 * @param toReturnType new return type
	 * @return new {@link OperationInfo} capable of handling the specified input types and
	 *         return type if converters for them exist, else null
	 * @param <L2> type of the new left operand
	 * @param <R2> type of the new right operand
	 */
	public <L2, R2, T2> @Nullable OperationInfo<L2, R2, T2> getConverted(Class<L2> fromLeft, Class<R2> fromRight,
			Class<T2> toReturnType) {
		if (fromLeft == Object.class || fromRight == Object.class)
			return null;
		if (!Converters.converterExists(fromLeft, left)
			|| !Converters.converterExists(fromRight, right)
			|| !Converters.converterExists(returnType, toReturnType))
			return null;
		return new OperationInfo<>(fromLeft, fromRight, toReturnType, (left, right) -> {
			L convertedLeft = Converters.convert(left, this.left);
			R convertedRight = Converters.convert(right, this.right);
			if (convertedLeft == null || convertedRight == null)
				return null;
			T result = operation.calculate(convertedLeft, convertedRight);
			return Converters.convert(result, toReturnType);
		});
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("left", left)
			.add("right", right)
			.add("returnType", returnType)
			.toString();
	}

}
