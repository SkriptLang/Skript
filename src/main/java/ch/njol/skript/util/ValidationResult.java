package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a validation check.
 * <p>
 * 		This record stores whether a check was valid,
 * 		an optional message explaining the result (i.e., an error or warning message),
 * 		and optional data returned from the check if it successfully passed.
 * </p>
 *
 * @param valid Whether the validation was successful.
 * @param message An optional message describing the result.
 * @param data Optional data returned from the validation.
 * @param <T> The type of data returned from a successful validation.
 */
public record ValidationResult<T>(
	boolean valid,
	@Nullable String message,
	@Nullable T data
) {

	/**
	 * Constructs a {@link ValidationResult} with only a validity flag.
	 * @param valid Whether the validation was successful.
	 */
	public ValidationResult(boolean valid) {
		this(valid, null, null);
	}

	/**
	 * Constructs a {@link ValidationResult} with a validity flag and message.
	 * @param valid Whether the validation was successful.
	 * @param message An optional message describing the result.
	 */
	public ValidationResult(boolean valid, @Nullable String message) {
		this(valid, message, null);
	}

	/**
	 * Constructs a {@link ValidationResult} with a validity flag and result data.
	 * @param valid Whether the validation was successful.
	 * @param data Optional data returned from the validation.
	 */
	public ValidationResult(boolean valid, @Nullable T data) {
		this(valid, null, data);
	}

	/**
	 * Constructs a {@link ValidationResult} with a validity flag, message and result data.
	 * @param valid Whether the validation was successful.
	 * @param message An optional message describing the result.
	 * @param data Optional data returned from the validation.
	 */
	public ValidationResult {}

}
