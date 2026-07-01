package org.skriptlang.skript.bukkit.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for quick {@link Entity} methods
 */
public class EntityUtils {

	/**
	 * Cache Skript EntityData -> Bukkit EntityType
	 */
	private static final BiMap<EntityData<?>, org.bukkit.entity.EntityType> SPAWNER_TYPES = HashBiMap.create();
	private static final Map<Class<? extends Entity>, org.bukkit.entity.EntityType> CLASS_ENTITY_TYPE_MAP = new HashMap<>();

	static {
		for (org.bukkit.entity.EntityType entityType : org.bukkit.entity.EntityType.values()) {
			Class<? extends Entity> entityClass = entityType.getEntityClass();
			if (entityClass != null)
				CLASS_ENTITY_TYPE_MAP.put(entityClass, entityType);
		}
	}

	/**
	 * @deprecated The minimum supported version of MC 1.21.4, all entities covered in this method now extend {@link Ageable}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public static boolean isAgeable(Entity entity) {
		return entity instanceof Ageable;
	}

	/**
	 * Gets the age of an {@link Entity} that extends {@link Ageable}.
	 *
	 * @param entity The {@link Entity} to grab the age from.
	 * @return The age of the {@link Entity} if {@link Ageable}, otherwise {@code 0}.
	 */
	public static int getAge(Entity entity) {
		if (entity instanceof Ageable ageable)
			return ageable.getAge();
		return 0;
	}

	/**
	 * Sets the age of an {@link Entity} that extends {@link Ageable}.
	 * Setting below 0 will make them a baby otherwise adult.
	 *
	 * @param entity The {@link Entity} to set the age for.
	 * @param age The age to set to.
	 */
	public static void setAge(Entity entity, int age) {
		if (entity instanceof Ageable ageable)
			ageable.setAge(age);
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

	private static void loadSpawnerTypes() {
		for (org.bukkit.entity.EntityType e : org.bukkit.entity.EntityType.values()) {
			Class<? extends Entity> c = e.getEntityClass();
			if (c != null)
				SPAWNER_TYPES.put(EntityData.fromClass(c), e);
		}
	}

	/**
	 * Get the {@link org.bukkit.entity.EntityType} that {@code data} correlates to.
	 * @param data The {@link EntityData} to get the {@link EntityType} from.
	 * @return The correlating {@link org.bukkit.entity.EntityType}.
	 */
	public static org.bukkit.entity.EntityType toBukkitEntityType(EntityData<?> data) {
		if (SPAWNER_TYPES.isEmpty())
			loadSpawnerTypes();
		EntityData<?> entityData = EntityData.fromClass(data.getType()); // Fix Comparison Issues
		if (SPAWNER_TYPES.containsKey(entityData))
			return SPAWNER_TYPES.get(entityData);
        return toBukkitEntityType(data.getType());
	}

	/**
	 * Attempts to get an {@link EntityType} from a {@link Class} extending {@link Entity}.
	 * Ensures at least one {@link EntityType} can represent an entity class through {@link Class#isAssignableFrom(Class)}.
	 * @param entityClass The {@link Class} extending {@link Entity}
	 * @return The exact or assignable {@link EntityType} or {@code null}
	 */
	public static @Nullable org.bukkit.entity.EntityType toBukkitEntityType(Class<? extends Entity> entityClass) {
		if (CLASS_ENTITY_TYPE_MAP.containsKey(entityClass)) {
			return CLASS_ENTITY_TYPE_MAP.get(entityClass);
		}
		org.bukkit.entity.EntityType closestEntityType = null;
		Class<? extends Entity> closestClass = null;
		for (org.bukkit.entity.EntityType entityType : org.bukkit.entity.EntityType.values()) {
			Class<? extends Entity> typeClass = entityType.getEntityClass();
			if (typeClass != null && typeClass.isAssignableFrom(entityClass)) {
				if (closestEntityType == null || closestClass.isAssignableFrom(typeClass)) {
					closestEntityType = entityType;
					closestClass = typeClass;
					if (typeClass.equals(entityClass))
						break;
				}
			}
		}
		CLASS_ENTITY_TYPE_MAP.put(entityClass, closestEntityType);
		return closestEntityType;
	}

	/**
	 * Gets a {@link EntityData} from the provided {@code entityType}.
	 * @param entityType The {@link org.bukkit.entity.EntityType} to get the {@link EntityData}.
	 * @return The {@link EntityData}.
	 */
	public static EntityData<?> toSkriptEntityData(org.bukkit.entity.EntityType entityType) {
		if (SPAWNER_TYPES.isEmpty())
			loadSpawnerTypes();
		return SPAWNER_TYPES.inverse().get(entityType);
	}

}
