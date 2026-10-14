package ch.njol.skript.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Result;

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
 * @deprecated Use {@link Result} instead, which makes a failure carrying no data a property of the
 * type rather than an invariant a caller has to uphold.
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
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

	/**
	 * Converts this to a {@link Result}, where a successful validation becomes a
	 * {@link Result.Success} carrying the data and the message as its warning, and a failed one a
	 * {@link Result.Failure} carrying the message.
	 *
	 * @return The equivalent result.
	 */
	@Contract(value = "-> new", pure = true)
	public @NotNull Result<T> toResult() {
		if (valid) {
			return Result.success(data, message);
		}
		return Result.failure(message != null ? message : "The validation failed.");
	}

	/**
	 * Converts a {@link Result} to a {@link ValidationResult}, for passing one to API which has
	 * not been migrated yet.
	 *
	 * @param result The result to convert.
	 * @param <T>    The type of the data.
	 * @return The equivalent validation result.
	 */
	@Contract(value = "_ -> new", pure = true)
	public static <T> @NotNull ValidationResult<T> of(@NotNull Result<T> result) {
		return new ValidationResult<>(result.isSuccess(), result.message(), result.value());
	}

}
