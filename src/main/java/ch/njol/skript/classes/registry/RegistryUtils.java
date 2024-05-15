/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.classes.registry;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for {@link Registry}
 *
 * @param <R> Registry class
 */
public class RegistryUtils<R extends Keyed> {

	private final Registry<R> registry;
	private final String languageNode;

	private final Map<R, String> names = new HashMap<>();
	private final Map<String, R> parseMap = new HashMap<>();

	public RegistryUtils(Registry<R> registry, String languageNode) {
		assert !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;
		this.registry = registry;
		this.languageNode = languageNode;
		refresh();
		Language.addListener(this::refresh);
	}

	private void refresh() {
		names.clear();
		parseMap.clear();
		for (R registryObject : registry) {
			NamespacedKey namespacedKey = registryObject.getKey();
			String namespace = namespacedKey.getNamespace();
			String key = namespacedKey.getKey();
			String keyWithSpaces = key.replace("_", " ");
			String languageKey = languageNode + "." + key;

			// Put the full namespaced key as a pattern
			parseMap.put(namespacedKey.toString(), registryObject);

			// If the object is a vanilla Minecraft object, we'll add the key with spaces as a pattern
			if (namespace.equalsIgnoreCase(NamespacedKey.MINECRAFT)) {
				parseMap.put(keyWithSpaces, registryObject);
			}

			String[] options = Language.getList(languageKey);
			// Missing/Custom registry objects
			if (options.length == 1 && options[0].equals(languageKey.toLowerCase(Locale.ENGLISH))) {
				if (namespace.equalsIgnoreCase(NamespacedKey.MINECRAFT)) {
					// If the object is a vanilla Minecraft object, we'll use the key with spaces as a name
					names.put(registryObject, keyWithSpaces);
				} else {
					// If the object is a custom object, we'll use the full namespaced key as a name
					names.put(registryObject, namespacedKey.toString());
				}
			} else {
				for (String option : options) {
					option = option.toLowerCase(Locale.ENGLISH);

					// Isolate the gender if one is present
					NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, languageKey);
					String first = strippedOption.getFirst();
					Integer second = strippedOption.getSecond();

					// Add to name map if needed
					names.putIfAbsent(registryObject, first);

					parseMap.put(first, registryObject);
					if (second != -1) { // There is a gender present
						parseMap.put(Noun.getArticleWithSpace(second, Language.F_INDEFINITE_ARTICLE) + first, registryObject);
					}
				}
			}
		}
	}

	/**
	 * This method attempts to match the string input against one of the string representations of the registry.
	 *
	 * @param input a string to attempt to match against one in the registry.
	 * @return The registry object matching the input, or null if no match could be made.
	 */
	@Nullable
	public R parse(String input) {
		return parseMap.get(input.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * This method returns the string representation of a registry.
	 *
	 * @param object The object to represent as a string.
	 * @param flags  not currently used
	 * @return A string representation of the registry object.
	 */
	public String toString(R object, int flags) {
		return names.get(object);
	}

	/**
	 * @return A comma-separated string containing a list of all names representing the registry.
	 * Note that some entries may represent the same registry object.
	 */
	public String getAllNames() {
		return StringUtils.join(parseMap.keySet(), ", ");
	}

}
