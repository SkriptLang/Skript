package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.NotNull;

public interface Function<T> {

	/**
	 * Executes this function with the given parameters.
	 *
	 * @param event The event that is associated with this function execution.
	 * @param arguments The arguments to execute the function with.
	 * @return The return value.
	 */
	T execute(FunctionEvent<?> event, FunctionArguments arguments);

	/**
	 * @return The signature represented by this function.
	 */
	@NotNull Signature<T> signature();

	/**
	 * @return The return type of this signature.
	 */
	default Class<T> returnType() {
		return signature().returnType();
	}

}
