package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.util.Pair;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.NamespacedKey;

import java.util.Set;

public class NamespacedUtils {

	private static final Set<Character> LEGAL_NAMESPACE_CHARS = Sets.newHashSet(ArrayUtils.toObject("abcdefghijklmnopqrstuvwxyz0123456789._-/".toCharArray()));

	/**
	 * Gets a namespaced key. This method will try to get existing keys first, but if that fails
	 * it will create the key in Skript's namespace.
	 *
	 * @param key the unparsed key
	 * @return the resulting NamespacedKey
	 */
	public static NamespacedKey getNamespacedKey(String key) {
		NamespacedKey namespacedKey = NamespacedKey.fromString(key, Skript.getInstance());
		if (namespacedKey != null)
			return namespacedKey;

        return createNamespacedKey(key);
	}

	/**
	 * Creates a namespaced key in Skript's namespace encoded to avoid the character limitations of a normal key.
	 * This key will be created in Skript's namespace.
	 *
	 * @param key The key to use
	 * @return a NamespacedKey with the encoded key in corresponding Namespace
	 */
	public static NamespacedKey createNamespacedKey(String key) {
		StringBuilder encodedKeyBuilder = new StringBuilder();
		// keys must be all lowercase
		key = key.toLowerCase().replace(' ', '_');
		int keyLength = key.length();
		for (int i = 0; i < keyLength; i++) {
			char currentChar = key.charAt(i);
			// if this character is legal to use in a namespace key
			if (LEGAL_NAMESPACE_CHARS.contains(currentChar)) {
				// if the original string had a ".x" in it, we need to escape it
				// so decoding doesn't think it's a hex sequence
				if (currentChar == '.' && key.charAt(i + 1) == 'x') {
					i += 1; // skip the "x"
					encodedKeyBuilder.append(".x");
					encodedKeyBuilder.append(Integer.toHexString('.'));
					encodedKeyBuilder.append(".x");
					encodedKeyBuilder.append(Integer.toHexString('x'));
					// if we're not at the end and the next char is a legal char, add the trailing dot
					// to represent the end of the hex sequence
					if (i != (keyLength - 1) && LEGAL_NAMESPACE_CHARS.contains(key.charAt(i + 1)))
						encodedKeyBuilder.append('.');
				} else {
					// we are dealing with a legal character, so we can just append it
					encodedKeyBuilder.append(currentChar);
				}
			} else {
				// add ".x(hex code)" to the encoded key
				encodedKeyBuilder.append(".x");
				encodedKeyBuilder.append(Integer.toHexString(currentChar));
				// only add the trailing dot if the next character is a legal character
				if (i != (keyLength - 1) && LEGAL_NAMESPACE_CHARS.contains(key.charAt(i + 1)))
					encodedKeyBuilder.append('.');
			}
		}
		return NamespacedKey.fromString(encodedKeyBuilder.toString(), Skript.getInstance());
	}

	/**
	 * Decodes a NamespacedKey encoded by #getNamespacedKey
	 *
	 * @param namespacedKey the namespaced key to decode
	 * @return a Pair with the first element as the namespace and the second as the decoded key
	 */
	public static Pair<String, String> decodeNamespacedKey(NamespacedKey namespacedKey) {
		String encodedKey = namespacedKey.getKey();
		StringBuilder decodedKeyBuilder = new StringBuilder();
		int encodedKeyLength = encodedKey.length();
		int lastCharIndex = encodedKeyLength - 1;
		for (int i = 0; i < encodedKeyLength; i++) {
			char currentChar = encodedKey.charAt(i);
			// if we are encountering a ".x" hex sequence
			if (i != lastCharIndex && currentChar  == '.' && encodedKey.charAt(i + 1) == 'x') {
				i += 2; // skip the ".x" so it isn't part of our hex string
				StringBuilder hexString = new StringBuilder();
				// The hex sequence continues until a . is encountered or we reach the end of the string
				while (i <= lastCharIndex && encodedKey.charAt(i) != '.') {
					hexString.append(encodedKey.charAt(i));
					i++;
				}
				// if the . was the start of another ".x" hex sequence, back up by 1 character
				if (i <= lastCharIndex && encodedKey.charAt(i + 1) == 'x')
					i--;
				// parse the hex sequence to a char
				char decodedChar = (char) Long.parseLong(hexString.toString(), 16);
				decodedKeyBuilder.append(decodedChar);
			} else {
				// this is just a normal character, not a hex sequence
				// so we can just append it
				decodedKeyBuilder.append(currentChar);
			}
		}
		return new Pair<>(namespacedKey.getNamespace(), decodedKeyBuilder.toString());
	}

}