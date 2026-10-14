package ch.njol.skript.bukkitutil;

import ch.njol.skript.localization.Message;
import ch.njol.skript.util.ValidationResult;
import org.skriptlang.skript.util.Result;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer;

/**
 * Utility class for {@link NamespacedKey}
 */
public class NamespacedUtils {

	public static final Message NAMEDSPACED_FORMAT_MESSAGE = new Message("misc.namespacedutils.format");

	/**
	 * Check if {@code character} is a valid {@link Character} for the namespace section of a {@link NamespacedKey}.
	 * @param character The {@link Character} to check.
	 * @return {@code True} if valid, otherwise {@code false}.
	 */
	public static boolean isValidNamespaceChar(char character) {
		return (character >= 'a' && character <= 'z') || (character >= '0' && character <= '9') || character == '.' || character == '_' || character == '-';
	}

	/**
	 * Check if {@code character} is a valid {@link Character} for the key section of a {@link NamespacedKey}.
	 * @param character The {@link Character} to check.
	 * @return {@code True} if valid, otherwise {@code false}.
	 */
	public static boolean isValidKeyChar(char character) {
		return isValidNamespaceChar(character) || character == '/';
	}

	/**
	 * Check if the {@code string} is valid for a {@link NamespacedKey} and get a {@link ValidationResult}
	 * containing if it's valid, an error or warning message and the resulting {@link NamespacedKey}.
	 * @param string The {@link String} to check.
	 * @return {@link ValidationResult}.
	 * @deprecated Use {@link #validate(String)} instead.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	@SuppressWarnings("removal")
	public static ValidationResult<NamespacedKey> checkValidation(String string) {
		return ValidationResult.of(validate(string));
	}

	/**
	 * Checks whether {@code string} is valid for a {@link NamespacedKey}.
	 *
	 * @param string The {@link String} to check.
	 * @return The key, or why {@code string} is not one. A successful result may carry a warning
	 * 	about the key it produced.
	 */
	public static Result<NamespacedKey> validate(String string) {
		if (string.length() > Short.MAX_VALUE)
			return Result.failure("A namespaced key can not be longer than " + Short.MAX_VALUE + " characters.");
		String[] split = string.split(":");
		if (split.length > 2)
			return Result.failure("A namespaced key can not have more than one ':'.");

		String key = split.length == 2 ? split[1] : split[0];
		if (key.isEmpty())
			return Result.failure("The key cannot be empty.");
		for (char character : key.toCharArray()) {
			if (!isValidKeyChar(character)) {
				return Result.failure("Invalid character '" + character + "'.");
			}
		}

		NamespacedKey namespacedKey;
		boolean emptyNamespace = false;
		if (split.length == 2) {
			String namespace = split[0];
			if (!namespace.isEmpty()) {
				for (char character : namespace.toCharArray()) {
					if (!isValidNamespaceChar(character)) {
						return Result.failure("Invalid character '" + character + "'.");
					}
				}
				namespacedKey = new NamespacedKey(namespace, key);
			} else {
				emptyNamespace = true;
				namespacedKey = NamespacedKey.minecraft(key);
			}
		} else {
			namespacedKey = NamespacedKey.minecraft(key);
		}

		if (emptyNamespace) {
			return Result.success(
				namespacedKey,
				"The namespace section of the key is empty. Consider removing the ':'.");
		}
		return Result.success(namespacedKey);
	}

	/**
	 * A helper method to run {@link #checkValidation} and send the appropriate runtime errors/warnings.
	 * @param string The string to parse as a namespaced key.
	 * @param producer The producer from which to send runtime errors.
	 * @return The key, if parsed without errors, otherwise null.
	 */
	public static @Nullable NamespacedKey checkValidationAndSend(String string, RuntimeErrorProducer producer) {
		return switch (NamespacedUtils.validate(string)) {
			case Result.Failure<NamespacedKey>(String error) -> {
				producer.error(error + ". " + NamespacedUtils.NAMEDSPACED_FORMAT_MESSAGE);
				yield null;
			}
			case Result.Success<NamespacedKey>(NamespacedKey key, String warning) -> {
				if (warning != null) {
					producer.warning(warning);
				}
				yield key;
			}
		};
	}

	/**
	 * Check if {@code string} is valid for a {@link NamespacedKey}.
	 * @param string The {@link String} to check.
	 * @return {@code True} if valid, otherwise {@code false}.
	 */
	public static boolean isValid(String string) {
		return validate(string) instanceof Result.Success<NamespacedKey>;
	}

}
