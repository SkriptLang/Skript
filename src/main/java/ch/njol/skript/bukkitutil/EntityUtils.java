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

import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;

/**
 * Utility class for quick {@link Entity} methods
 */
public class EntityUtils {

	private static final boolean HAS_PIGLINS = Skript.classExists("org.bukkit.entity.Piglin");

	/**
	 * Cache Skript EntityData -> Bukkit EntityType
	 */
	private static final BiMap<EntityData<?>, EntityType> SPAWNER_TYPES = HashBiMap.create();

	static {
		for (EntityType e : EntityType.values()) {
			Class<? extends Entity> c = e.getEntityClass();
			if (c != null)
				SPAWNER_TYPES.put(EntityData.fromClass(c), e);
		}
	}

	/**
	 * Check if an entity is ageable.
	 * Some entities, such as zombies, do not have an age but can be a baby/adult.
	 *
	 * @param entity Entity to check
	 * @return True if entity is ageable
	 */
	public static boolean isAgeable(Entity entity) {
		if (entity instanceof Ageable || entity instanceof Zombie)
			return true;
		return HAS_PIGLINS && (entity instanceof Piglin || entity instanceof Zoglin);
	}

	/**
	 * Get the age of an ageable entity.
	 * Entities such as zombies do not have an age, this will return -1 if baby, 0 if adult.
	 *
	 * @param entity Entity to grab age for
	 * @return Age of entity (if zombie/piglin/zoglin -1 = baby, 0 = adult) (if not ageable, will return 0)
	 */
	public static int getAge(Entity entity) {
		if (entity instanceof Ageable)
			return ((Ageable) entity).getAge();
		else if (entity instanceof Zombie)
			return ((Zombie) entity).isBaby() ? -1 : 0;
		else if (HAS_PIGLINS) {
			if (entity instanceof Piglin)
				return ((Piglin) entity).isBaby() ? -1 : 0;
			else if (entity instanceof Zoglin)
				return ((Zoglin) entity).isBaby() ? -1 : 0;
		}
		return 0;
	}

	/**
	 * Set the age of an entity.
	 * Entities such as zombies do not have an age, setting below 0 will make them a baby otherwise adult.
	 *
	 * @param entity Entity to set age for
	 * @param age    Age to set
	 */
	public static void setAge(Entity entity, int age) {
		if (entity instanceof Ageable)
			((Ageable) entity).setAge(age);
		else if (entity instanceof Zombie)
			((Zombie) entity).setBaby(age < 0);
		else if (HAS_PIGLINS) {
			if (entity instanceof Piglin)
				((Piglin) entity).setBaby(age < 0);
			else if (entity instanceof Zoglin)
				((Zoglin) entity).setBaby(age < 0);
		}
	}

	/**
	 * Quick method for making an entity a baby.
	 * Ageable entities (such as sheep or pigs) will set their default baby age to -24000.
	 *
	 * @param entity Entity to make baby
	 */
	public static void setBaby(Entity entity) {
		setAge(entity, -24000);
	}

	/**
	 * Quick method for making an entity an adult.
	 *
	 * @param entity Entity to make adult
	 */
	public static void setAdult(Entity entity) {
		setAge(entity, 0);
	}

	/**
	 * Quick method to check if entity is an adult.
	 *
	 * @param entity Entity to check
	 * @return True if entity is an adult
	 */
	public static boolean isAdult(Entity entity) {
		return getAge(entity) >= 0;
	}

	/**
	 * Convert from Skript's EntityData to Bukkit's EntityType
	 * @param e Skript's EntityData
	 * @return Bukkit's EntityType
	 */
	public static EntityType toBukkitEntityType(EntityData<?> e) {
		return SPAWNER_TYPES.get(EntityData.fromClass(e.getType())); // Fix Comparison Issues
	}

	/**
	 * Convert from Bukkit's EntityType to Skript's EntityData
	 * @param e Bukkit's EntityType
	 * @return Skript's EntityData
	 */
	public static EntityData<?> toSkriptEntityData(EntityType e) {
		return SPAWNER_TYPES.inverse().get(e);
	}

	/**
	 * Teleports the given entity to the given location.
	 * Teleports to the given location in the entity's world if the location's world is null.
	 */
	public static void teleport(Entity entity, Location location) {
		if (location.getWorld() == null) {
			location = location.clone();
			location.setWorld(entity.getWorld());
		}

		entity.teleport(location);
	}

}
