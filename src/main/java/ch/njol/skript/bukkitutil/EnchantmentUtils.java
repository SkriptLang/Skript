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

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps enchantments to their keys.
 */
public class EnchantmentUtils {

	private static final Map<Enchantment, String> NAMES = new HashMap<>();
	private static final Map<String, Enchantment> PATTERNS = new HashMap<>();
	private static final boolean HAS_REGISTRY = BukkitUtils.registryExists("ENCHANTMENT");

	static {
		if (!HAS_REGISTRY) {
			Language.addListener(() -> {
				NAMES.clear();
				PATTERNS.clear();
				for (Enchantment enchantment : Enchantment.values()) {
					NamespacedKey key = enchantment.getKey();
					final String[] names = Language.getList("enchantments." + key.getKey());

					if (!names[0].startsWith("enchantments.")) {
						NAMES.put(enchantment, names[0]);
						// Add lang file names
						for (String name : names)
							PATTERNS.put(name.toLowerCase(Locale.ENGLISH), enchantment);
					}
					// If Minecraft provided, add key without namespace and underscores (ex: "fire aspect")
					if (key.getNamespace().equalsIgnoreCase(NamespacedKey.MINECRAFT))
						PATTERNS.put(key.getKey().replace("_", " "), enchantment);
					// Add full namespaced key as pattern (ex: "minecraft:fire_aspect", "custom:floopy_floopy")
					PATTERNS.put(key.toString(), enchantment);
				}
			});
		}
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

	public static ClassInfo<Enchantment> createClassInfo() {
		return new ClassInfo<>(Enchantment.class, "enchantment")
			.parser(new Parser<>() {
				@Override
				@Nullable
				public Enchantment parse(final String s, final ParseContext context) {
					return EnchantmentUtils.parseEnchantment(s);
				}

				@Override
				public String toString(final Enchantment e, final int flags) {
					return EnchantmentUtils.toString(e, flags);
				}

				@Override
				public String toVariableNameString(final Enchantment e) {
					return "" + EnchantmentUtils.getKey(e);
				}
			}).serializer(new Serializer<>() {
				@Override
				public Fields serialize(final Enchantment ench) {
					final Fields f = new Fields();
					f.putObject("key", EnchantmentUtils.getKey(ench));
					return f;
				}

				@Override
				public boolean canBeInstantiated() {
					return false;
				}

				@Override
				public void deserialize(final Enchantment o, final Fields f) {
					assert false;
				}

				@Override
				protected Enchantment deserialize(final Fields fields) throws StreamCorruptedException {
					final String key = fields.getObject("key", String.class);
					assert key != null; // If a key happens to be null, something went really wrong...
					final Enchantment e = EnchantmentUtils.getByKey(key);
					if (e == null)
						throw new StreamCorruptedException("Invalid enchantment " + key);
					return e;
				}

				@Override
				@Nullable
				public Enchantment deserialize(String s) {
					return Enchantment.getByName(s);
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}
			})
			.usage(StringUtils.join(EnchantmentUtils.getNames(), ", "))
			.supplier(Enchantment.values());
	}

}
