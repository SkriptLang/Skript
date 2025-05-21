package ch.njol.skript.bukkitutil;

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.util.ValidationResult;
import org.bukkit.NamespacedKey;

/**
 * Utility class for {@link NamespacedKey}
 */
public class NamespacedUtils {

	public static final String NAMEDSPACED_FORMAT_MESSAGE =
		new ArgsMessage("misc.namespacedutils.format").getValue();

	public static boolean isValidChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
	}

	/**
	 * Check if the {@code string} is valid for a {@link NamespacedKey} and get a {@link ValidationResult}
	 * containing if it's valid, an error or warning message and the resulting {@link NamespacedKey}.
	 * @param string The {@link String} to check.
	 * @return {@link ValidationResult}.
	 */
	public static ValidationResult<NamespacedKey> checkValidator(String string) {
		if (string.length() > Short.MAX_VALUE)
			return new ValidationResult<>(false, "A namespaced key can not be longer than " + Short.MAX_VALUE + " characters.");
		String[] split = string.split(":");
		if (split.length > 2)
			return new ValidationResult<>(false, "A namespaced key can not have more than one ':'.");

		String key = split.length == 2 ? split[1] : split[0];
		if (key.isEmpty())
			return new ValidationResult<>(false, "The key cannot be empty.");
		for (char character : key.toCharArray()) {
			if (!isValidChar(character)) {
				return new ValidationResult<>(false, "Invalid character '" + character + "'.");
			}
		}

		NamespacedKey namespacedKey;
		boolean emptyNamespace = false;
		if (split.length == 2) {
			String namespace = split[0];
			if (!namespace.isEmpty()) {
				for (char character : namespace.toCharArray()) {
					if (!isValidChar(character)) {
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

		if (emptyNamespace)
			return new ValidationResult<>(
				true,
				"The namespace section of the key is empty. Consider removing the ':'.",
				namespacedKey);
		return new ValidationResult<>(true, namespacedKey);
	}

	/**
	 * Check if {@code string} is valid for a {@link NamespacedKey}.
	 * @param string The {@link String} to check.
	 * @return {@code True} if valid, otherwise {@code false}.
	 */
	public static boolean isValid(String string) {
		return checkValidator(string).isValid();
	}


}
