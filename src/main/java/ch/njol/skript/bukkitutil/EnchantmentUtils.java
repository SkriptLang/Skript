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
			List<Enchantment> enchantments = new ArrayList<>();
			if (HAS_REGISTRY) {
				Registry.ENCHANTMENT.forEach(enchantments::add);
			} else {
				enchantments.addAll(Arrays.asList(Enchantment.values()));
			}
			for (Enchantment enchantment : enchantments) {
				final String[] names = Language.getList("enchantments." + getKey(enchantment));
				NAMES.put(enchantment, names[0]);

				for (String name : names)
					PATTERNS.put(name.toLowerCase(Locale.ENGLISH), enchantment);
			}
		});
	}

	public static String getKey(Enchantment enchantment) {
		return enchantment.getKey().getKey();
	}

	@Nullable
	public static Enchantment getByKey(String key) {
		return Enchantment.getByKey(NamespacedKey.minecraft(key));
	}

	@SuppressWarnings("deprecation")
	@Nullable
	public static Enchantment parseEnchantment(String s) {
		s = s.toLowerCase(Locale.ROOT);
		// First try to parse from the lang file
		Enchantment enchantment = PATTERNS.get(s);
		if (enchantment != null)
			return enchantment;

		// If that fails, we move forward with getting from key
		s = s.replace(" ", "_");
		NamespacedKey key;
		try {
			if (s.contains(":"))
				key = NamespacedKey.fromString(s);
			else
				key = NamespacedKey.minecraft(s);
		} catch (IllegalArgumentException ignore) {
			return null;
		}
		if (key == null)
			return null;
		if (HAS_REGISTRY) // Registry added in Bukkit 1.14
			return Registry.ENCHANTMENT.get(key);
		return Enchantment.getByKey(key);
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

		NamespacedKey key = enchantment.getKey();
		// Else if it's a missing minecraft enchant, just return the key
		if (key.getNamespace().equalsIgnoreCase("minecraft"))
			return key.getKey();
		// Else if it's a custom enchant, return with the namespace
		// ex: `some_namespace:explosive`
		return key.toString();
	}

	// REMIND flags?
	@SuppressWarnings("null")
	public static String toString(final Enchantment enchantment, final int flags) {
		return toString(enchantment);
	}

}
