package ch.njol.skript.bukkitutil;

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.ValidationResult;
import org.bukkit.NamespacedKey;

/**
 * Utility class for {@link NamespacedKey}
 */
public class NamespacedUtils {

	public static final Message NAMEDSPACED_FORMAT_MESSAGE = new ArgsMessage("misc.namespacedutils.format");

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
	 */
	public static ValidationResult<NamespacedKey> checkValidation(String string) {
		if (string.length() > Short.MAX_VALUE)
			return new ValidationResult<>(false, "A namespaced key can not be longer than " + Short.MAX_VALUE + " characters.");
		String[] split = string.split(":");
		if (split.length > 2)
			return new ValidationResult<>(false, "A namespaced key can not have more than one ':'.");

		String key = split.length == 2 ? split[1] : split[0];
		if (key.isEmpty())
			return new ValidationResult<>(false, "The key cannot be empty.");
		for (char character : key.toCharArray()) {
			if (!isValidKeyChar(character)) {
				return new ValidationResult<>(false, "Invalid character '" + character + "'.");
			}
		}

		NamespacedKey namespacedKey;
		boolean emptyNamespace = false;
		if (split.length == 2) {
			String namespace = split[0];
			if (!namespace.isEmpty()) {
				for (char character : namespace.toCharArray()) {
					if (!isValidNamespaceChar(character)) {
						return new ValidationResult<>(false, "Invalid character '" + character + "'.");
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
			return new ValidationResult<>(
				true,
				"The namespace section of the key is empty. Consider removing the ':'.",
				namespacedKey);
		}
		return new ValidationResult<>(true, namespacedKey);
	}

	/**
	 * Check if {@code string} is valid for a {@link NamespacedKey}.
	 * @param string The {@link String} to check.
	 * @return {@code True} if valid, otherwise {@code false}.
	 */
	public static boolean isValid(String string) {
		return checkValidation(string).valid();
	}

}
