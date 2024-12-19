package org.skriptlang.skript.lang.arithmetic;

/**
 * @param <T> The type of the difference
 * @param <R> The return type of the difference
 */
public final class DifferenceInfo<T, R> {

	private final Class<T> type;
	private final Class<R> returnType;
	private final Operation<T, T, R> operation;

	public DifferenceInfo(Class<T> type, Class<R> returnType, Operation<T, T, R> operation) {
		this.type = type;
		this.returnType = returnType;
		this.operation = operation;
	}

	public Class<T> getType() {
		return type;
	}

	public Class<R> getReturnType() {
		return returnType;
	}

	public Operation<T, T, R> getOperation() {
		return operation;
	}

}
