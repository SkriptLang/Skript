package ch.njol.skript.bukkitutil;

import ch.njol.skript.entity.EntityData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link org.skriptlang.skript.bukkit.entity.EntityUtils} instead.
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class EntityUtils {

	/**
	 * Check if an entity is ageable.
	 * Some entities, such as zombies, do not have an age but can be a baby/adult.
	 *
	 * @param entity Entity to check
	 * @return True if entity is ageable
	 */
	public static boolean isAgeable(Entity entity) {
		return org.skriptlang.skript.bukkit.entity.EntityUtils.isAgeable(entity);
	}

	/**
	 * Get the age of an ageable entity.
	 * Entities such as zombies do not have an age, this will return -1 if baby, 0 if adult.
	 *
	 * @param entity Entity to grab age for
	 * @return Age of entity (if zombie/piglin/zoglin -1 = baby, 0 = adult) (if not ageable, will return 0)
	 */
	public static int getAge(Entity entity) {
		return org.skriptlang.skript.bukkit.entity.EntityUtils.getAge(entity);
	}

	/**
	 * Set the age of an entity.
	 * Entities such as zombies do not have an age, setting below 0 will make them a baby otherwise adult.
	 *
	 * @param entity Entity to set age for
	 * @param age    Age to set
	 */
	public static void setAge(Entity entity, int age) {
		org.skriptlang.skript.bukkit.entity.EntityUtils.setAge(entity, age);
	}

	/**
	 * Quick method for making an entity a baby.
	 * Ageable entities (such as sheep or pigs) will set their default baby age to -24000.
	 *
	 * @param entity Entity to make baby
	 */
	public static void setBaby(Entity entity) {
		org.skriptlang.skript.bukkit.entity.EntityUtils.setBaby(entity);
	}

	/**
	 * Quick method for making an entity an adult.
	 *
	 * @param entity Entity to make adult
	 */
	public static void setAdult(Entity entity) {
		org.skriptlang.skript.bukkit.entity.EntityUtils.setAdult(entity);
	}

	/**
	 * Quick method to check if entity is an adult.
	 *
	 * @param entity Entity to check
	 * @return True if entity is an adult
	 */
	public static boolean isAdult(Entity entity) {
		return org.skriptlang.skript.bukkit.entity.EntityUtils.isAdult(entity);
	}

	/**
	 * Convert from Skript's EntityData to Bukkit's EntityType
	 * @param entityData Skript's EntityData
	 * @return Bukkit's EntityType
	 */
	public static EntityType toBukkitEntityType(EntityData<?> entityData) {
		return org.skriptlang.skript.bukkit.entity.EntityUtils
			.toBukkitEntityType((org.skriptlang.skript.bukkit.entity.EntityData<?>) entityData);
	}

	/**
	 * Attempts to get an {@link EntityType} from a {@link Class} extending {@link Entity}.
	 * Ensures at least one {@link EntityType} can represent an entity class through {@link Class#isAssignableFrom(Class)}.
	 * @param entityClass The {@link Class} extending {@link Entity}
	 * @return The exact or assignable {@link EntityType} or {@code null}
	 */
	public static @Nullable EntityType toBukkitEntityType(Class<? extends Entity> entityClass) {
		return org.skriptlang.skript.bukkit.entity.EntityUtils.toBukkitEntityType(entityClass);
	}

	/**
	 * Convert from Bukkit's EntityType to Skript's EntityData
	 * @param entityType Bukkit's EntityType
	 * @return Skript's EntityData
	 */
	public static EntityData<?> toSkriptEntityData(EntityType entityType) {
		return org.skriptlang.skript.bukkit.entity.EntityUtils.toSkriptEntityData(entityType);
	}

}
