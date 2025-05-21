package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a validation check.
 * <p>
 *     This class stores whether a check was valid,
 *     An optional message explaining the result (i.e. an error or warning message),
 *     And optional data returned from the check if it successfully passed.
 * </p>
 * @param <T> The type of data returned from a successful validation.
 */
public class ValidationResult<T> {

	private final boolean valid;
	private final @Nullable String message;
	private final @Nullable T data;

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
	public ValidationResult(boolean valid, @Nullable String message, @Nullable T data) {
		this.valid = valid;
		this.message = message;
		this.data = data;
	}

	/**
	 * Returns whether the validation was successful.
	 * @return {@code True} if valid, otherwise {@code false}.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Returns the optional message attached to this validation result.
	 * Can be represented as an error or warning message.
	 * @return The message, or {@code null} if not provided.
	 */
	public @Nullable String getMessage() {
		return message;
	}

	/**
	 * Returns the optional data resulting from the validation.
	 * @return The result data, or {@code null} if not provided.
	 */
	public @Nullable T getData() {
		return data;
	}

}
