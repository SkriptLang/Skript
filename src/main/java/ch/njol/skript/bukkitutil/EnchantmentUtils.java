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

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ch.njol.skript.Skript;

/**
 * Maps enchantments to their ids in Minecraft 1.12.
 */
public class EnchantmentUtils {
	
	private static final boolean KEY_METHOD_EXISTS = Skript.methodExists(Enchantment.class, "getKey");
	private static final BiMap<Enchantment, String> ENCHANTMENTS = HashBiMap.create();
	
	static {
		ENCHANTMENTS.put(Enchantment.PROTECTION_ENVIRONMENTAL, "protection");
		ENCHANTMENTS.put(Enchantment.PROTECTION_FIRE, "fire_protection");
		ENCHANTMENTS.put(Enchantment.PROTECTION_FALL, "feather_falling");
		ENCHANTMENTS.put(Enchantment.PROTECTION_EXPLOSIONS, "blast_protection");
		ENCHANTMENTS.put(Enchantment.PROTECTION_PROJECTILE, "projectile_protection");
		ENCHANTMENTS.put(Enchantment.OXYGEN, "respiration");
		ENCHANTMENTS.put(Enchantment.WATER_WORKER, "aqua_affinity");
		ENCHANTMENTS.put(Enchantment.THORNS, "thorns");
		ENCHANTMENTS.put(Enchantment.DEPTH_STRIDER, "depth_strider");
		ENCHANTMENTS.put(Enchantment.DAMAGE_ALL, "sharpness");
		ENCHANTMENTS.put(Enchantment.DAMAGE_UNDEAD, "smite");
		ENCHANTMENTS.put(Enchantment.DAMAGE_ARTHROPODS, "bane_of_arthropods");
		ENCHANTMENTS.put(Enchantment.KNOCKBACK, "knockback");
		ENCHANTMENTS.put(Enchantment.FIRE_ASPECT, "fire_aspect");
		ENCHANTMENTS.put(Enchantment.LOOT_BONUS_MOBS, "looting");
		ENCHANTMENTS.put(Enchantment.DIG_SPEED, "efficiency");
		ENCHANTMENTS.put(Enchantment.SILK_TOUCH, "silk_touch");
		ENCHANTMENTS.put(Enchantment.DURABILITY, "unbreaking");
		ENCHANTMENTS.put(Enchantment.LOOT_BONUS_BLOCKS, "fortune");
		ENCHANTMENTS.put(Enchantment.ARROW_DAMAGE, "power");
		ENCHANTMENTS.put(Enchantment.ARROW_KNOCKBACK, "punch");
		ENCHANTMENTS.put(Enchantment.ARROW_FIRE, "flame");
		ENCHANTMENTS.put(Enchantment.ARROW_INFINITE, "infinity");
		ENCHANTMENTS.put(Enchantment.LUCK, "luck_of_the_sea");
		ENCHANTMENTS.put(Enchantment.LURE, "lure");
		
		if (Skript.isRunningMinecraft(1, 9)) {
			ENCHANTMENTS.put(Enchantment.FROST_WALKER, "frost_walker");
			ENCHANTMENTS.put(Enchantment.MENDING, "mending");
		}
		
		if (Skript.isRunningMinecraft(1, 11)) {
			ENCHANTMENTS.put(Enchantment.BINDING_CURSE, "binding_curse");
			ENCHANTMENTS.put(Enchantment.VANISHING_CURSE, "vanishing_curse");
			ENCHANTMENTS.put(Enchantment.SWEEPING_EDGE, "sweeping_edge");
		}
	}
	
	public static String getKey(Enchantment ench) {
		if (KEY_METHOD_EXISTS)
			return ench.getKey().getKey();
		String name = ENCHANTMENTS.get(ench);
		assert name != null : "missing name for " + ench;
		return name;
	}
	
	@Nullable
	public static Enchantment getByKey(String key) {
		if (KEY_METHOD_EXISTS)
			return Enchantment.getByKey(NamespacedKey.minecraft(key));
		return ENCHANTMENTS.inverse().get(key);
	}
}
