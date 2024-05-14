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
package ch.njol.skript.bukkitutil;

import ch.njol.skript.localization.Language;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps enchantments to their keys.
 */
public class EnchantmentUtils {

	private static final Map<Enchantment, String> NAMES = new HashMap<>();
	private static final Map<String, Enchantment> PATTERNS = new HashMap<>();
	private static final boolean HAS_REGISTRY = Skript.classExists("org.bukkit.Registry") && Skript.fieldExists(Registry.class, "ENCHANTMENT");

	static {
		Language.addListener(() -> {
			NAMES.clear();
			PATTERNS.clear();
			List<Enchantment> enchantments = new ArrayList<>();
			if (HAS_REGISTRY) {
				Registry.ENCHANTMENT.forEach(enchantments::add);
			} else {
				enchantments.addAll(Arrays.asList(Enchantment.values()));
			}
			for (Enchantment enchantment : enchantments) {
				NamespacedKey key = enchantment.getKey();
				final String[] names = Language.getList("enchantments." + key.getKey());

				if (!names[0].startsWith("enchantments.")) {
					NAMES.put(enchantment, names[0]);
					// Add lang file names
					for (String name : names)
						PATTERNS.put(name.toLowerCase(Locale.ENGLISH), enchantment);
				}
				// If Minecraft provided, add key without namespace and underscores (ex: "fire aspect")
				if (key.getNamespace().equalsIgnoreCase("minecraft"))
					PATTERNS.put(key.getKey().replace("_", " "), enchantment);
				// Add full namespaced key as pattern (ex: "minecraft:fire_aspect", "custom:floopy_floopy")
				PATTERNS.put(key.toString(), enchantment);
			}
		});
	}

	public static String getKey(Enchantment enchantment) {
		return enchantment.getKey().toString();
	}

	@Nullable
	public static Enchantment getByKey(String key) {
		if (!key.contains(":")) {
			// Old method for old variables
			return Enchantment.getByKey(NamespacedKey.minecraft(key));
		} else {
			NamespacedKey namespacedKey = NamespacedKey.fromString(key);
			if (namespacedKey == null)
				return null;

			if (HAS_REGISTRY) {
				return Registry.ENCHANTMENT.get(namespacedKey);
			} else {
				return Enchantment.getByKey(namespacedKey);
			}
		}
	}

	@Nullable
	public static Enchantment parseEnchantment(String s) {
		return PATTERNS.get(s);
	}

	@SuppressWarnings("null")
	public static Collection<String> getNames() {
		return NAMES.values();
	}

	@SuppressWarnings("null")
	public static String toString(final Enchantment enchantment) {
		// If we have a name in the lang file, return that first
		if (NAMES.containsKey(enchantment))
			return NAMES.get(enchantment);

		// If no name is available, return the namespaced key
		return enchantment.getKey().toString();
	}

	// REMIND flags?
	@SuppressWarnings("null")
	public static String toString(final Enchantment enchantment, final int flags) {
		return toString(enchantment);
	}

}
