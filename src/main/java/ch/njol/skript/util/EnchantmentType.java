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
package ch.njol.skript.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Peter Güttinger
 */
public class EnchantmentType implements YggdrasilSerializable {

	private static final boolean HAS_REGISTRY = Skript.classExists("org.bukkit.Registry") && Skript.fieldExists(Registry.class, "ENCHANTMENT");

	private final Enchantment type;
	private final int level;

	/**
	 * Used for deserialisation only
	 */
	@SuppressWarnings({"unused", "null"})
	private EnchantmentType() {
		type = null;
		level = -1;
	}

	public EnchantmentType(final Enchantment type) {
		assert type != null;
		this.type = type;
		this.level = -1;
	}
	public EnchantmentType(final Enchantment type, final int level) {
		assert type != null;
		this.type = type;
		this.level = level;
	}

	/**
	 * @return level or 1 if level == -1
	 */
	public int getLevel() {
		return level == -1 ? 1 : level;
	}

	/**
	 * @return the internal level, can be -1
	 */
	public int getInternalLevel() {
		return level;
	}

	@Nullable
	public Enchantment getType() {
		return type;
	}

	/**
	 * Checks whether the given item type has this enchantment.
	 * @param item the item to be checked.
	 * @deprecated Use {@link ItemType#hasEnchantments(Enchantment...)}
	 */
	@Deprecated
	public boolean has(final ItemType item) {
		return item.hasEnchantments(type);
	}

	@Override
	public String toString() {
		return toString(type) + (level == -1 ? "" : " " + level);
	}

	@SuppressWarnings("null")
	public static String toString(final Enchantment enchantment) {
		return getEnchantmentName(enchantment);
	}

	// REMIND flags?
	@SuppressWarnings("null")
	public static String toString(final Enchantment enchantment, final int flags) {
		return toString(enchantment);
	}

	@SuppressWarnings("null")
	private final static Pattern pattern = Pattern.compile(".+ \\d+");

	/**
	 * Parses an enchantment type from string. This includes an {@link Enchantment}
	 * and its level.
	 * @param s String to parse.
	 * @return Enchantment type, or null if parsing failed.
	 */
	@Nullable
	public static EnchantmentType parse(final String s) {
		if (pattern.matcher(s).matches()) {
			String name = s.substring(0, s.lastIndexOf(' '));
			assert name != null;
			final Enchantment ench = parseEnchantment(name);
			if (ench == null)
				return null;
			String level = s.substring(s.lastIndexOf(' ') + 1);
			assert level != null;
			return new EnchantmentType(ench, Utils.parseInt(level));
		}
		final Enchantment ench = parseEnchantment(s);
		if (ench == null)
			return null;
		return new EnchantmentType(ench, -1);
	}

	@SuppressWarnings("deprecation")
	@Nullable
	public static Enchantment parseEnchantment(String s) {
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

	@SuppressWarnings({"null", "deprecation"})
	public static Collection<String> getNames() {
		List<String> names = new ArrayList<>();
		if (HAS_REGISTRY)
			Registry.ENCHANTMENT.forEach(enchantment -> names.add(getEnchantmentName(enchantment)));
		else
			for (Enchantment enchantment : Enchantment.values()) {
				names.add(getEnchantmentName(enchantment));
			}
		return names;
	}

	private static String getEnchantmentName(Enchantment enchantment) {
		NamespacedKey key = enchantment.getKey();
		// If it's a minecraft enchant, just return the key
		if (key.getNamespace().equalsIgnoreCase("minecraft"))
			return key.getKey();
		// Else if it's a custom enchant, return with the namespace
		// ex: `some_namespace:explosive`
		return key.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EnchantmentType))
			return false;
		final EnchantmentType other = (EnchantmentType) obj;
		if (level != other.level)
			return false;
		return type.equals(other.type);
	}

}
